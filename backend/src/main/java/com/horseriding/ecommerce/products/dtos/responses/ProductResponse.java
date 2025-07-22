package com.horseriding.ecommerce.products.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for public product information responses.
 * Used for product listings and basic product information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Long categoryId;
    private String categoryName;
    private List<String> imageUrls;
    private boolean active;
    private boolean featured;
    private String brand;
    private String model;
    private String sku;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Utility methods for stock status
     */
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean isOutOfStock() {
        return stockQuantity == null || stockQuantity <= 0;
    }

    public String getMainImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }
}
