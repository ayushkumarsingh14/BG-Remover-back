
package com.ayush.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ayush.backend.dto.RazorpayDTO;
import com.ayush.backend.response.RemoveBGResponse;
import com.ayush.backend.service.OrderService;
import com.ayush.backend.service.RazorpayService;
import com.razorpay.Order;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final RazorpayService razorpayService;

    // ✅ Create Razorpay Order
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestParam String planId, Authentication authentication) {
        System.out.println("DEBUG: Authenticated user = " + (authentication != null ? authentication.getName() : "null"));
        System.out.println("DEBUG: Received planId = " + planId);

        if (authentication == null || authentication.getName() == null || authentication.getName().isEmpty()) {
            System.out.println("DEBUG: User not authenticated. Access denied.");
            RemoveBGResponse response = RemoveBGResponse.builder()
                    .success(false)
                    .data("User does not have access to this resource")
                    .statusCode(HttpStatus.FORBIDDEN)
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            System.out.println("DEBUG: Creating order for user: " + authentication.getName());
            Order order = orderService.createOrder(planId, authentication.getName());

            System.out.println("DEBUG: Razorpay Order Created: " + order);

            RazorpayDTO responseDto = convertToDTO(order);

            System.out.println("DEBUG: Converted RazorpayDTO: " + responseDto);

            RemoveBGResponse response = RemoveBGResponse.builder()
                    .success(true)
                    .statusCode(HttpStatus.CREATED)
                    .data(responseDto)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println("ERROR: Failed to create order. Reason: " + e.getMessage());
            e.printStackTrace();

            RemoveBGResponse errorResponse = RemoveBGResponse.builder()
                    .success(false)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .data("Failed to create order: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ✅ Convert Razorpay Order to DTO
    private RazorpayDTO convertToDTO(Order order) {
        return RazorpayDTO.builder()
                .id(order.get("id"))
                .entity(order.get("entity"))
                .amount(order.get("amount"))
                .currency(order.get("currency"))
                .status(order.get("status"))
                .createdAt(order.get("createdAt"))
                .receipt(order.get("receipt"))
                .build();
    }

    // ✅ Verify Razorpay Payment
    @PostMapping("/verify")
public ResponseEntity<?> verifyOrder(@RequestBody Map<String, Object> request)
 throws RazorpayException {
        System.out.println("DEBUG: Verifying Razorpay payment with params: " + request);

        try {
            if (!request.containsKey("razorpay_order_id") || request.get("razorpay_order_id") == null) {
                throw new IllegalArgumentException("Missing razorpay_order_id parameter");
            }

            String razorpayOrder = String.valueOf(request.get("razorpay_order_id"));

            System.out.println("DEBUG: Verifying Razorpay Order ID: " + razorpayOrder);

            Map<String, Object> returnValue = razorpayService.verifyPayment(razorpayOrder);

            System.out.println("DEBUG: Verification result: " + returnValue);

            return ResponseEntity.ok(returnValue);

        } catch (Exception e) {
            System.out.println("ERROR: Payment verification failed. Reason: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", "failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
