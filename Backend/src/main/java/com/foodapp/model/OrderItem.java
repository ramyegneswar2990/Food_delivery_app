package com.foodapp.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a snapshot of a menu item within a placed Order.
 * Price is captured at the time of ordering to maintain historical accuracy.
 */
@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false)
    private int quantity;

    /**
     * Price captured at time of order placement (not live menu price).
     */
    @Column(nullable = false)
    private Double price;
}
