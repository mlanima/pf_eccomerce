package com.horseriding.ecommerce.products;

import com.horseriding.ecommerce.brands.Brand;
import com.horseriding.ecommerce.categories.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity with pagination and filtering support.
 * Extends PagingAndSortingRepository for pagination and JpaSpecificationExecutor for dynamic filtering.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * Find all active products with pagination.
     * Used for product catalog display.
     *
     * @param pageable Pagination information
     * @return Page of active products
     */
    Page<Product> findByActiveTrue(Pageable pageable);

    /**
     * Find all products by category with pagination.
     * Used for category-based product browsing.
     *
     * @param category The category to filter by
     * @param pageable Pagination information
     * @return Page of products in the specified category
     */
    Page<Product> findByCategory(Category category, Pageable pageable);

    /**
     * Find all active products by category with pagination.
     * Used for customer-facing category-based product browsing.
     *
     * @param category The category to filter by
     * @param pageable Pagination information
     * @return Page of active products in the specified category
     */
    Page<Product> findByCategoryAndActiveTrue(Category category, Pageable pageable);

    /**
     * Find all products by category ID with pagination.
     * Used for category-based product browsing without loading the category.
     *
     * @param categoryId The category ID to filter by
     * @param pageable Pagination information
     * @return Page of products in the specified category
     */
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * Find all active products by category ID with pagination.
     * Used for customer-facing category-based product browsing without loading the category.
     *
     * @param categoryId The category ID to filter by
     * @param pageable Pagination information
     * @return Page of active products in the specified category
     */
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    /**
     * Find all products by brand with pagination.
     * Used for brand-based product browsing.
     *
     * @param brand The brand to filter by
     * @param pageable Pagination information
     * @return Page of products from the specified brand
     */
    Page<Product> findByBrand(Brand brand, Pageable pageable);

    /**
     * Find all active products by brand with pagination.
     * Used for customer-facing brand-based product browsing.
     *
     * @param brand The brand to filter by
     * @param pageable Pagination information
     * @return Page of active products from the specified brand
     */
    Page<Product> findByBrandAndActiveTrue(Brand brand, Pageable pageable);

    /**
     * Find all products by brand ID with pagination.
     * Used for brand-based product browsing without loading the brand.
     *
     * @param brandId The brand ID to filter by
     * @param pageable Pagination information
     * @return Page of products from the specified brand
     */
    Page<Product> findByBrandId(Long brandId, Pageable pageable);

    /**
     * Find all active products by brand ID with pagination.
     * Used for customer-facing brand-based product browsing without loading the brand.
     *
     * @param brandId The brand ID to filter by
     * @param pageable Pagination information
     * @return Page of active products from the specified brand
     */
    Page<Product> findByBrandIdAndActiveTrue(Long brandId, Pageable pageable);

    /**
     * Find all products that are in stock (stock quantity > 0) with pagination.
     * Used for filtering available products.
     *
     * @param pageable Pagination information
     * @return Page of in-stock products
     */
    Page<Product> findByStockQuantityGreaterThan(Integer stockQuantity, Pageable pageable);

    /**
     * Find all active products that are in stock (stock quantity > 0) with pagination.
     * Used for customer-facing filtering of available products.
     *
     * @param pageable Pagination information
     * @return Page of active in-stock products
     */
    Page<Product> findByActiveTrueAndStockQuantityGreaterThan(Integer stockQuantity, Pageable pageable);

    /**
     * Find all products with low stock (stock quantity <= low stock threshold) with pagination.
     * Used for inventory management and alerts.
     *
     * @param pageable Pagination information
     * @return Page of low stock products
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.lowStockThreshold AND p.stockQuantity > 0")
    Page<Product> findLowStockProducts(Pageable pageable);

    /**
     * Find all active products with low stock (stock quantity <= low stock threshold) with pagination.
     * Used for inventory management and alerts.
     *
     * @param pageable Pagination information
     * @return Page of active low stock products
     */
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockQuantity <= p.lowStockThreshold AND p.stockQuantity > 0")
    Page<Product> findActiveLowStockProducts(Pageable pageable);

    /**
     * Find all products that are out of stock (stock quantity = 0) with pagination.
     * Used for inventory management.
     *
     * @param pageable Pagination information
     * @return Page of out-of-stock products
     */
    Page<Product> findByStockQuantity(Integer stockQuantity, Pageable pageable);

    /**
     * Find all active products that are out of stock (stock quantity = 0) with pagination.
     * Used for inventory management of active products.
     *
     * @param pageable Pagination information
     * @return Page of active out-of-stock products
     */
    Page<Product> findByActiveTrueAndStockQuantity(Integer stockQuantity, Pageable pageable);

    /**
     * Find all featured products.
     * Used for homepage and promotional displays.
     *
     * @return List of featured products
     */
    List<Product> findByFeaturedTrue();

    /**
     * Find all active featured products.
     * Used for customer-facing homepage and promotional displays.
     *
     * @return List of active featured products
     */
    List<Product> findByFeaturedTrueAndActiveTrue();

    /**
     * Find all featured products with pagination.
     * Used for homepage and promotional displays with pagination.
     *
     * @param pageable Pagination information
     * @return Page of featured products
     */
    Page<Product> findByFeaturedTrue(Pageable pageable);

    /**
     * Find all active featured products with pagination.
     * Used for customer-facing homepage and promotional displays with pagination.
     *
     * @param pageable Pagination information
     * @return Page of active featured products
     */
    Page<Product> findByFeaturedTrueAndActiveTrue(Pageable pageable);

    /**
     * Find a product by SKU.
     * Used for product lookup by unique identifier.
     *
     * @param sku The SKU to search for
     * @return Optional containing the product if found
     */
    Optional<Product> findBySku(String sku);

    /**
     * Check if a product with the given SKU exists.
     * Used for product creation validation.
     *
     * @param sku The SKU to check
     * @return true if a product with the SKU exists, false otherwise
     */
    boolean existsBySku(String sku);

    /**
     * Search for products by name or description with pagination.
     * Used for product search functionality.
     *
     * @param searchTerm The search term to match against name or description
     * @param pageable Pagination information
     * @return Page of products matching the search criteria
     */
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search for active products by name or description with pagination.
     * Used for customer-facing product search functionality.
     *
     * @param searchTerm The search term to match against name or description
     * @param pageable Pagination information
     * @return Page of active products matching the search criteria
     */
    @Query("SELECT p FROM Product p WHERE p.active = true AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> searchActiveProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find products by price range with pagination.
     * Used for price filtering in product catalog.
     *
     * @param minPrice The minimum price
     * @param maxPrice The maximum price
     * @param pageable Pagination information
     * @return Page of products within the price range
     */
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find active products by price range with pagination.
     * Used for customer-facing price filtering in product catalog.
     *
     * @param minPrice The minimum price
     * @param maxPrice The maximum price
     * @param pageable Pagination information
     * @return Page of active products within the price range
     */
    Page<Product> findByActiveTrueAndPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find products by category and price range with pagination.
     * Used for combined category and price filtering.
     *
     * @param category The category to filter by
     * @param minPrice The minimum price
     * @param maxPrice The maximum price
     * @param pageable Pagination information
     * @return Page of products matching the criteria
     */
    Page<Product> findByCategoryAndPriceBetween(Category category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find active products by category and price range with pagination.
     * Used for customer-facing combined category and price filtering.
     *
     * @param category The category to filter by
     * @param minPrice The minimum price
     * @param maxPrice The maximum price
     * @param pageable Pagination information
     * @return Page of active products matching the criteria
     */
    Page<Product> findByCategoryAndActiveTrueAndPriceBetween(Category category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find products by category ID and price range with pagination.
     * Used for combined category and price filtering without loading the category.
     *
     * @param categoryId The category ID to filter by
     * @param minPrice The minimum price
     * @param maxPrice The maximum price
     * @param pageable Pagination information
     * @return Page of products matching the criteria
     */
    Page<Product> findByCategoryIdAndPriceBetween(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find active products by category ID and price range with pagination.
     * Used for customer-facing combined category and price filtering without loading the category.
     *
     * @param categoryId The category ID to filter by
     * @param minPrice The minimum price
     * @param maxPrice The maximum price
     * @param pageable Pagination information
     * @return Page of active products matching the criteria
     */
    Page<Product> findByCategoryIdAndActiveTrueAndPriceBetween(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find products by brand and price range with pagination.
     * Used for combined brand and price filtering.
     *
     * @param brand The brand to filter by
     * @param minPrice The minimum price
     * @param maxPrice The maximum price
     * @param pageable Pagination information
     * @return Page of products matching the criteria
     */
    Page<Product> findByBrandAndPriceBetween(Brand brand, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find active products by brand and price range with pagination.
     * Used for customer-facing combined brand and price filtering.
     *
     * @param brand The brand to filter by
     * @param minPrice The minimum price
     * @param maxPrice The maximum price
     * @param pageable Pagination information
     * @return Page of active products matching the criteria
     */
    Page<Product> findByBrandAndActiveTrueAndPriceBetween(Brand brand, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find products by brand ID and price range with pagination.
     * Used for combined brand and price filtering without loading the brand.
     *
     * @param brandId The brand ID to filter by
     * @param minPrice The minimum price
     * @param maxPrice The maximum price
     * @param pageable Pagination information
     * @return Page of products matching the criteria
     */
    Page<Product> findByBrandIdAndPriceBetween(Long brandId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find active products by brand ID and price range with pagination.
     * Used for customer-facing combined brand and price filtering without loading the brand.
     *
     * @param brandId The brand ID to filter by
     * @param minPrice The minimum price
     * @param maxPrice The maximum price
     * @param pageable Pagination information
     * @return Page of active products matching the criteria
     */
    Page<Product> findByBrandIdAndActiveTrueAndPriceBetween(Long brandId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Find recently added products with pagination.
     * Used for "New Arrivals" section.
     *
     * @param pageable Pagination information
     * @return Page of products ordered by creation date
     */
    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find recently added active products with pagination.
     * Used for customer-facing "New Arrivals" section.
     *
     * @param pageable Pagination information
     * @return Page of active products ordered by creation date
     */
    Page<Product> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Count products by category.
     * Used for category statistics and management.
     *
     * @param category The category to count products for
     * @return The number of products in the category
     */
    long countByCategory(Category category);

    /**
     * Count active products by category.
     * Used for customer-facing category statistics.
     *
     * @param category The category to count products for
     * @return The number of active products in the category
     */
    long countByCategoryAndActiveTrue(Category category);

    /**
     * Count products by brand.
     * Used for brand statistics and management.
     *
     * @param brand The brand to count products for
     * @return The number of products from the brand
     */
    long countByBrand(Brand brand);

    /**
     * Count active products by brand.
     * Used for customer-facing brand statistics.
     *
     * @param brand The brand to count products for
     * @return The number of active products from the brand
     */
    long countByBrandAndActiveTrue(Brand brand);

    /**
     * Advanced search for products with multiple criteria and pagination.
     * Used for comprehensive product filtering.
     *
     * @param searchTerm The search term to match against name or description
     * @param categoryId The category ID to filter by (optional)
     * @param brandId The brand ID to filter by (optional)
     * @param minPrice The minimum price (optional)
     * @param maxPrice The maximum price (optional)
     * @param inStock Whether to include only in-stock products (optional)
     * @param pageable Pagination information
     * @return Page of products matching the criteria
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "WHERE (:searchTerm IS NULL OR " +
           "      LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "      LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:inStock IS NULL OR (:inStock = true AND p.stockQuantity > 0) OR (:inStock = false))")
    Page<Product> advancedSearch(
            @Param("searchTerm") String searchTerm,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("inStock") Boolean inStock,
            Pageable pageable);

    /**
     * Advanced search for active products with multiple criteria and pagination.
     * Used for customer-facing comprehensive product filtering.
     *
     * @param searchTerm The search term to match against name or description
     * @param categoryId The category ID to filter by (optional)
     * @param brandId The brand ID to filter by (optional)
     * @param minPrice The minimum price (optional)
     * @param maxPrice The maximum price (optional)
     * @param inStock Whether to include only in-stock products (optional)
     * @param pageable Pagination information
     * @return Page of active products matching the criteria
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "WHERE p.active = true " +
           "AND (:searchTerm IS NULL OR " +
           "     LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:brandId IS NULL OR p.brand.id = :brandId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:inStock IS NULL OR (:inStock = true AND p.stockQuantity > 0) OR (:inStock = false))")
    Page<Product> advancedSearchActive(
            @Param("searchTerm") String searchTerm,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("inStock") Boolean inStock,
            Pageable pageable);
}