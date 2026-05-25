package com.foodapp.controller;

import com.foodapp.dto.ApiResponse;
import com.foodapp.dto.OrderResponse;
import com.foodapp.security.CustomUserDetails;
import com.foodapp.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder() {
        Long userId = getAuthenticatedUserId();
        OrderResponse response = orderService.placeOrder(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Order placed successfully"));
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long orderId) {
        Long userId = getAuthenticatedUserId();
        OrderResponse response = orderService.cancelOrder(userId, orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Order cancelled successfully"));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrderHistory() {
        Long userId = getAuthenticatedUserId();
        List<OrderResponse> response = orderService.getOrderHistory(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Order history retrieved successfully"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long orderId) {
        Long userId = getAuthenticatedUserId();
        OrderResponse response = orderService.getOrderById(userId, orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Order details retrieved successfully"));
    }

    // ── Private Helper ────────────────────────────────────────────────────────

    /**
     * Extracts the authenticated user's ID from the Spring Security context.
     * Relies on CustomUserDetails exposing getId().
     */
    private Long getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((CustomUserDetails) principal).getId();
    }
}
