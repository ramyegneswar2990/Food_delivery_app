package com.foodapp.model;

/**
 * Represents the lifecycle stages of a food delivery order.
 */
public enum OrderStatus {
    PLACED,
    CONFIRMED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
