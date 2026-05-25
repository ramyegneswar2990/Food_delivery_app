package com.foodapp.controller;

import com.foodapp.dto.ApiResponse;
import com.foodapp.dto.CartDTO;
import com.foodapp.dto.CartItemRequest;
import com.foodapp.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for cart management.
 * All endpoints require authentication (enforced by security config from Member 3).
 * The logged-in user's ID is resolved from the Spring Security context.
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * GET /api/cart
     * Returns the current user's cart.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartDTO>> getCart() {
        Long userId = getAuthenticatedUserId();
        CartDTO cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart fetched successfully"));
    }

    /**
     * POST /api/cart/add
     * Adds a menu item to the cart, or increments quantity if already present.
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartDTO>> addItem(
            @Valid @RequestBody CartItemRequest request) {
        Long userId = getAuthenticatedUserId();
        CartDTO cart = cartService.addItem(userId, request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item added to cart"));
    }

    /**
     * DELETE /api/cart/remove/{cartItemId}
     * Removes a specific item from the cart.
     */
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<ApiResponse<CartDTO>> removeItem(
            @PathVariable Long cartItemId) {
        Long userId = getAuthenticatedUserId();
        CartDTO cart = cartService.removeItem(userId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item removed from cart"));
    }

    /**
     * PUT /api/cart/update-by-menu/{menuItemId}
     * Updates quantity using the STABLE menuItemId (avoids stale cartItemId issues).
     * If quantity <= 0 the item is removed.
     */
    @PutMapping("/update-by-menu/{menuItemId}")
    public ResponseEntity<ApiResponse<CartDTO>> updateByMenu(
            @PathVariable Long menuItemId,
            @RequestBody CartItemRequest request) {
        Long userId = getAuthenticatedUserId();
        CartDTO cart = cartService.updateItemQuantityByMenuId(userId, menuItemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart updated"));
    }

    /**
     * PUT /api/cart/update/{cartItemId}
     * Updates the quantity of a specific cart item.
     * If quantity <= 0, the item is removed.
     */
    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<ApiResponse<CartDTO>> updateItem(
            @PathVariable Long cartItemId,
            @RequestBody CartItemRequest request) {
        Long userId = getAuthenticatedUserId();
        CartDTO cart = cartService.updateItemQuantity(userId, cartItemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart updated"));
    }

    /**
     * DELETE /api/cart/clear
     * Clears all items from the cart.
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        Long userId = getAuthenticatedUserId();
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared"));
    }

    // ── Private Helper ────────────────────────────────────────────────────────

    /**
     * Extracts the authenticated user's ID from the Spring Security context.
     * Relies on the custom UserDetails implementation (Member 3) exposing getId().
     */
    private Long getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Cast to the custom UserDetails that exposes getId()
        return ((com.foodapp.security.CustomUserDetails) principal).getId();
    }
}
