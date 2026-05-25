package com.foodapp.repository;

import com.foodapp.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Returns all notifications for a user, useful for notification centre.
     */
    List<Notification> findByUser_Id(Long userId);

    /**
     * Returns unsent notifications that need to be dispatched.
     */
    List<Notification> findBySentFalse();
}
