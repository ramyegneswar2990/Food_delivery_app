package com.foodapp.service;

import com.foodapp.dto.MenuItemDTO;

import java.util.List;

/**
 * Service interface for menu-related operations.
 */
public interface MenuService {

    /**
     * Returns all available menu items for a given restaurant.
     * Throws ResourceNotFoundException if the restaurant does not exist.
     */
    List<MenuItemDTO> getMenuByRestaurant(Long restaurantId);
}
