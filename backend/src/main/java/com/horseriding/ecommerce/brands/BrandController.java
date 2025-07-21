package com.horseriding.ecommerce.brands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for Brand entity operations.
 * Provides endpoints for brand management.
 */
@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Slf4j
public class BrandController {

    private final BrandService brandService;

    /**
     * Get all active brands.
     * Public endpoint for customers to view brands.
     */
    @GetMapping
    public ResponseEntity<List<Brand>> getAllBrands() {
        log.debug("GET /api/brands - Fetching all active brands");
        List<Brand> brands = brandService.getAllActiveBrands();
        return ResponseEntity.ok(brands);
    }

    /**
     * Get all active brands with pagination.
     * Public endpoint with pagination support.
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<Brand>> getAllBrandsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.debug("GET /api/brands/paginated - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                 page, size, sortBy, sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Brand> brands = brandService.getAllActiveBrands(pageable);
        
        return ResponseEntity.ok(brands);
    }

    /**
     * Get brand by ID.
     * Public endpoint for viewing brand details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable Long id) {
        log.debug("GET /api/brands/{} - Fetching brand by ID", id);
        
        return brandService.getBrandById(id)
                .map(brand -> ResponseEntity.ok(brand))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search brands by name.
     * Public endpoint for brand search.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Brand>> searchBrands(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("GET /api/brands/search - query: {}, page: {}, size: {}", query, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Brand> brands = brandService.searchBrands(query, pageable);
        
        return ResponseEntity.ok(brands);
    }

    /**
     * Get brands by country.
     * Public endpoint for filtering brands by country.
     */
    @GetMapping("/country/{country}")
    public ResponseEntity<List<Brand>> getBrandsByCountry(@PathVariable String country) {
        log.debug("GET /api/brands/country/{} - Fetching brands by country", country);
        
        List<Brand> brands = brandService.getBrandsByCountry(country);
        return ResponseEntity.ok(brands);
    }

    /**
     * Get brands ordered by product count.
     * Public endpoint for popular brands.
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Brand>> getPopularBrands() {
        log.debug("GET /api/brands/popular - Fetching brands ordered by product count");
        
        List<Brand> brands = brandService.getBrandsOrderedByProductCount();
        return ResponseEntity.ok(brands);
    }

    /**
     * Create a new brand.
     * Admin-only endpoint.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Brand> createBrand(@Valid @RequestBody Brand brand) {
        log.info("POST /api/brands - Creating new brand: {}", brand.getName());
        
        try {
            Brand createdBrand = brandService.createBrand(brand);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBrand);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create brand: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update an existing brand.
     * Admin-only endpoint.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Brand> updateBrand(@PathVariable Long id, @Valid @RequestBody Brand brand) {
        log.info("PUT /api/brands/{} - Updating brand", id);
        
        try {
            Brand updatedBrand = brandService.updateBrand(id, brand);
            return ResponseEntity.ok(updatedBrand);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update brand: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deactivate a brand (soft delete).
     * Admin-only endpoint.
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Void> deactivateBrand(@PathVariable Long id) {
        log.info("PATCH /api/brands/{}/deactivate - Deactivating brand", id);
        
        try {
            brandService.deactivateBrand(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to deactivate brand: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Permanently delete a brand.
     * Admin-only endpoint.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        log.info("DELETE /api/brands/{} - Permanently deleting brand", id);
        
        try {
            brandService.deleteBrand(id);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            log.warn("Cannot delete brand: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            log.warn("Brand not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get brand statistics.
     * Admin-only endpoint.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<BrandStats> getBrandStats() {
        log.debug("GET /api/brands/stats - Fetching brand statistics");
        
        long activeBrandCount = brandService.getActiveBrandCount();
        BrandStats stats = new BrandStats(activeBrandCount);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Check if brand name exists.
     * Admin-only endpoint for validation.
     */
    @GetMapping("/check-name")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Boolean> checkBrandNameExists(@RequestParam String name) {
        log.debug("GET /api/brands/check-name - Checking if brand name exists: {}", name);
        
        boolean exists = brandService.brandNameExists(name);
        return ResponseEntity.ok(exists);
    }

    /**
     * Simple DTO for brand statistics.
     */
    public record BrandStats(long activeBrandCount) {}
} 