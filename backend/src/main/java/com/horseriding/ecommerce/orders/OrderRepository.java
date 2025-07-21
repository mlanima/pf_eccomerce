package com.horseriding.ecommerce.orders;

import com.horseriding.ecommerce.users.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity with customer and admin views.
 * Provides methods for finding orders by user, status, and date range.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    /**
     * Find all orders for a specific user.
     * Used for customer order history.
     *
     * @param user The user to find orders for
     * @return List of orders for the user
     */
    List<Order> findByUser(User user);

    /**
     * Find all orders for a specific user with pagination.
     * Used for customer order history with pagination.
     *
     * @param user The user to find orders for
     * @param pageable Pagination information
     * @return Page of orders for the user
     */
    Page<Order> findByUser(User user, Pageable pageable);

    /**
     * Find all orders for a specific user ordered by creation date (newest first).
     * Used for customer order history display.
     *
     * @param user The user to find orders for
     * @return List of orders for the user ordered by creation date
     */
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find all orders for a specific user ordered by creation date (newest first) with pagination.
     * Used for customer order history display with pagination.
     *
     * @param user The user to find orders for
     * @param pageable Pagination information
     * @return Page of orders for the user ordered by creation date
     */
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Find all orders for a specific user with a specific status.
     * Used for filtering customer order history by status.
     *
     * @param user The user to find orders for
     * @param status The order status to filter by
     * @return List of orders for the user with the specified status
     */
    List<Order> findByUserAndStatus(User user, OrderStatus status);

    /**
     * Find all orders for a specific user with a specific status with pagination.
     * Used for filtering customer order history by status with pagination.
     *
     * @param user The user to find orders for
     * @param status The order status to filter by
     * @param pageable Pagination information
     * @return Page of orders for the user with the specified status
     */
    Page<Order> findByUserAndStatus(User user, OrderStatus status, Pageable pageable);

    /**
     * Find all orders for a specific user ID.
     * Used for customer order history without loading the user.
     *
     * @param userId The user ID to find orders for
     * @return List of orders for the user ID
     */
    List<Order> findByUserId(Long userId);

    /**
     * Find all orders for a specific user ID with pagination.
     * Used for customer order history without loading the user with pagination.
     *
     * @param userId The user ID to find orders for
     * @param pageable Pagination information
     * @return Page of orders for the user ID
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /**
     * Find all orders for a specific user ID ordered by creation date (newest first).
     * Used for customer order history display without loading the user.
     *
     * @param userId The user ID to find orders for
     * @return List of orders for the user ID ordered by creation date
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find all orders for a specific user ID ordered by creation date (newest first) with pagination.
     * Used for customer order history display without loading the user with pagination.
     *
     * @param userId The user ID to find orders for
     * @param pageable Pagination information
     * @return Page of orders for the user ID ordered by creation date
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find all orders for a specific user ID with a specific status.
     * Used for filtering customer order history by status without loading the user.
     *
     * @param userId The user ID to find orders for
     * @param status The order status to filter by
     * @return List of orders for the user ID with the specified status
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * Find all orders for a specific user ID with a specific status with pagination.
     * Used for filtering customer order history by status without loading the user with pagination.
     *
     * @param userId The user ID to find orders for
     * @param status The order status to filter by
     * @param pageable Pagination information
     * @return Page of orders for the user ID with the specified status
     */
    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    /**
     * Find all orders with a specific status.
     * Used for admin order management filtering by status.
     *
     * @param status The order status to filter by
     * @return List of orders with the specified status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find all orders with a specific status with pagination.
     * Used for admin order management filtering by status with pagination.
     *
     * @param status The order status to filter by
     * @param pageable Pagination information
     * @return Page of orders with the specified status
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * Find all orders ordered by creation date (newest first).
     * Used for admin order management display.
     *
     * @return List of orders ordered by creation date
     */
    List<Order> findAllByOrderByCreatedAtDesc();

    /**
     * Find all orders ordered by creation date (newest first) with pagination.
     * Used for admin order management display with pagination.
     *
     * @param pageable Pagination information
     * @return Page of orders ordered by creation date
     */
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find all orders with a specific status ordered by creation date (newest first).
     * Used for admin order management filtering by status and ordered by date.
     *
     * @param status The order status to filter by
     * @return List of orders with the specified status ordered by creation date
     */
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    /**
     * Find all orders with a specific status ordered by creation date (newest first) with pagination.
     * Used for admin order management filtering by status and ordered by date with pagination.
     *
     * @param status The order status to filter by
     * @param pageable Pagination information
     * @return Page of orders with the specified status ordered by creation date
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    /**
     * Find an order by PayPal payment ID.
     * Used for PayPal webhook integration and payment verification.
     *
     * @param paypalPaymentId The PayPal payment ID to search for
     * @return Optional containing the order if found
     */
    Optional<Order> findByPaypalPaymentId(String paypalPaymentId);

    /**
     * Find an order by PayPal order ID.
     * Used for PayPal webhook integration and order verification.
     *
     * @param paypalOrderId The PayPal order ID to search for
     * @return Optional containing the order if found
     */
    Optional<Order> findByPaypalOrderId(String paypalOrderId);

    /**
     * Find an order by tracking number.
     * Used for order tracking and customer service.
     *
     * @param trackingNumber The tracking number to search for
     * @return Optional containing the order if found
     */
    Optional<Order> findByTrackingNumber(String trackingNumber);

    /**
     * Find orders created between two dates.
     * Used for admin reporting and analytics.
     *
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of orders created between the dates
     */
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find orders created between two dates with pagination.
     * Used for admin reporting and analytics with pagination.
     *
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param pageable Pagination information
     * @return Page of orders created between the dates
     */
    Page<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find orders with total amount greater than or equal to a value.
     * Used for admin reporting and analytics.
     *
     * @param amount The minimum total amount
     * @return List of orders with total amount greater than or equal to the value
     */
    List<Order> findByTotalAmountGreaterThanEqual(BigDecimal amount);

    /**
     * Find orders with total amount greater than or equal to a value with pagination.
     * Used for admin reporting and analytics with pagination.
     *
     * @param amount The minimum total amount
     * @param pageable Pagination information
     * @return Page of orders with total amount greater than or equal to the value
     */
    Page<Order> findByTotalAmountGreaterThanEqual(BigDecimal amount, Pageable pageable);

    /**
     * Find orders with total amount less than or equal to a value.
     * Used for admin reporting and analytics.
     *
     * @param amount The maximum total amount
     * @return List of orders with total amount less than or equal to the value
     */
    List<Order> findByTotalAmountLessThanEqual(BigDecimal amount);

    /**
     * Find orders with total amount less than or equal to a value with pagination.
     * Used for admin reporting and analytics with pagination.
     *
     * @param amount The maximum total amount
     * @param pageable Pagination information
     * @return Page of orders with total amount less than or equal to the value
     */
    Page<Order> findByTotalAmountLessThanEqual(BigDecimal amount, Pageable pageable);

    /**
     * Find orders with total amount between two values.
     * Used for admin reporting and analytics.
     *
     * @param minAmount The minimum total amount
     * @param maxAmount The maximum total amount
     * @return List of orders with total amount between the values
     */
    List<Order> findByTotalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * Find orders with total amount between two values with pagination.
     * Used for admin reporting and analytics with pagination.
     *
     * @param minAmount The minimum total amount
     * @param maxAmount The maximum total amount
     * @param pageable Pagination information
     * @return Page of orders with total amount between the values
     */
    Page<Order> findByTotalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * Count orders by status.
     * Used for admin dashboard statistics.
     *
     * @param status The order status to count
     * @return The number of orders with the specified status
     */
    long countByStatus(OrderStatus status);

    /**
     * Count orders by user.
     * Used for customer statistics.
     *
     * @param user The user to count orders for
     * @return The number of orders for the user
     */
    long countByUser(User user);

    /**
     * Count orders by user ID.
     * Used for customer statistics without loading the user.
     *
     * @param userId The user ID to count orders for
     * @return The number of orders for the user ID
     */
    long countByUserId(Long userId);

    /**
     * Count orders created between two dates.
     * Used for admin reporting and analytics.
     *
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return The number of orders created between the dates
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Search for orders by user name, email, or tracking number with pagination.
     * Used for admin order management with search functionality.
     *
     * @param searchTerm The search term to match against user name, email, or tracking number
     * @param pageable Pagination information
     * @return Page of orders matching the search criteria
     */
    @Query("SELECT o FROM Order o JOIN o.user u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.trackingNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Order> searchOrders(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Advanced search for orders with multiple criteria and pagination.
     * Used for admin order management with comprehensive filtering.
     *
     * @param userId The user ID to filter by (optional)
     * @param status The order status to filter by (optional)
     * @param minAmount The minimum total amount (optional)
     * @param maxAmount The maximum total amount (optional)
     * @param startDate The start date (inclusive, optional)
     * @param endDate The end date (inclusive, optional)
     * @param searchTerm The search term to match against user name, email, or tracking number (optional)
     * @param pageable Pagination information
     * @return Page of orders matching the criteria
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN o.user u WHERE " +
           "(:userId IS NULL OR o.user.id = :userId) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:minAmount IS NULL OR o.totalAmount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR o.totalAmount <= :maxAmount) AND " +
           "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.createdAt <= :endDate) AND " +
           "(:searchTerm IS NULL OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.trackingNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Order> advancedSearch(
            @Param("userId") Long userId,
            @Param("status") OrderStatus status,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    /**
     * Find recent orders with pagination.
     * Used for admin dashboard recent orders display.
     *
     * @param limit The maximum number of orders to return
     * @return List of recent orders
     */
    @Query(value = "SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(Pageable pageable);

    /**
     * Find orders requiring attention (PAID or PROCESSING status) with pagination.
     * Used for admin dashboard to highlight orders that need processing.
     *
     * @param pageable Pagination information
     * @return Page of orders requiring attention
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PAID' OR o.status = 'PROCESSING' ORDER BY o.createdAt ASC")
    Page<Order> findOrdersRequiringAttention(Pageable pageable);

    /**
     * Find orders shipped but not delivered with pagination.
     * Used for admin dashboard to track shipments in progress.
     *
     * @param pageable Pagination information
     * @return Page of orders shipped but not delivered
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'SHIPPED' ORDER BY o.shippedAt ASC")
    Page<Order> findOrdersInTransit(Pageable pageable);

    /**
     * Calculate total sales amount for a date range.
     * Used for admin reporting and analytics.
     *
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return The total sales amount for the date range
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE " +
           "o.status != 'CANCELLED' AND o.status != 'REFUNDED' AND " +
           "o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal calculateTotalSales(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate total sales amount for a date range by status.
     * Used for admin reporting and analytics with status filtering.
     *
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param status The order status to filter by
     * @return The total sales amount for the date range and status
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE " +
           "o.status = :status AND " +
           "o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal calculateTotalSalesByStatus(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") OrderStatus status);

    /**
     * Count orders by day for a date range.
     * Used for admin dashboard charts and analytics.
     *
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of daily order counts
     */
    @Query("SELECT FUNCTION('DATE', o.createdAt) as orderDate, COUNT(o) as orderCount " +
           "FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate " +
           "GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY orderDate")
    List<Object[]> countOrdersByDay(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate sales by day for a date range.
     * Used for admin dashboard charts and analytics.
     *
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return List of daily sales amounts
     */
    @Query("SELECT FUNCTION('DATE', o.createdAt) as orderDate, SUM(o.totalAmount) as dailySales " +
           "FROM Order o WHERE o.status != 'CANCELLED' AND o.status != 'REFUNDED' AND " +
           "o.createdAt >= :startDate AND o.createdAt <= :endDate " +
           "GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY orderDate")
    List<Object[]> calculateSalesByDay(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}