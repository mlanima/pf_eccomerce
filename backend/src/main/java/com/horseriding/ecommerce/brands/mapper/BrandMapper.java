package com.horseriding.ecommerce.brands.mapper;

import com.horseriding.ecommerce.brands.Brand;
import com.horseriding.ecommerce.brands.dto.BrandResponse;
import com.horseriding.ecommerce.brands.dto.CreateBrandRequest;
import com.horseriding.ecommerce.brands.dto.UpdateBrandRequest;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between Brand entities and DTOs.
 */
@Component
public class BrandMapper {

    /**
     * Convert a Brand entity to a BrandResponse DTO.
     *
     * @param brand The Brand entity to convert
     * @return The BrandResponse DTO
     */
    public BrandResponse toResponse(Brand brand) {
        if (brand == null) {
            return null;
        }

        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .websiteUrl(brand.getWebsiteUrl())
                .countryOfOrigin(brand.getCountryOfOrigin())
                .productCount(brand.getProductCount())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }

    /**
     * Convert a list of Brand entities to a list of BrandResponse DTOs.
     *
     * @param brands The list of Brand entities to convert
     * @return The list of BrandResponse DTOs
     */
    public List<BrandResponse> toResponseList(List<Brand> brands) {
        if (brands == null) {
            return List.of();
        }

        return brands.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Convert a CreateBrandRequest DTO to a Brand entity.
     *
     * @param request The CreateBrandRequest DTO to convert
     * @return The Brand entity
     */
    public Brand toEntity(CreateBrandRequest request) {
        if (request == null) {
            return null;
        }

        Brand brand = new Brand();
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setWebsiteUrl(request.getWebsiteUrl());
        brand.setCountryOfOrigin(request.getCountryOfOrigin());

        return brand;
    }

    /**
     * Update a Brand entity with values from an UpdateBrandRequest DTO.
     *
     * @param brand The Brand entity to update
     * @param request The UpdateBrandRequest DTO with new values
     * @return The updated Brand entity
     */
    public Brand updateEntityFromRequest(Brand brand, UpdateBrandRequest request) {
        if (brand == null || request == null) {
            return brand;
        }

        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setWebsiteUrl(request.getWebsiteUrl());
        brand.setCountryOfOrigin(request.getCountryOfOrigin());

        return brand;
    }
}
