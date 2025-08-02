package com.ayush.backend.service;

import org.w3c.dom.ranges.RangeException;

import com.razorpay.Order;

public interface OrderService {
    Order createOrder(String plan, String clerkId) throws RangeException;
}
