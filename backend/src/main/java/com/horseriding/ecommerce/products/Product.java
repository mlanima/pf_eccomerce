package com.horseriding.ecommerce.products;

import com.horseriding.ecommerce.brands.Brand;
import com.horseriding.ecommerce.categories.Category;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Product entity representing horse riding equipment and accessories.
 * Contains product information, pricing, inventory, and category relationships.
 */
@Entity
@Table(
        name = "products",
        indexes = {
            @Index(name = "idx_product_name", columnList = "name"),
            @Index(name = "idx_product_category", columnList = "category_id"),
            @Index(name = "idx_product_brand", columnList = "brand_id"),
            @Index(name = "idx_product_price", columnList = "price"),
            @Index(name = "idx_product_stock", columnList = "stock_quantity")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"category", "brand", "imageUrls", "specifications"})
public class Product {

    /** Maximum length for product name. */
    private static final int MAX_NAME_LENGTH = 200;

    /** Maximum length for product description. */
    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    /** Maximum integer digits for price. */
    private static final int PRICE_INTEGER_DIGITS = 8;

    /** Maximum fraction digits for price. */
    private static final int PRICE_FRACTION_DIGITS = 2;

    /** Default low stock threshold. */
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    /** Maximum length for dimensions. */
    private static final int MAX_DIMENSIONS_LENGTH = 100;

    /** Maximum length for model. */
    private static final int MAX_MODEL_LENGTH = 100;

    /** Maximum length for SKU. */
    private static final int MAX_SKU_LENGTH = 50;

    /** Weight precision. */
    private static final int WEIGHT_PRECISION = 8;

    /** Weight scale. */
    private static final int WEIGHT_SCALE = 3;

    /** Unique identifier for the product. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Product name. */
    @Column(nullable = false)
    @NotBlank(message = "Product name is required")
    @Size(max = MAX_NAME_LENGTH, message = "Product name must not exceed 200 characters")
    private String name;

    /** Product description. */
    @Column(length = MAX_DESCRIPTION_LENGTH)
    @Size(max = MAX_DESCRIPTION_LENGTH, message = "Description must not exceed 2000 characters")
    private String description;

    /** Product price. */
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(
            integer = PRICE_INTEGER_DIGITS,
            fraction = PRICE_FRACTION_DIGITS,
            message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    /** Current stock quantity. */
    @Column(name = "stock_quantity", nullable = false)
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity = 0;

    /** Threshold for low stock alerts. */
    @Column(name = "low_stock_threshold")
    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold = DEFAULT_LOW_STOCK_THRESHOLD;

    /** Product category. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Category is required")
    private Category category;

    /** Product images. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    /** Product specifications. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "product_specifications",
            joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "spec_name")
    @Column(name = "spec_value")
    private Map<String, String> specifications = new HashMap<>();

    /** Whether the product is featured. */
    @Column(name = "featured")
    private boolean featured = false;

    /** Product weight in kilograms. */
    @Column(name = "weight_kg", precision = WEIGHT_PRECISION, scale = WEIGHT_SCALE)
    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    private BigDecimal weightKg;

    /** Product dimensions. */
    @Column(name = "dimensions")
    @Size(max = MAX_DIMENSIONS_LENGTH, message = "Dimensions must not exceed 100 characters")
    private String dimensions;

    /** Product brand. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    /** Product model. */
    @Column(name = "model")
    @Size(max = MAX_MODEL_LENGTH, message = "Model must not exceed 100 characters")
    private String model;

    /** Product SKU (Stock Keeping Unit). */
    @Column(name = "sku", unique = true)
    @Size(max = MAX_SKU_LENGTH, message = "SKU must not exceed 50 characters")
    private String sku;

    /** Timestamp when the product was created. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the product was last updated. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating new products.
     *
     * @param name the product name
     * @param description the product description
     * @param price the product price
     * @param category the product category
     */
    public Product(
            final String name,
            final String description,
            final BigDecimal price,
            final Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stockQuantity = 0;
    }

    /**
     * JPA lifecycle callback executed before persisting the entity.
     * Sets the creation and update timestamps.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback executed before updating the entity.
     * Updates the last modified timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if the product is in stock.
     *
     * @return true if the product is in stock, false otherwise
     */
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    /**
     * Checks if the product stock is low.
     *
     * @return true if the product stock is low, false otherwise
     */
    public boolean isLowStock() {
        return stockQuantity != null
                && lowStockThreshold != null
                && stockQuantity <= lowStockThreshold
                && stockQuantity > 0;
    }

    /**
     * Checks if the product is out of stock.
     *
     * @return true if the product is out of stock, false otherwise
     */
    public boolean isOutOfStock() {
        return stockQuantity == null || stockQuantity <= 0;
    }

    /**
     * Adds an image URL to the product.
     *
     * @param imageUrl the image URL to add
     */
    public void addImage(final String imageUrl) {
        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
        }
        imageUrls.add(imageUrl);
    }

    /**
     * Removes an image URL from the product.
     *
     * @param imageUrl the image URL to remove
     */
    public void removeImage(final String imageUrl) {
        if (imageUrls != null) {
            imageUrls.remove(imageUrl);
        }
    }

    /**
     * Adds a specification to the product.
     *
     * @param name the specification name
     * @param value the specification value
     */
    public void addSpecification(final String name, final String value) {
        if (specifications == null) {
            specifications = new HashMap<>();
        }
        specifications.put(name, value);
    }

    /**
     * Removes a specification from the product.
     *
     * @param name the specification name to remove
     */
    public void removeSpecification(final String name) {
        if (specifications != null) {
            specifications.remove(name);
        }
    }

    /**
     * Gets the main image URL of the product.
     *
     * @return the main image URL, or null if no images exist
     */
    public String getMainImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }

    /**
     * Checks if the product has images.
     *
     * @return true if the product has images, false otherwise
     */
    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
    }

    /**
     * Checks if the product has specifications.
     *
     * @return true if the product has specifications, false otherwise
     */
    public boolean hasSpecifications() {
        return specifications != null && !specifications.isEmpty();
    }
}
