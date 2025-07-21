package com.horseriding.ecommerce.brands.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing brand.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandUpdateRequest {
    
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
    
    private boolean active = true;
}