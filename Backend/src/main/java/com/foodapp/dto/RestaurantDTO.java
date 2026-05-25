package com.foodapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Restaurant — exposes only safe, non-sensitive fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDTO {

    private Long id;
    private String name;
    private String cuisine;
    private String address;
    private String imageUrl;
    private Double rating;
}
