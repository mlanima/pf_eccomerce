package com.horseriding.ecommerce.brands.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Brand entity.
 * Used for public brand information display.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponse {
    
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private String websiteUrl;
    private String countryOfOrigin;
    private boolean active;
    private int productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}