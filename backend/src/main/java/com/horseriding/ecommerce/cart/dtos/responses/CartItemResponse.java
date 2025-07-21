package com.horseriding.ecommerce.cart.dtos.responses;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for cart item responses with product information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String productBrand;
    private String productModel;
    private String mainImageUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private Integer availableStock;
    private boolean productActive;
    private boolean inStock;
    private boolean quantityAvailable;
    private String validationMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Utility methods
     */
    public boolean isValid() {
        return productActive && inStock && quantityAvailable;
    }

    public String getProductDisplayName() {
        StringBuilder displayName = new StringBuilder(productName != null ? productName : "");
        if (productBrand != null && !productBrand.trim().isEmpty()) {
            displayName.append(" (").append(productBrand);
            if (productModel != null && !productModel.trim().isEmpty()) {
                displayName.append(" ").append(productModel);
            }
            displayName.append(")");
        }
        return displayName.toString();
    }

    public boolean hasValidationIssues() {
        return validationMessage != null && !validationMessage.trim().isEmpty();
    }
}