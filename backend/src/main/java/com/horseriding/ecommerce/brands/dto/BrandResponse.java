package com.horseriding.ecommerce.brands.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning brand information in API responses.
 * Separates the internal entity model from the API contract.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponse {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private String websiteUrl;
    private String countryOfOrigin;
    private int productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}