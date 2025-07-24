package com.horseriding.ecommerce.cart;

import com.horseriding.ecommerce.products.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * CartItem entity representing individual items within a shopping cart.
 * Contains product information, quantity, and calculated pricing.
 */
@Entity
@Table(
        name = "cart_items",
        indexes = {
            @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
            @Index(name = "idx_cart_item_product", columnList = "product_id"),
            @Index(
                    name = "idx_cart_item_cart_product",
                    columnList = "cart_id, product_id",
                    unique = true)
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"cart", "product"})
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @NotNull(message = "Cart is required")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    @Column(nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 999, message = "Quantity cannot exceed 999")
    private Integer quantity;

    @Transient private String validationMessage;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor for creating new cart items
    public CartItem(Cart cart, Product product, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
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
    public BigDecimal getUnitPrice() {
        return product != null ? product.getPrice() : BigDecimal.ZERO;
    }

    public BigDecimal getTotalPrice() {
        if (product != null && product.getPrice() != null && quantity != null) {
            return product.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    public String getProductName() {
        return product != null ? product.getName() : null;
    }

    public String getProductSku() {
        return product != null ? product.getSku() : null;
    }

    public String getProductBrand() {
        return product != null && product.getBrand() != null ? product.getBrand().getName() : null;
    }

    public String getMainImageUrl() {
        return product != null ? product.getMainImageUrl() : null;
    }

    public boolean isProductInStock() {
        return product != null && product.isInStock();
    }

    public boolean isQuantityAvailable() {
        return product != null
                && product.getStockQuantity() != null
                && quantity != null
                && product.getStockQuantity() >= quantity;
    }

    public boolean isProductActive() {
        return product != null;
    }

    public boolean isValid() {
        return isProductActive() && isProductInStock() && isQuantityAvailable();
    }

    public String getValidationMessage() {
        if (product == null) {
            return "Product not found";
        }
        if (!product.isInStock()) {
            return "Product is out of stock";
        }
        if (!isQuantityAvailable()) {
            return "Requested quantity ("
                    + quantity
                    + ") exceeds available stock ("
                    + product.getStockQuantity()
                    + ")";
        }
        return null;
    }

    public void increaseQuantity(int amount) {
        if (amount > 0) {
            this.quantity = (this.quantity != null ? this.quantity : 0) + amount;
        }
    }

    public void decreaseQuantity(int amount) {
        if (amount > 0 && this.quantity != null) {
            this.quantity = Math.max(1, this.quantity - amount);
        }
    }
}
