package com.horseriding.ecommerce.orders;

import com.horseriding.ecommerce.products.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderItem entity representing individual items within an order.
 * Contains product information, quantity, and pricing at the time of order.
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order", columnList = "order_id"),
    @Index(name = "idx_order_item_product", columnList = "product_id")
})
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"order", "product"})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Order is required")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    @Column(nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 999, message = "Quantity cannot exceed 999")
    private Integer quantity;

    // Price at the time of order (to preserve historical pricing)
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Unit price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.01", message = "Total price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Total price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal totalPrice;

    // Product information at the time of order (for historical reference)
    @Column(name = "product_name", nullable = false)
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String productName;

    @Column(name = "product_sku")
    @Size(max = 50, message = "Product SKU must not exceed 50 characters")
    private String productSku;

    @Column(name = "product_brand")
    @Size(max = 100, message = "Product brand must not exceed 100 characters")
    private String productBrand;

    @Column(name = "product_model")
    @Size(max = 100, message = "Product model must not exceed 100 characters")
    private String productModel;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor for creating new order items
    public OrderItem(Order order, Product product, Integer quantity, BigDecimal unitPrice) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        
        // Copy product information for historical reference
        if (product != null) {
            this.productName = product.getName();
            this.productSku = product.getSku();
            this.productBrand = product.getBrand();
            this.productModel = product.getModel();
        }
    }

    // JPA lifecycle callbacks for audit fields
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Calculate total price if not set
        if (totalPrice == null && unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        
        // Recalculate total price if quantity or unit price changed
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // Custom setters with business logic
    public void setProduct(Product product) {
        this.product = product;
        
        // Update product information when product is set
        if (product != null) {
            this.productName = product.getName();
            this.productSku = product.getSku();
            this.productBrand = product.getBrand();
            this.productModel = product.getModel();
        }
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        
        // Recalculate total price when quantity changes
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        
        // Recalculate total price when unit price changes
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // Utility methods
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

    public BigDecimal calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
}