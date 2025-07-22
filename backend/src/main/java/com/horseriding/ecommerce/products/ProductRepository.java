package com.horseriding.ecommerce.products;

import com.horseriding.ecommerce.brands.Brand;
import com.horseriding.ecommerce.categories.Category;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Product entity.
 * Provides basic CRUD operations, search, and pagination.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find all products by category with pagination.
     * Used for category-based product browsing.
     *
     * @param category the category to filter by
     * @param pageable pagination information
     * @return page of products in the specified category
     */
    Page<Product> findByCategory(Category category, Pageable pageable);

    /**
     * Find all products by brand with pagination.
     * Used for brand-based product browsing.
     *
     * @param brand the brand to filter by
     * @param pageable pagination information
     * @return page of products from the specified brand
     */
    Page<Product> findByBrand(Brand brand, Pageable pageable);

    /**
     * Find a product by SKU.
     * Used for product lookup by unique identifier.
     *
     * @param sku the SKU to search for
     * @return Optional containing the product if found, empty otherwise
     */
    Optional<Product> findBySku(String sku);

    /**
     * Check if a product with the given SKU exists.
     * Used for product creation validation.
     *
     * @param sku the SKU to check
     * @return true if product exists, false otherwise
     */
    boolean existsBySku(String sku);

    /**
     * Search for products by name or description with pagination.
     * Used for product search functionality.
     *
     * @param searchTerm the search term to match against name and description
     * @param pageable pagination information
     * @return page of products matching the search criteria
     */
    @Query(
            "SELECT p FROM Product p WHERE "
                    + "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
                    + "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);
}
