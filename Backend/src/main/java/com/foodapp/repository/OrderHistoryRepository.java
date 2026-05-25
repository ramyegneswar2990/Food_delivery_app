package com.foodapp.repository;

import com.foodapp.model.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    /**
     * Returns the audit history for a specific order.
     */
    Optional<OrderHistory> findByOrder_Id(Long orderId);

    /**
     * Returns all history records sorted newest-first.
     */
    List<OrderHistory> findAllByOrderByChangedAtDesc();
}
