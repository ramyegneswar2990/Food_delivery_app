package com.foodapp.repository;

import com.foodapp.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /**
     * Returns available menu items for a specific restaurant.
     * Filters out items marked as unavailable (out of stock, discontinued, etc.).
     */
    List<MenuItem> findByRestaurant_IdAndAvailableTrue(Long restaurantId);

    /**
     * Returns all menu items for a restaurant regardless of availability.
     * Used for admin/restaurant management views.
     */
    List<MenuItem> findByRestaurant_Id(Long restaurantId);
}
