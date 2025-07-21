package com.horseriding.ecommerce.brands;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Brand entity operations.
 * Provides data access methods for brand management.
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    /**
     * Find brand by name (case-insensitive).
     */
    Optional<Brand> findByNameIgnoreCase(String name);

    /**
     * Find all active brands.
     */
    List<Brand> findByActiveTrue();

    /**
     * Find all active brands with pagination.
     */
    Page<Brand> findByActiveTrue(Pageable pageable);

    /**
     * Find brands by name containing search term (case-insensitive).
     */
    Page<Brand> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    /**
     * Find brands by country of origin.
     */
    List<Brand> findByCountryOfOriginIgnoreCaseAndActiveTrue(String countryOfOrigin);

    /**
     * Check if brand name exists (case-insensitive).
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Get brands with product count.
     */
    @Query("SELECT b FROM Brand b LEFT JOIN FETCH b.products WHERE b.active = true")
    List<Brand> findActiveBrandsWithProducts();

    /**
     * Find brands that have products.
     */
    @Query("SELECT DISTINCT b FROM Brand b INNER JOIN b.products p WHERE b.active = true AND p.active = true")
    List<Brand> findBrandsWithActiveProducts();

    /**
     * Count active brands.
     */
    long countByActiveTrue();
}