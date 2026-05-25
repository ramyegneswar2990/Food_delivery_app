package com.foodapp.controller;

import com.foodapp.dto.ApiResponse;
import com.foodapp.dto.MenuItemDTO;
import com.foodapp.dto.RestaurantDTO;
import com.foodapp.service.MenuService;
import com.foodapp.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public REST controller for browsing restaurants and their menus.
 * No authentication required — security config (Member 3) whitelists these endpoints.
 */
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuService menuService;

    /**
     * GET /api/restaurants
     * Returns all active restaurants.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantDTO>>> getAllRestaurants() {
        List<RestaurantDTO> restaurants = restaurantService.getAllActiveRestaurants();
        return ResponseEntity.ok(ApiResponse.success(restaurants, "Restaurants fetched successfully"));
    }

    /**
     * GET /api/restaurants/{id}
     * Returns a single restaurant by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantDTO>> getRestaurantById(@PathVariable Long id) {
        RestaurantDTO restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(ApiResponse.success(restaurant, "Restaurant fetched successfully"));
    }

    /**
     * GET /api/restaurants/{id}/menu
     * Returns available menu items for a given restaurant.
     */
    @GetMapping("/{id}/menu")
    public ResponseEntity<ApiResponse<List<MenuItemDTO>>> getMenu(@PathVariable Long id) {
        List<MenuItemDTO> menu = menuService.getMenuByRestaurant(id);
        return ResponseEntity.ok(ApiResponse.success(menu, "Menu fetched successfully"));
    }
}
