package com.horseriding.ecommerce.brands;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
     * Find brands by name containing search term (case-insensitive).
     */
    Page<Brand> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find brands by country of origin.
     */
    List<Brand> findByCountryOfOriginIgnoreCase(String countryOfOrigin);

    /**
     * Check if brand name exists (case-insensitive).
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find brands that have products.
     */
    @Query("SELECT DISTINCT b FROM Brand b INNER JOIN b.products p")
    List<Brand> findBrandsWithProducts();
}