package com.foodapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for a user's full cart, including all items and grand total.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    private Long cartId;
    private List<CartItemDTO> items;
    private Double grandTotal;
}
