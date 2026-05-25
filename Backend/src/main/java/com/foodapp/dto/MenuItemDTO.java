package com.foodapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for MenuItem.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDTO {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String imageUrl;
    private boolean available;
}
