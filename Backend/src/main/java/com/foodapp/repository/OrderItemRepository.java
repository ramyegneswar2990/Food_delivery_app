package com.foodapp.repository;

import com.foodapp.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Returns all items for a specific order.
     */
    List<OrderItem> findByOrder_Id(Long orderId);
}
