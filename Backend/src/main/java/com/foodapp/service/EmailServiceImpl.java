package com.foodapp.service;

import com.foodapp.model.Order;
import com.foodapp.model.OrderItem;
import com.foodapp.model.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendRegistrationEmail(User user) {
        log.info("Starting async execution to send registration email to {}", user.getEmail());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom("ramyegneswarseeram@gmail.com", "FoodApp");
            helper.setTo(user.getEmail());
            helper.setSubject("Welcome to FoodApp!");
            
            String htmlContent = "<div style=\"font-family: 'Outfit', 'Inter', sans-serif; padding: 20px; background-color: #f7f9fc; color: #333;\">" +
                    "  <div style=\"max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border: 1px solid #eef2f6;\">" +
                    "    <div style=\"background: linear-gradient(135deg, #FF416C, #FF4B2B); padding: 30px; text-align: center; color: white;\">" +
                    "      <h1 style=\"margin: 0; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;\">Welcome to FoodApp!</h1>" +
                    "    </div>" +
                    "    <div style=\"padding: 30px; line-height: 1.6;\">" +
                    "      <p style=\"font-size: 16px; margin-top: 0;\">Hi <strong>" + user.getName() + "</strong>,</p>" +
                    "      <p style=\"font-size: 15px; color: #555;\">We are thrilled to welcome you! Your account has been created successfully and you are ready to explore our wide selection of delicious cuisines.</p>" +
                    "      <div style=\"text-align: center; margin: 30px 0;\">" +
                    "        <a href=\"http://localhost:3000\" style=\"display: inline-block; padding: 12px 30px; background: linear-gradient(135deg, #FF416C, #FF4B2B); color: white; text-decoration: none; border-radius: 25px; font-weight: 600; box-shadow: 0 4px 10px rgba(255, 75, 43, 0.3);\">Start Ordering Now</a>" +
                    "      </div>" +
                    "      <p style=\"font-size: 14px; color: #888; border-top: 1px solid #eee; padding-top: 20px; margin-bottom: 0;\">If you did not sign up for this account, please ignore this email.</p>" +
                    "    </div>" +
                    "  </div>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Registration email sent successfully to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send registration email to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendOrderConfirmationEmail(User user, Order order) {
        log.info("Starting async execution to send order confirmation email for order #{}", order.getId());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom("ramyegneswarseeram@gmail.com", "FoodApp");
            helper.setTo(user.getEmail());
            helper.setSubject("Order #" + order.getId() + " Confirmed!");

            StringBuilder itemsHtml = new StringBuilder();
            itemsHtml.append("<table style=\"width: 100%; border-collapse: collapse; margin-top: 20px; font-size: 14px;\">")
                    .append("<thead>")
                    .append("  <tr style=\"border-bottom: 2px solid #eef2f6; text-align: left; color: #888;\">")
                    .append("    <th style=\"padding: 10px 0;\">Item</th>")
                    .append("    <th style=\"padding: 10px 0; text-align: center;\">Qty</th>")
                    .append("    <th style=\"padding: 10px 0; text-align: right;\">Price</th>")
                    .append("  </tr>")
                    .append("</thead>")
                    .append("<tbody>");

            for (OrderItem item : order.getOrderItems()) {
                double total = item.getPrice() * item.getQuantity();
                itemsHtml.append("  <tr style=\"border-bottom: 1px solid #f2f5f9;\">")
                        .append("    <td style=\"padding: 12px 0; font-weight: 500;\">").append(item.getMenuItem().getName()).append("</td>")
                        .append("    <td style=\"padding: 12px 0; text-align: center; color: #555;\">").append(item.getQuantity()).append("</td>")
                        .append("    <td style=\"padding: 12px 0; text-align: right; font-weight: 600;\">$").append(String.format("%.2f", total)).append("</td>")
                        .append("  </tr>");
            }
            itemsHtml.append("</tbody></table>");

            String htmlContent = "<div style=\"font-family: 'Outfit', 'Inter', sans-serif; padding: 20px; background-color: #f7f9fc; color: #333;\">" +
                    "  <div style=\"max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border: 1px solid #eef2f6;\">" +
                    "    <div style=\"background: linear-gradient(135deg, #11998e, #38ef7d); padding: 30px; text-align: center; color: white;\">" +
                    "      <h1 style=\"margin: 0; font-size: 26px; font-weight: 700;\">Order Confirmed!</h1>" +
                    "      <p style=\"margin: 5px 0 0 0; font-size: 16px; opacity: 0.9;\">Order ID: #" + order.getId() + "</p>" +
                    "    </div>" +
                    "    <div style=\"padding: 30px;\">" +
                    "      <p style=\"font-size: 16px; margin-top: 0;\">Hi <strong>" + user.getName() + "</strong>,</p>" +
                    "      <p style=\"font-size: 15px; color: #555;\">Your order has been successfully placed at <strong>" + order.getRestaurant().getName() + "</strong> and is now being prepared.</p>" +
                    "      " + itemsHtml.toString() +
                    "      <div style=\"margin-top: 20px; padding: 15px; background-color: #f8fafc; border-radius: 8px; border: 1px dashed #e2e8f0;\">" +
                    "        <div style=\"display: flex; justify-content: space-between; font-size: 16px; font-weight: 700; margin-bottom: 10px;\">" +
                    "          <span>Total Amount:</span>" +
                    "          <span style=\"color: #11998e;\">$" + String.format("%.2f", order.getTotalAmount()) + "</span>" +
                    "        </div>" +
                    "        <div style=\"display: flex; justify-content: space-between; font-size: 14px; color: #555;\">" +
                    "          <span>Estimated Delivery Time:</span>" +
                    "          <span style=\"font-weight: 600;\">30-45 mins</span>" +
                    "        </div>" +
                    "      </div>" +
                    "      <p style=\"font-size: 14px; color: #888; border-top: 1px solid #eee; padding-top: 20px; margin-top: 30px; margin-bottom: 0; text-align: center;\">Thank you for dining with FoodApp!</p>" +
                    "    </div>" +
                    "  </div>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Order confirmation email sent successfully for order #{}", order.getId());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order #{}: {}", order.getId(), e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void sendOrderCancellationEmail(User user, Order order) {
        log.info("Starting async execution to send order cancellation email for order #{}", order.getId());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom("ramyegneswarseeram@gmail.com", "FoodApp");
            helper.setTo(user.getEmail());
            helper.setSubject("Order #" + order.getId() + " Cancelled");

            String htmlContent = "<div style=\"font-family: 'Outfit', 'Inter', sans-serif; padding: 20px; background-color: #f7f9fc; color: #333;\">" +
                    "  <div style=\"max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border: 1px solid #eef2f6;\">" +
                    "    <div style=\"background: linear-gradient(135deg, #e53935, #e35d5b); padding: 30px; text-align: center; color: white;\">" +
                    "      <h1 style=\"margin: 0; font-size: 26px; font-weight: 700;\">Order Cancelled</h1>" +
                    "      <p style=\"margin: 5px 0 0 0; font-size: 16px; opacity: 0.9;\">Order ID: #" + order.getId() + "</p>" +
                    "    </div>" +
                    "    <div style=\"padding: 30px;\">" +
                    "      <p style=\"font-size: 16px; margin-top: 0;\">Hi <strong>" + user.getName() + "</strong>,</p>" +
                    "      <p style=\"font-size: 15px; color: #555;\">We confirm that your order #<strong>" + order.getId() + "</strong> has been successfully cancelled as requested.</p>" +
                    "      <div style=\"margin-top: 25px; padding: 20px; background-color: #fff5f5; border-radius: 8px; border: 1px solid #fed7d7; color: #9b2c2c;\">" +
                    "        <h3 style=\"margin-top: 0; font-size: 15px; font-weight: 700;\">Refund Information</h3>" +
                    "        <p style=\"margin: 5px 0 0 0; font-size: 14px; line-height: 1.5;\">A full refund of <strong>$" + String.format("%.2f", order.getTotalAmount()) + "</strong> has been initiated to your original payment method. You should see it in your account within <strong>3-5 business days</strong>.</p>" +
                    "      </div>" +
                    "      <p style=\"font-size: 14px; color: #888; border-top: 1px solid #eee; padding-top: 20px; margin-top: 30px; margin-bottom: 0; text-align: center;\">If you have any questions, please contact our support team.</p>" +
                    "    </div>" +
                    "  </div>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Order cancellation email sent successfully for order #{}", order.getId());
        } catch (Exception e) {
            log.error("Failed to send order cancellation email for order #{}: {}", order.getId(), e.getMessage(), e);
        }
    }
}
