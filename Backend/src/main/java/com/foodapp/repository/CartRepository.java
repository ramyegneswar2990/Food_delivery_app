package com.foodapp.repository;

import com.foodapp.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Retrieves a user's cart by their user ID.
     * Returns empty Optional if the user has no active cart yet.
     */
    Optional<Cart> findByUser_Id(Long userId);
}
