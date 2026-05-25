package com.foodapp.repository;

import com.foodapp.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Returns all orders for a user, sorted newest-first.
     * Used for customer order history pages.
     */
    List<Order> findByUser_IdOrderByOrderedAtDesc(Long userId);

    /**
     * Returns all orders for a given restaurant, sorted newest-first.
     * Used for restaurant management dashboards.
     */
    List<Order> findByRestaurant_IdOrderByOrderedAtDesc(Long restaurantId);
}
