package com.horseriding.ecommerce.orders;

import com.horseriding.ecommerce.users.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity representing customer orders with PayPal payment integration.
 * Contains order information, shipping details from PayPal, and order items.
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user", columnList = "user_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_paypal_payment", columnList = "paypal_payment_id"),
    @Index(name = "idx_order_created_at", columnList = "created_at"),
    @Index(name = "idx_order_tracking", columnList = "tracking_number")
})
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user", "items"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    // Shipping address fields populated by PayPal
    @Column(name = "shipping_name", nullable = false)
    @NotBlank(message = "Shipping name is required")
    @Size(max = 200, message = "Shipping name must not exceed 200 characters")
    private String shippingName;

    @Column(name = "shipping_address_line1", nullable = false)
    @NotBlank(message = "Shipping address line 1 is required")
    @Size(max = 255, message = "Shipping address line 1 must not exceed 255 characters")
    private String shippingAddressLine1;

    @Column(name = "shipping_address_line2")
    @Size(max = 255, message = "Shipping address line 2 must not exceed 255 characters")
    private String shippingAddressLine2;

    @Column(name = "shipping_city", nullable = false)
    @NotBlank(message = "Shipping city is required")
    @Size(max = 100, message = "Shipping city must not exceed 100 characters")
    private String shippingCity;

    @Column(name = "shipping_state")
    @Size(max = 100, message = "Shipping state must not exceed 100 characters")
    private String shippingState;

    @Column(name = "shipping_postal_code", nullable = false)
    @NotBlank(message = "Shipping postal code is required")
    @Size(max = 20, message = "Shipping postal code must not exceed 20 characters")
    private String shippingPostalCode;

    @Column(name = "shipping_country", nullable = false)
    @NotBlank(message = "Shipping country is required")
    @Size(max = 100, message = "Shipping country must not exceed 100 characters")
    private String shippingCountry;

    @Column(name = "shipping_phone")
    @Size(max = 20, message = "Shipping phone must not exceed 20 characters")
    private String shippingPhone;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Total amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal totalAmount;

    @Column(name = "subtotal_amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Subtotal amount is required")
    @DecimalMin(value = "0.01", message = "Subtotal amount must be greater than 0")
    private BigDecimal subtotalAmount;

    @Column(name = "shipping_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Shipping amount cannot be negative")
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.00", message = "Tax amount cannot be negative")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Order status is required")
    private OrderStatus status = OrderStatus.PENDING;

    // PayPal integration fields
    @Column(name = "paypal_payment_id", unique = true)
    @Size(max = 100, message = "PayPal payment ID must not exceed 100 characters")
    private String paypalPaymentId;

    @Column(name = "paypal_payer_id")
    @Size(max = 100, message = "PayPal payer ID must not exceed 100 characters")
    private String paypalPayerId;

    @Column(name = "paypal_order_id")
    @Size(max = 100, message = "PayPal order ID must not exceed 100 characters")
    private String paypalOrderId;

    @Column(name = "payment_method")
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod = "PAYPAL";

    @Column(name = "tracking_number")
    @Size(max = 100, message = "Tracking number must not exceed 100 characters")
    private String trackingNumber;

    @Column(name = "carrier")
    @Size(max = 50, message = "Carrier must not exceed 50 characters")
    private String carrier;

    @Column(name = "notes", length = 1000)
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    // Constructor for creating new orders
    public Order(User user, BigDecimal totalAmount) {
        this.user = user;
        this.totalAmount = totalAmount;
        this.subtotalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
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
        
        // Set timestamps based on status changes
        if (status == OrderStatus.SHIPPED && shippedAt == null) {
            this.shippedAt = LocalDateTime.now();
        }
        if (status == OrderStatus.DELIVERED && deliveredAt == null) {
            this.deliveredAt = LocalDateTime.now();
        }
    }

    // Utility methods
    public void addItem(OrderItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        if (items != null) {
            items.remove(item);
            item.setOrder(null);
        }
    }

    public int getTotalItemCount() {
        return items != null ? items.stream().mapToInt(OrderItem::getQuantity).sum() : 0;
    }

    public String getFullShippingAddress() {
        StringBuilder address = new StringBuilder();
        address.append(shippingAddressLine1);
        if (shippingAddressLine2 != null && !shippingAddressLine2.trim().isEmpty()) {
            address.append(", ").append(shippingAddressLine2);
        }
        address.append(", ").append(shippingCity);
        if (shippingState != null && !shippingState.trim().isEmpty()) {
            address.append(", ").append(shippingState);
        }
        address.append(" ").append(shippingPostalCode);
        address.append(", ").append(shippingCountry);
        return address.toString();
    }

    public boolean isPaid() {
        return status == OrderStatus.PAID || status == OrderStatus.PROCESSING || 
               status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED;
    }

    public boolean isShippable() {
        return status == OrderStatus.PAID || status == OrderStatus.PROCESSING;
    }

    public boolean isCompleted() {
        return status == OrderStatus.DELIVERED;
    }

    public boolean isCancellable() {
        return status == OrderStatus.PENDING || status == OrderStatus.PAID;
    }
}