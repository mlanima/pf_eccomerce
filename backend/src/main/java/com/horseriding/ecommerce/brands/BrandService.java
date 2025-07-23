package com.horseriding.ecommerce.brands;

import com.horseriding.ecommerce.exception.ResourceNotFoundException;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for Brand entity operations.
 * Contains business logic for brand management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BrandService {

    /** Repository for brand data access. */
    private final BrandRepository brandRepository;

    /**
     * Get all brands.
     *
     * @return list of all brands
     */
    public List<Brand> getAllBrands() {
        log.debug("Fetching all brands");
        return brandRepository.findAll();
    }

    /**
     * Get all brands with pagination.
     *
     * @param pageable pagination information
     * @return page of brands
     */
    public Page<Brand> getAllBrands(final Pageable pageable) {
        log.debug("Fetching brands with pagination: {}", pageable);
        return brandRepository.findAll(pageable);
    }

    /**
     * Get brand by ID.
     *
     * @param id brand ID
     * @return brand with the specified ID
     * @throws ResourceNotFoundException if brand not found
     */
    public Brand getBrandById(final Long id) {
        log.debug("Fetching brand by ID: {}", id);
        return brandRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + id));
    }

    /**
     * Get brand by name (case-insensitive).
     *
     * @param name brand name
     * @return brand with the specified name
     * @throws ResourceNotFoundException if brand not found
     */
    public Brand getBrandByName(final String name) {
        log.debug("Fetching brand by name: {}", name);
        return brandRepository
                .findByNameIgnoreCase(name)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Brand not found with name: " + name));
    }

    /**
     * Search brands by name.
     *
     * @param name brand name to search for
     * @param pageable pagination information
     * @return page of brands matching the search criteria
     */
    public Page<Brand> searchBrandsByName(final String name, final Pageable pageable) {
        log.debug("Searching brands by name: {} with pagination: {}", name, pageable);
        return brandRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    /**
     * Get brands by country of origin.
     *
     * @param countryOfOrigin country of origin
     * @return list of brands from the specified country
     */
    public List<Brand> getBrandsByCountry(final String countryOfOrigin) {
        log.debug("Fetching brands by country: {}", countryOfOrigin);
        return brandRepository.findByCountryOfOriginIgnoreCase(countryOfOrigin);
    }

    /**
     * Create a new brand.
     *
     * @param brand brand to create
     * @return created brand
     * @throws IllegalArgumentException if brand name already exists
     */
    @Transactional
    public Brand createBrand(final Brand brand) {
        log.debug("Creating new brand: {}", brand.getName());

        // Check if brand name already exists
        if (brandRepository.existsByNameIgnoreCase(brand.getName())) {
            throw new IllegalArgumentException(
                    "Brand with name '" + brand.getName() + "' already exists");
        }

        Brand savedBrand = brandRepository.save(brand);
        log.info("Created brand with ID: {}", savedBrand.getId());
        return savedBrand;
    }

    /**
     * Update an existing brand.
     *
     * @param id brand ID
     * @param brandDetails updated brand details
     * @return updated brand
     * @throws ResourceNotFoundException if brand not found
     * @throws IllegalArgumentException if new name conflicts with existing brand
     */
    @Transactional
    public Brand updateBrand(final Long id, final Brand brandDetails) {
        log.debug("Updating brand with ID: {}", id);

        Brand existingBrand = getBrandById(id);

        // Check if new name conflicts with existing brand (excluding current brand)
        if (!existingBrand.getName().equalsIgnoreCase(brandDetails.getName())
                && brandRepository.existsByNameIgnoreCase(brandDetails.getName())) {
            throw new IllegalArgumentException(
                    "Brand with name '" + brandDetails.getName() + "' already exists");
        }

        // Update fields
        existingBrand.setName(brandDetails.getName());
        existingBrand.setDescription(brandDetails.getDescription());
        existingBrand.setLogoUrl(brandDetails.getLogoUrl());
        existingBrand.setWebsiteUrl(brandDetails.getWebsiteUrl());
        existingBrand.setCountryOfOrigin(brandDetails.getCountryOfOrigin());

        Brand updatedBrand = brandRepository.save(existingBrand);
        log.info("Updated brand with ID: {}", updatedBrand.getId());
        return updatedBrand;
    }

    /**
     * Delete a brand.
     *
     * @param id brand ID
     * @throws ResourceNotFoundException if brand not found
     * @throws IllegalStateException if brand has associated products
     */
    @Transactional
    public void deleteBrand(final Long id) {
        log.debug("Deleting brand with ID: {}", id);

        Brand brand = getBrandById(id);

        // Check if brand has products
        if (brand.hasProducts()) {
            log.warn(
                    "Attempting to delete brand with products. Brand ID: {}, Product count: {}",
                    id,
                    brand.getProductCount());
            throw new IllegalStateException(
                    "Cannot delete brand that has associated products. "
                            + "Please reassign or remove products first.");
        }

        brandRepository.delete(brand);
        log.info("Deleted brand with ID: {}", id);
    }

    /**
     * Get total count of brands.
     *
     * @return total number of brands
     */
    public long getBrandCount() {
        return brandRepository.count();
    }

    /**
     * Search brands by name with pagination.
     * Used for brand search functionality.
     *
     * @param query search query
     * @param pageable pagination information
     * @return page of brands matching the search criteria
     */
    public Page<Brand> searchBrands(final String query, final Pageable pageable) {
        log.debug("Searching brands by query: {} with pagination: {}", query, pageable);
        return brandRepository.findByNameContainingIgnoreCase(query, pageable);
    }

    /**
     * Get brands ordered by product count.
     * Used for popular brands display.
     *
     * @return list of brands ordered by product count
     */
    public List<Brand> getBrandsOrderedByProductCount() {
        log.debug("Fetching brands ordered by product count");
        List<Brand> brands = brandRepository.findAll();
        brands.sort(Comparator.comparing(Brand::getProductCount).reversed());
        return brands;
    }

    /**
     * Check if a brand name exists.
     * Used for brand creation validation.
     *
     * @param name brand name to check
     * @return true if brand name exists, false otherwise
     */
    public boolean brandNameExists(final String name) {
        log.debug("Checking if brand name exists: {}", name);
        return brandRepository.existsByNameIgnoreCase(name);
    }
}
