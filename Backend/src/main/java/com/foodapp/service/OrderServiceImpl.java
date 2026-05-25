package com.foodapp.service;

import com.foodapp.dto.OrderItemDTO;
import com.foodapp.dto.OrderResponse;
import com.foodapp.exception.ResourceNotFoundException;
import com.foodapp.model.*;
import com.foodapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public OrderResponse placeOrder(Long userId) {
        log.info("Placing order for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalStateException("Cart is empty"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            log.warn("Order placement failed - cart is empty for user ID: {}", userId);
            throw new IllegalStateException("Cart is empty");
        }

        // Get restaurant from the first cart item's menu item
        Restaurant restaurant = cart.getItems().get(0).getMenuItem().getRestaurant();

        Order order = Order.builder()
                .user(user)
                .restaurant(restaurant)
                .status(OrderStatus.PLACED)
                .orderedAt(LocalDateTime.now())
                .build();

        // Map CartItems to OrderItems and calculate total amount
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;
        for (CartItem cartItem : cart.getItems()) {
            MenuItem menuItem = cartItem.getMenuItem();
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(cartItem.getQuantity())
                    .price(menuItem.getPrice())
                    .build();
            orderItems.add(orderItem);
            totalAmount += menuItem.getPrice() * cartItem.getQuantity();
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        // Save order (cascade will save all order items)
        Order savedOrder = orderRepository.save(order);
        log.info("Order saved with ID: {} and total: {}", savedOrder.getId(), savedOrder.getTotalAmount());

        // Create OrderHistory entry (status change: null -> PLACED)
        OrderHistory history = OrderHistory.builder()
                .order(savedOrder)
                .previousStatus("null")
                .newStatus(OrderStatus.PLACED.name())
                .changedAt(LocalDateTime.now())
                .build();
        orderHistoryRepository.save(history);

        // Clear the user's cart
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Cleared cart for user ID: {}", userId);

        // Send order confirmation email asynchronously
        emailService.sendOrderConfirmationEmail(user, savedOrder);

        // Save Notification record
        Notification notification = Notification.builder()
                .user(user)
                .type("ORDER_PLACED")
                .message("Your order #" + savedOrder.getId() + " has been placed successfully.")
                .sent(true)
                .sentAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);

        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        log.info("Cancelling order ID: {} for user ID: {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(userId)) {
            log.warn("Access Denied - order ID: {} does not belong to user ID: {}", orderId, userId);
            throw new AccessDeniedException("You do not have permission to cancel this order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("Cancellation failed - order ID: {} is already cancelled", orderId);
            throw new IllegalStateException("Order is already cancelled");
        }

        // Eagerly load user to avoid LazyInitializationException in email/notification
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String previousStatus = order.getStatus().name();
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        // Create OrderHistory entry
        OrderHistory history = OrderHistory.builder()
                .order(savedOrder)
                .previousStatus(previousStatus)
                .newStatus(OrderStatus.CANCELLED.name())
                .changedAt(LocalDateTime.now())
                .build();
        orderHistoryRepository.save(history);

        // Save Notification record
        Notification notification = Notification.builder()
                .user(user)
                .type("ORDER_CANCELLED")
                .message("Your order #" + savedOrder.getId() + " has been cancelled.")
                .sent(true)
                .sentAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);

        // Send cancellation email — wrapped so a mail failure doesn't break cancel
        try {
            emailService.sendOrderCancellationEmail(user, savedOrder);
        } catch (Exception e) {
            log.warn("Failed to send cancellation email for order {}: {}", savedOrder.getId(), e.getMessage());
        }

        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistory(Long userId) {
        log.info("Fetching order history for user ID: {}", userId);
        List<Order> orders = orderRepository.findByUser_IdOrderByOrderedAtDesc(userId);
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long userId, Long orderId) {
        log.info("Fetching order ID: {} for user ID: {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(userId)) {
            log.warn("Access Denied - order ID: {} does not belong to user ID: {}", orderId, userId);
            throw new AccessDeniedException("You do not have permission to view this order");
        }

        return mapToOrderResponse(order);
    }

    // ── Helper Mapper ─────────────────────────────────────────────────────────

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(oi -> OrderItemDTO.builder()
                        .itemName(oi.getMenuItem().getName())
                        .quantity(oi.getQuantity())
                        .price(oi.getPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getId())
                .restaurantName(order.getRestaurant().getName())
                .items(itemDTOs)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .orderedAt(order.getOrderedAt())
                .build();
    }
}
