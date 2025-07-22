package com.horseriding.ecommerce.cart.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for cart responses with cart items and totals.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private Long id;
    private Long userId;
    private List<CartItemResponse> items;
    private int totalItemCount;
    private int uniqueItemCount;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Utility methods
     */
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public boolean hasValidItems() {
        return items != null && items.stream().anyMatch(CartItemResponse::isValid);
    }

    public boolean hasInvalidItems() {
        return items != null && items.stream().anyMatch(item -> !item.isValid());
    }

    public BigDecimal getValidItemsTotal() {
        if (items == null) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .filter(CartItemResponse::isValid)
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
