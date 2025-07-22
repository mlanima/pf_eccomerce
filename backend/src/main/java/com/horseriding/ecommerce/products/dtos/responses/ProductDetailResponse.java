package com.horseriding.ecommerce.products.dtos.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for detailed product information responses.
 * Includes full specifications and all product details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private Long categoryId;
    private String categoryName;
    private List<String> imageUrls;
    private Map<String, String> specifications;
    private boolean active;
    private boolean featured;
    private BigDecimal weightKg;
    private String dimensions;
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

    public boolean isLowStock() {
        return stockQuantity != null
                && lowStockThreshold != null
                && stockQuantity <= lowStockThreshold
                && stockQuantity > 0;
    }

    public boolean isOutOfStock() {
        return stockQuantity == null || stockQuantity <= 0;
    }

    public String getMainImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }

    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
    }

    public boolean hasSpecifications() {
        return specifications != null && !specifications.isEmpty();
    }
}
