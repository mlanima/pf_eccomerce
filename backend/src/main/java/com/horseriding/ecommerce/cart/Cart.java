package com.horseriding.ecommerce.cart;

import com.horseriding.ecommerce.users.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Cart entity representing a user's shopping cart.
 * Contains cart items and provides methods for cart management.
 */
@Entity
@Table(
        name = "carts",
        indexes = {
            @Index(name = "idx_cart_user", columnList = "user_id", unique = true),
            @Index(name = "idx_cart_updated_at", columnList = "updated_at")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user", "items"})
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @NotNull(message = "User is required")
    private User user;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "session_id")
    private String sessionId; // For guest users (future enhancement)

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor for creating new carts
    public Cart(User user) {
        this.user = user;
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
    public void addItem(CartItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }

        // Check if item with same product already exists
        CartItem existingItem = findItemByProductId(item.getProduct().getId());
        if (existingItem != null) {
            // Update quantity of existing item
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
        } else {
            // Add new item
            items.add(item);
            item.setCart(this);
        }
    }

    public void removeItem(CartItem item) {
        if (items != null) {
            items.remove(item);
            item.setCart(null);
        }
    }

    public void removeItemByProductId(Long productId) {
        if (items != null) {
            CartItem itemToRemove = findItemByProductId(productId);
            if (itemToRemove != null) {
                removeItem(itemToRemove);
            }
        }
    }

    public CartItem findItemByProductId(Long productId) {
        if (items != null && productId != null) {
            return items.stream()
                    .filter(
                            item ->
                                    item.getProduct() != null
                                            && productId.equals(item.getProduct().getId()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public void updateItemQuantity(Long productId, Integer newQuantity) {
        CartItem item = findItemByProductId(productId);
        if (item != null) {
            if (newQuantity <= 0) {
                removeItem(item);
            } else {
                item.setQuantity(newQuantity);
            }
        }
    }

    public void clearCart() {
        if (items != null) {
            items.clear();
        }
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public int getTotalItemCount() {
        if (items == null) {
            return 0;
        }
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public int getUniqueItemCount() {
        return items != null ? items.size() : 0;
    }

    public BigDecimal getTotalAmount() {
        if (items == null) {
            return BigDecimal.ZERO;
        }
        return items.stream().map(CartItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean hasItem(Long productId) {
        return findItemByProductId(productId) != null;
    }

    public boolean isExpired(int maxAgeInDays) {
        if (updatedAt == null) {
            return false;
        }
        return updatedAt.isBefore(LocalDateTime.now().minusDays(maxAgeInDays));
    }
}
