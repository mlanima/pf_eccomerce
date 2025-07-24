package com.horseriding.ecommerce.products.dtos.requests;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for product creation requests with validation for admin operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(
            integer = 8,
            fraction = 2,
            message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity = 0;

    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold = 10;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private List<String> imageUrls;

    private Map<String, String> specifications;

    private boolean featured = false;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    private BigDecimal weightKg;

    @Size(max = 100, message = "Dimensions must not exceed 100 characters")
    private String dimensions;

    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;

    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;
}
