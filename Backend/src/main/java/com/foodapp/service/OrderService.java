package com.foodapp.service;

import com.foodapp.dto.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(Long userId);
    OrderResponse cancelOrder(Long userId, Long orderId);
    List<OrderResponse> getOrderHistory(Long userId);
    OrderResponse getOrderById(Long userId, Long orderId);
}
