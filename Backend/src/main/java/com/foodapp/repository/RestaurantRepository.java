package com.foodapp.repository;

import com.foodapp.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     * Returns only restaurants where the active flag is true.
     * Used for the customer-facing restaurant listing page.
     */
    List<Restaurant> findByActiveTrue();

    /**
     * Used by DataSeeder to skip restaurants that are already seeded.
     */
    boolean existsByName(String name);
}
