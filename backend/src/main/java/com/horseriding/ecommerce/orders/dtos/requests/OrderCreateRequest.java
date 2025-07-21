package com.horseriding.ecommerce.orders.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for order creation requests during order placement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "Order items are required")
    private List<OrderItemCreateRequest> items;

    // Shipping address fields (populated by PayPal)
    @NotBlank(message = "Shipping name is required")
    @Size(max = 200, message = "Shipping name must not exceed 200 characters")
    private String shippingName;

    @NotBlank(message = "Shipping address line 1 is required")
    @Size(max = 255, message = "Shipping address line 1 must not exceed 255 characters")
    private String shippingAddressLine1;

    @Size(max = 255, message = "Shipping address line 2 must not exceed 255 characters")
    private String shippingAddressLine2;

    @NotBlank(message = "Shipping city is required")
    @Size(max = 100, message = "Shipping city must not exceed 100 characters")
    private String shippingCity;

    @Size(max = 100, message = "Shipping state must not exceed 100 characters")
    private String shippingState;

    @NotBlank(message = "Shipping postal code is required")
    @Size(max = 20, message = "Shipping postal code must not exceed 20 characters")
    private String shippingPostalCode;

    @NotBlank(message = "Shipping country is required")
    @Size(max = 100, message = "Shipping country must not exceed 100 characters")
    private String shippingCountry;

    @Size(max = 20, message = "Shipping phone must not exceed 20 characters")
    private String shippingPhone;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Total amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal totalAmount;

    @NotNull(message = "Subtotal amount is required")
    @DecimalMin(value = "0.01", message = "Subtotal amount must be greater than 0")
    private BigDecimal subtotalAmount;

    @DecimalMin(value = "0.00", message = "Shipping amount cannot be negative")
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Tax amount cannot be negative")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    // PayPal integration fields
    @Size(max = 100, message = "PayPal payment ID must not exceed 100 characters")
    private String paypalPaymentId;

    @Size(max = 100, message = "PayPal payer ID must not exceed 100 characters")
    private String paypalPayerId;

    @Size(max = 100, message = "PayPal order ID must not exceed 100 characters")
    private String paypalOrderId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemCreateRequest {
        
        @NotNull(message = "Product ID is required")
        private Long productId;

        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 999, message = "Quantity cannot exceed 999")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
        private BigDecimal unitPrice;
    }
}