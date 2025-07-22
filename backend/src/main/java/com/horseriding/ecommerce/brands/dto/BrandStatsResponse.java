package com.horseriding.ecommerce.brands.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for brand statistics response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandStatsResponse {
    private long activeBrandCount;
}
