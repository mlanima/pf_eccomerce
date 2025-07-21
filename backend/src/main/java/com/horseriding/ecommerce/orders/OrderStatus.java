package com.horseriding.ecommerce.orders;

/**
 * Enumeration defining the different order statuses in the system.
 */
public enum OrderStatus {
    PENDING,        // Order created but payment not yet processed
    PAID,           // Payment confirmed via PayPal
    PROCESSING,     // Order is being prepared for shipment
    SHIPPED,        // Order has been shipped
    DELIVERED,      // Order has been delivered to customer
    CANCELLED,      // Order has been cancelled
    REFUNDED        // Order has been refunded
}