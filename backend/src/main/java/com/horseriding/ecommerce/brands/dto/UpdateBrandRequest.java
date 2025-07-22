package com.horseriding.ecommerce.brands.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing brand.
 * Contains validation rules for brand updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBrandRequest {
    @NotBlank(message = "Brand name is required")
    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    private String websiteUrl;

    @Size(max = 100, message = "Country of origin must not exceed 100 characters")
    private String countryOfOrigin;
}