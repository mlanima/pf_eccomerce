package com.horseriding.ecommerce.orders;

import com.horseriding.ecommerce.products.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for OrderItem entity.
 * Provides methods for finding order items by order and product.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all order items for a specific order.
     * Used for order detail display.
     *
     * @param order The order to find items for
     * @return List of order items for the order
     */
    List<OrderItem> findByOrder(Order order);

    /**
     * Find all order items for a specific order with pagination.
     * Used for order detail display with pagination.
     *
     * @param order The order to find items for
     * @param pageable Pagination information
     * @return Page of order items for the order
     */
    Page<OrderItem> findByOrder(Order order, Pageable pageable);

    /**
     * Find all order items for a specific order ID.
     * Used for order detail display without loading the order.
     *
     * @param orderId The order ID to find items for
     * @return List of order items for the order ID
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Find all order items for a specific order ID with pagination.
     * Used for order detail display without loading the order with pagination.
     *
     * @param orderId The order ID to find items for
     * @param pageable Pagination information
     * @return Page of order items for the order ID
     */
    Page<OrderItem> findByOrderId(Long orderId, Pageable pageable);

    /**
     * Find all order items for a specific product.
     * Used for product sales history.
     *
     * @param product The product to find items for
     * @return List of order items for the product
     */
    List<OrderItem> findByProduct(Product product);

    /**
     * Find all order items for a specific product with pagination.
     * Used for product sales history with pagination.
     *
     * @param product The product to find items for
     * @param pageable Pagination information
     * @return Page of order items for the product
     */
    Page<OrderItem> findByProduct(Product product, Pageable pageable);

    /**
     * Find all order items for a specific product ID.
     * Used for product sales history without loading the product.
     *
     * @param productId The product ID to find items for
     * @return List of order items for the product ID
     */
    List<OrderItem> findByProductId(Long productId);

    /**
     * Find all order items for a specific product ID with pagination.
     * Used for product sales history without loading the product with pagination.
     *
     * @param productId The product ID to find items for
     * @param pageable Pagination information
     * @return Page of order items for the product ID
     */
    Page<OrderItem> findByProductId(Long productId, Pageable pageable);

    /**
     * Count order items by product.
     * Used for product popularity statistics.
     *
     * @param product The product to count items for
     * @return The number of order items for the product
     */
    long countByProduct(Product product);

    /**
     * Count order items by product ID.
     * Used for product popularity statistics without loading the product.
     *
     * @param productId The product ID to count items for
     * @return The number of order items for the product ID
     */
    long countByProductId(Long productId);

    /**
     * Find top selling products for a date range.
     * Used for admin dashboard and analytics.
     *
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param pageable Pagination information
     * @return Page of product IDs and quantities sold
     */
    @Query("SELECT oi.product.id, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.status != 'CANCELLED' AND o.status != 'REFUNDED' AND " +
           "o.createdAt >= :startDate AND o.createdAt <= :endDate " +
           "GROUP BY oi.product.id ORDER BY totalQuantity DESC")
    Page<Object[]> findTopSellingProducts(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Calculate total quantity sold for a product in a date range.
     * Used for product sales analytics.
     *
     * @param productId The product ID to calculate sales for
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return The total quantity sold for the product in the date range
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi JOIN oi.order o " +
           "WHERE oi.product.id = :productId AND " +
           "o.status != 'CANCELLED' AND o.status != 'REFUNDED' AND " +
           "o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Integer calculateTotalQuantitySold(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find products that are frequently purchased together.
     * Used for product recommendations.
     *
     * @param productId The product ID to find related products for
     * @param pageable Pagination information
     * @return Page of related product IDs and co-occurrence counts
     */
    @Query("SELECT oi2.product.id, COUNT(oi2) as frequency " +
           "FROM OrderItem oi1 JOIN oi1.order o JOIN o.items oi2 " +
           "WHERE oi1.product.id = :productId AND oi2.product.id != :productId " +
           "GROUP BY oi2.product.id ORDER BY frequency DESC")
    Page<Object[]> findFrequentlyPurchasedTogether(@Param("productId") Long productId, Pageable pageable);
}