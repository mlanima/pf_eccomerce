package com.horseriding.ecommerce.products.dtos.responses;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for product search results.
 * Contains minimal information needed for search results display.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Long categoryId;
    private String categoryName;
    private String mainImageUrl;
    private String brand;
    private String sku;

    /**
     * Utility methods for stock status
     */
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean isOutOfStock() {
        return stockQuantity == null || stockQuantity <= 0;
    }
}
