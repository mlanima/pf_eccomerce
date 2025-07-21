package com.horseriding.ecommerce.orders.dtos.responses;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for order line item responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long id;
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    // Product information at the time of order
    private String productName;
    private String productSku;
    private String productBrand;
    private String productModel;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Utility methods
     */
    public String getProductDisplayName() {
        StringBuilder displayName = new StringBuilder(productName);
        if (productBrand != null && !productBrand.trim().isEmpty()) {
            displayName.append(" (").append(productBrand);
            if (productModel != null && !productModel.trim().isEmpty()) {
                displayName.append(" ").append(productModel);
            }
            displayName.append(")");
        }
        return displayName.toString();
    }
}