package com.foodapp.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a single item line within a user's Cart.
 * The itemTotal is computed as quantity * menuItem.price.
 */
@Entity
@Table(name = "cart_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @ToString.Exclude
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Builder.Default
    @Column(nullable = false)
    private int quantity = 1;

    /**
     * Computed field: quantity * menuItem.price.
     * Updated manually whenever quantity or menuItem changes.
     */
    private Double itemTotal;

    /**
     * Recalculates itemTotal based on current quantity and menuItem price.
     */
    public void calculateItemTotal() {
        if (this.menuItem != null && this.menuItem.getPrice() != null) {
            this.itemTotal = this.quantity * this.menuItem.getPrice();
        }
    }
}
