package com.horseriding.ecommerce.brands;

import com.horseriding.ecommerce.products.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Brand entity representing equipment manufacturers and brands.
 * Contains brand information and relationships to products.
 */
@Entity
@Table(name = "brands", indexes = {
    @Index(name = "idx_brand_name", columnList = "name"),
    @Index(name = "idx_brand_active", columnList = "active")
})
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"products"})
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Brand name is required")
    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    private String name;

    @Column(length = 1000)
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Column(name = "logo_url")
    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    @Column(name = "website_url")
    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    private String websiteUrl;

    @Column(name = "country_of_origin")
    @Size(max = 100, message = "Country of origin must not exceed 100 characters")
    private String countryOfOrigin;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor for creating new brands
    public Brand(String name) {
        this.name = name;
        this.active = true;
    }

    public Brand(String name, String description) {
        this.name = name;
        this.description = description;
        this.active = true;
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
    public int getProductCount() {
        return products != null ? products.size() : 0;
    }

    public boolean hasProducts() {
        return products != null && !products.isEmpty();
    }
}