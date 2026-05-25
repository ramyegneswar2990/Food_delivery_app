package com.foodapp.service;

import com.foodapp.dto.RestaurantDTO;

import java.util.List;

/**
 * Service interface for restaurant-related operations.
 */
public interface RestaurantService {

    /**
     * Returns all restaurants with active = true.
     */
    List<RestaurantDTO> getAllActiveRestaurants();

    /**
     * Returns a single restaurant by ID, or throws ResourceNotFoundException.
     */
    RestaurantDTO getRestaurantById(Long id);
}
