package com.ayush.backend.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.ayush.backend.repo.OrderRepo;
import com.ayush.backend.service.OrderService;
import com.ayush.backend.service.RazorpayService;
import com.razorpay.Order;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final RazorpayService razorpayService;
    private final OrderRepo orderRepo;

    private final Map<String, PlanDetails> PLAN_DETAILS = Map.of(
        "Basic", new PlanDetails("Basic", 100, 499.00),
        "Premium", new PlanDetails("Premium", 250, 899.00),
        "Ultimate", new PlanDetails("Ultimate", 100, 1499.00)
    );

    private record PlanDetails(String name, int credits, double amount) {}

    @Override
    public Order createOrder(String planId, String clerkId) {
        System.out.println("➡️ createOrder() called with planId: " + planId + ", clerkId: " + clerkId);

        PlanDetails details = PLAN_DETAILS.get(planId);

        if (details == null) {
            System.out.println("❌ Invalid planId received: " + planId);
            throw new IllegalArgumentException("Invalid planId: " + planId);
        }

        System.out.println("✅ Plan Found => Name: " + details.name() + ", Credits: " + details.credits() + ", Amount: " + details.amount());

        try {
            System.out.println("🚀 Creating Razorpay order for amount: " + details.amount() + " INR");

            Order razorpayOrder = razorpayService.createdOrder(details.amount(), "INR");

            System.out.println("✅ Razorpay order created. Razorpay Order ID: " + razorpayOrder.get("id"));

            com.ayush.backend.model.Order newOrder = com.ayush.backend.model.Order.builder()
                    .clerkId(clerkId)
                    .plan(details.name())
                    .credits(details.credits())
                    .amount(details.amount())
                    .orderId(razorpayOrder.get("id"))
                    .build();

            System.out.println("💾 Saving order to DB: " + newOrder);
            orderRepo.save(newOrder);
            System.out.println("✅ Order saved successfully for Clerk ID: " + clerkId);

            return razorpayOrder;

        } catch (Exception e) {
            System.out.println("🔥 Exception occurred while creating order: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Order creation failed");
        }
    }
}
