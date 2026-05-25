package com.foodapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String restaurantName;
    private List<OrderItemDTO> items;
    private Double totalAmount;
    private String status;
    private LocalDateTime orderedAt;
    private String deliveryAddress;
    private String contactNumber;
}
