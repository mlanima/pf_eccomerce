package com.horseriding.ecommerce.brands;

import com.horseriding.ecommerce.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Brand entity operations.
 * Contains business logic for brand management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;

    /**
     * Get all active brands.
     */
    public List<Brand> getAllActiveBrands() {
        log.debug("Fetching all active brands");
        return brandRepository.findByActiveTrue();
    }

    /**
     * Get all active brands with pagination.
     */
    public Page<Brand> getAllActiveBrands(Pageable pageable) {
        log.debug("Fetching active brands with pagination: {}", pageable);
        return brandRepository.findByActiveTrue(pageable);
    }

    /**
     * Get brand by ID.
     */
    public Brand getBrandById(Long id) {
        log.debug("Fetching brand by ID: {}", id);
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + id));
    }

    /**
     * Get brand by name (case-insensitive).
     */
    public Brand getBrandByName(String name) {
        log.debug("Fetching brand by name: {}", name);
        return brandRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with name: " + name));
    }

    /**
     * Search brands by name.
     */
    public Page<Brand> searchBrandsByName(String name, Pageable pageable) {
        log.debug("Searching brands by name: {} with pagination: {}", name, pageable);
        return brandRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable);
    }

    /**
     * Get brands by country of origin.
     */
    public List<Brand> getBrandsByCountry(String countryOfOrigin) {
        log.debug("Fetching brands by country: {}", countryOfOrigin);
        return brandRepository.findByCountryOfOriginIgnoreCaseAndActiveTrue(countryOfOrigin);
    }

    /**
     * Get brands that have active products.
     */
    public List<Brand> getBrandsWithActiveProducts() {
        log.debug("Fetching brands with active products");
        return brandRepository.findBrandsWithActiveProducts();
    }

    /**
     * Create a new brand.
     */
    @Transactional
    public Brand createBrand(Brand brand) {
        log.debug("Creating new brand: {}", brand.getName());
        
        // Check if brand name already exists
        if (brandRepository.existsByNameIgnoreCase(brand.getName())) {
            throw new IllegalArgumentException("Brand with name '" + brand.getName() + "' already exists");
        }
        
        Brand savedBrand = brandRepository.save(brand);
        log.info("Created brand with ID: {}", savedBrand.getId());
        return savedBrand;
    }

    /**
     * Update an existing brand.
     */
    @Transactional
    public Brand updateBrand(Long id, Brand brandDetails) {
        log.debug("Updating brand with ID: {}", id);
        
        Brand existingBrand = getBrandById(id);
        
        // Check if new name conflicts with existing brand (excluding current brand)
        if (!existingBrand.getName().equalsIgnoreCase(brandDetails.getName()) &&
            brandRepository.existsByNameIgnoreCase(brandDetails.getName())) {
            throw new IllegalArgumentException("Brand with name '" + brandDetails.getName() + "' already exists");
        }
        
        // Update fields
        existingBrand.setName(brandDetails.getName());
        existingBrand.setDescription(brandDetails.getDescription());
        existingBrand.setLogoUrl(brandDetails.getLogoUrl());
        existingBrand.setWebsiteUrl(brandDetails.getWebsiteUrl());
        existingBrand.setCountryOfOrigin(brandDetails.getCountryOfOrigin());
        existingBrand.setActive(brandDetails.isActive());
        
        Brand updatedBrand = brandRepository.save(existingBrand);
        log.info("Updated brand with ID: {}", updatedBrand.getId());
        return updatedBrand;
    }

    /**
     * Soft delete a brand (set active to false).
     */
    @Transactional
    public void deleteBrand(Long id) {
        log.debug("Soft deleting brand with ID: {}", id);
        
        Brand brand = getBrandById(id);
        
        // Check if brand has products
        if (brand.hasProducts()) {
            log.warn("Attempting to delete brand with products. Brand ID: {}, Product count: {}", 
                    id, brand.getProductCount());
            throw new IllegalStateException("Cannot delete brand that has associated products. " +
                    "Please reassign or remove products first.");
        }
        
        brand.setActive(false);
        brandRepository.save(brand);
        log.info("Soft deleted brand with ID: {}", id);
    }

    /**
     * Permanently delete a brand.
     */
    @Transactional
    public void permanentlyDeleteBrand(Long id) {
        log.debug("Permanently deleting brand with ID: {}", id);
        
        Brand brand = getBrandById(id);
        
        // Check if brand has products
        if (brand.hasProducts()) {
            throw new IllegalStateException("Cannot permanently delete brand that has associated products. " +
                    "Please reassign or remove products first.");
        }
        
        brandRepository.delete(brand);
        log.info("Permanently deleted brand with ID: {}", id);
    }

    /**
     * Reactivate a soft-deleted brand.
     */
    @Transactional
    public Brand reactivateBrand(Long id) {
        log.debug("Reactivating brand with ID: {}", id);
        
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + id));
        
        brand.setActive(true);
        Brand reactivatedBrand = brandRepository.save(brand);
        log.info("Reactivated brand with ID: {}", id);
        return reactivatedBrand;
    }

    /**
     * Get total count of active brands.
     */
    public long getActiveBrandCount() {
        return brandRepository.countByActiveTrue();
    }
    
    /**
     * Search brands by name with pagination.
     * Used for brand search functionality.
     */
    public Page<Brand> searchBrands(String query, Pageable pageable) {
        log.debug("Searching brands by query: {} with pagination: {}", query, pageable);
        return brandRepository.findByNameContainingIgnoreCaseAndActiveTrue(query, pageable);
    }
    
    /**
     * Get brands ordered by product count.
     * Used for popular brands display.
     */
    public List<Brand> getBrandsOrderedByProductCount() {
        log.debug("Fetching brands ordered by product count");
        List<Brand> brands = brandRepository.findByActiveTrue();
        brands.sort(Comparator.comparing(Brand::getProductCount).reversed());
        return brands;
    }
    
    /**
     * Deactivate a brand (soft delete).
     * Used for admin brand management.
     */
    @Transactional
    public void deactivateBrand(Long id) {
        log.debug("Deactivating brand with ID: {}", id);
        Brand brand = getBrandById(id);
        brand.setActive(false);
        brandRepository.save(brand);
        log.info("Deactivated brand with ID: {}", id);
    }
    
    /**
     * Check if a brand name exists.
     * Used for brand creation validation.
     */
    public boolean brandNameExists(String name) {
        log.debug("Checking if brand name exists: {}", name);
        return brandRepository.existsByNameIgnoreCase(name);
    }
}