package com.foodapp.service;

import com.foodapp.model.Order;
import com.foodapp.model.User;

public interface EmailService {
    void sendRegistrationEmail(User user);
    void sendOrderConfirmationEmail(User user, Order order);
    void sendOrderCancellationEmail(User user, Order order);
}
