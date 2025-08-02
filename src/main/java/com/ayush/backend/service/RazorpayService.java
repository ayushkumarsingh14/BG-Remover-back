package com.ayush.backend.service;

import java.util.Map;

import com.razorpay.Order;
import com.razorpay.RazorpayException;

public interface RazorpayService {
    Order createdOrder(Double amount, String currency) throws RazorpayException;

    Map<String, Object> verifyPayment(String razorpayOrderId) throws RazorpayException;
}
