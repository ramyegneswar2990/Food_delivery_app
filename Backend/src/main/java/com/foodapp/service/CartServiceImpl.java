package com.foodapp.service;

import com.foodapp.dto.CartDTO;
import com.foodapp.dto.CartItemDTO;
import com.foodapp.dto.CartItemRequest;
import com.foodapp.exception.ResourceNotFoundException;
import com.foodapp.model.Cart;
import com.foodapp.model.CartItem;
import com.foodapp.model.MenuItem;
import com.foodapp.model.User;
import com.foodapp.repository.CartRepository;
import com.foodapp.repository.MenuItemRepository;
import com.foodapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link CartService}.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    // ── Public Operations ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CartDTO getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return toCartDTO(cart);
    }

    @Override
    public CartDTO addItem(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        // Verify menu item exists and is available
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .filter(MenuItem::isAvailable)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu item not found or unavailable: " + request.getMenuItemId()));

        // Check if the item is already in the cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(ci -> ci.getMenuItem().getId().equals(menuItem.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Increment quantity on existing line
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItem.calculateItemTotal();
        } else {
            // Add new line item
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(request.getQuantity())
                    .build();
            cartItem.calculateItemTotal();
            cart.getItems().add(cartItem);
        }

        Cart saved = cartRepository.save(cart);
        return toCartDTO(saved);
    }

    @Override
    public CartDTO removeItem(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(ci -> ci.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found or does not belong to this cart: " + cartItemId));

        cart.getItems().remove(cartItem);
        Cart saved = cartRepository.save(cart);
        return toCartDTO(saved);
    }

    @Override
    public CartDTO updateItemQuantityByMenuId(Long userId, Long menuItemId, int quantity) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(ci -> ci.getMenuItem().getId().equals(menuItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found for menu item: " + menuItemId));

        if (quantity <= 0) {
            cart.getItems().remove(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItem.calculateItemTotal();
        }

        Cart saved = cartRepository.save(cart);
        return toCartDTO(saved);
    }

    @Override
    public CartDTO updateItemQuantity(Long userId, Long cartItemId, int quantity) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cart.getItems().stream()
                .filter(ci -> ci.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found: " + cartItemId));

        if (quantity <= 0) {
            cart.getItems().remove(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItem.calculateItemTotal();
        }

        Cart saved = cartRepository.save(cart);
        return toCartDTO(saved);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Retrieves the cart for a user, or creates and persists a new empty one.
     */
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUser_Id(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            Cart newCart = Cart.builder().user(user).build();
            return cartRepository.save(newCart);
        });
    }

    /**
     * Calculates the grand total by summing all CartItem itemTotal values.
     */
    private Double calculateGrandTotal(Cart cart) {
        return cart.getItems().stream()
                .mapToDouble(ci -> ci.getItemTotal() != null ? ci.getItemTotal() : 0.0)
                .sum();
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private CartDTO toCartDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(this::toCartItemDTO)
                .collect(Collectors.toList());

        return CartDTO.builder()
                .cartId(cart.getId())
                .items(itemDTOs)
                .grandTotal(calculateGrandTotal(cart))
                .build();
    }

    private CartItemDTO toCartItemDTO(CartItem ci) {
        return CartItemDTO.builder()
                .cartItemId(ci.getId())
                .menuItemId(ci.getMenuItem().getId())
                .itemName(ci.getMenuItem().getName())
                .price(ci.getMenuItem().getPrice())
                .quantity(ci.getQuantity())
                .itemTotal(ci.getItemTotal())
                .build();
    }
}
