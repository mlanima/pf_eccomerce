package com.horseriding.ecommerce.orders.dtos.responses;

import com.horseriding.ecommerce.orders.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for customer order history responses.
 * Optimized for order history listings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryResponse {

    private Long id;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String trackingNumber;
    private String carrier;
    private int totalItemCount;
    private LocalDateTime createdAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    /**
     * Utility methods
     */
    public boolean isPaid() {
        return status == OrderStatus.PAID || status == OrderStatus.PROCESSING || 
               status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED;
    }

    public boolean isCompleted() {
        return status == OrderStatus.DELIVERED;
    }

    public boolean isCancellable() {
        return status == OrderStatus.PENDING || status == OrderStatus.PAID;
    }

    public boolean hasTracking() {
        return trackingNumber != null && !trackingNumber.trim().isEmpty();
    }
}