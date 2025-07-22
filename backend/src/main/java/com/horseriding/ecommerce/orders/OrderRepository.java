package com.horseriding.ecommerce.orders;

import com.horseriding.ecommerce.users.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Order entity.
 * Provides basic CRUD operations, search, and pagination.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders for a specific user with pagination.
     * Used for customer order history.
     */
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Find all orders for a specific user ID with pagination.
     * Used for customer order history without loading the user.
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find an order by PayPal payment ID.
     * Used for PayPal webhook integration.
     */
    Optional<Order> findByPaypalPaymentId(String paypalPaymentId);

    /**
     * Search for orders by user name, email, or tracking number with pagination.
     * Used for admin order management with search functionality.
     */
    @Query("SELECT o FROM Order o JOIN o.user u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(o.trackingNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Order> searchOrders(@Param("searchTerm") String searchTerm, Pageable pageable);
}