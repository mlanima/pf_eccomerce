package com.horseriding.ecommerce.products;

import com.horseriding.ecommerce.brands.Brand;
import com.horseriding.ecommerce.categories.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Product entity representing horse riding equipment and accessories.
 * Contains product information, pricing, inventory, and category relationships.
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_name", columnList = "name"),
    @Index(name = "idx_product_category", columnList = "category_id"),
    @Index(name = "idx_product_brand", columnList = "brand_id"),
    @Index(name = "idx_product_price", columnList = "price"),
    @Index(name = "idx_product_stock", columnList = "stock_quantity")
})
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"category", "brand", "imageUrls", "specifications"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    @Column(length = 2000)
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity = 0;

    @Column(name = "low_stock_threshold")
    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold = 10;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Category is required")
    private Category category;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_specifications", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "spec_name")
    @Column(name = "spec_value")
    private Map<String, String> specifications = new HashMap<>();



    @Column(name = "featured")
    private boolean featured = false;

    @Column(name = "weight_kg", precision = 8, scale = 3)
    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    private BigDecimal weightKg;

    @Column(name = "dimensions")
    @Size(max = 100, message = "Dimensions must not exceed 100 characters")
    private String dimensions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(name = "model")
    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    @Column(name = "sku", unique = true)
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor for creating new products
    public Product(String name, String description, BigDecimal price, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stockQuantity = 0;
    }

    // JPA lifecycle callbacks for audit fields
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Utility methods
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean isLowStock() {
        return stockQuantity != null && lowStockThreshold != null && 
               stockQuantity <= lowStockThreshold && stockQuantity > 0;
    }

    public boolean isOutOfStock() {
        return stockQuantity == null || stockQuantity <= 0;
    }

    public void addImage(String imageUrl) {
        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
        }
        imageUrls.add(imageUrl);
    }

    public void removeImage(String imageUrl) {
        if (imageUrls != null) {
            imageUrls.remove(imageUrl);
        }
    }

    public void addSpecification(String name, String value) {
        if (specifications == null) {
            specifications = new HashMap<>();
        }
        specifications.put(name, value);
    }

    public void removeSpecification(String name) {
        if (specifications != null) {
            specifications.remove(name);
        }
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