package com.horseriding.ecommerce.orders.dtos.requests;

import com.horseriding.ecommerce.orders.OrderStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for order update requests for admin status updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateRequest {

    private OrderStatus status;

    @Size(max = 100, message = "Tracking number must not exceed 100 characters")
    private String trackingNumber;

    @Size(max = 50, message = "Carrier must not exceed 50 characters")
    private String carrier;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
