package com.ayush.backend.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ayush.backend.model.Order;

public interface OrderRepo extends JpaRepository<Order, Long>{
    Optional<Order> findByOrderId(String orderId);
}
