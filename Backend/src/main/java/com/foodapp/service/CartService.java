package com.foodapp.service;

import com.foodapp.dto.CartDTO;
import com.foodapp.dto.CartItemRequest;

/**
 * Service interface for cart-related operations.
 */
public interface CartService {

    /**
     * Returns the cart for the given user, creating an empty one if none exists.
     */
    CartDTO getCart(Long userId);

    /**
     * Adds a menu item to the user's cart, or increments quantity if already present.
     * Throws ResourceNotFoundException if the menu item does not exist or is unavailable.
     */
    CartDTO addItem(Long userId, CartItemRequest request);

    /**
     * Removes a specific cart item from the user's cart.
     * Throws ResourceNotFoundException if the item is not found or belongs to a different cart.
     */
    CartDTO removeItem(Long userId, Long cartItemId);

    /**
     * Clears all items from the user's cart.
     */
    void clearCart(Long userId);
}
