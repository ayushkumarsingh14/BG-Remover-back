package com.ayush.backend.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ayush.backend.dto.UserDTO;
import com.ayush.backend.model.Order;
import com.ayush.backend.repo.OrderRepo;
import com.ayush.backend.service.RazorpayService;
import com.ayush.backend.service.UserService;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RazorpayServiceImpl implements RazorpayService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private final OrderRepo orderRepo;
    private final UserService userService;

    @Override
    public com.razorpay.Order createdOrder(Double amount, String currency) throws RazorpayException {
        try {
            System.out.println("Creating Razorpay client...");
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            System.out.println("Preparing order request payload...");
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount * 100); // amount in paise
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "order_receipt_" + System.currentTimeMillis());
            orderRequest.put("payment_capture", 1);

            System.out.println("Sending order create request to Razorpay...");
            com.razorpay.Order createdOrder = razorpayClient.orders.create(orderRequest);

            System.out.println("Order created successfully with ID: " + createdOrder.get("id"));
            return createdOrder;

        } catch (RazorpayException e) {
            System.out.println("Error while creating Razorpay order: " + e.getMessage());
            throw new RazorpayException("Razorpay error: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> verifyPayment(String razorpayOrderId) throws RazorpayException {
        Map<String, Object> returnValue = new HashMap<>();
        try {
            System.out.println("Verifying payment for Razorpay order ID: " + razorpayOrderId);

            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            com.razorpay.Order orderInfo = razorpayClient.orders.fetch(razorpayOrderId);

            System.out.println("Fetched order status from Razorpay: " + orderInfo.get("status"));

            if (orderInfo.get("status").toString().equalsIgnoreCase("paid")) {

                System.out.println("Order is marked as paid. Checking in database...");

                Order existingOrder = orderRepo.findByOrderId(razorpayOrderId)
                        .orElseThrow(() -> new RuntimeException("Order not found " + razorpayOrderId));

                System.out.println("Existing order found. Payment status: " + existingOrder.getPayment());

                if (existingOrder.getPayment()) {
                    System.out.println("Payment already marked as true. Returning failed response.");
                    returnValue.put("status", false);
                    returnValue.put("message", "payment failed");
                    return returnValue;
                }

                System.out.println("Fetching user details with Clerk ID: " + existingOrder.getClerkId());
                UserDTO userDTO = userService.getUserByClerkId(existingOrder.getClerkId());

                System.out.println("Adding credits. Previous: " + userDTO.getCredits() +
                        ", To add: " + existingOrder.getCredits());

                userDTO.setCredits(userDTO.getCredits() + existingOrder.getCredits());

                userService.saveUser(userDTO);

                System.out.println("Credits added and user saved successfully.");

                existingOrder.setPayment(true);
                orderRepo.save(existingOrder);

                System.out.println("Order marked as paid and saved.");

                returnValue.put("status", true);
                returnValue.put("message", "Credits added");
                return returnValue;
            } else {
                System.out.println("Order is not paid yet.");
            }

        } catch (Exception e) {
            System.out.println("Error while verifying payment: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while verifying the payment ");
        }

        return returnValue;
    }
}
