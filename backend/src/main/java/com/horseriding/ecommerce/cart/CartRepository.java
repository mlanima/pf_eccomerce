package com.horseriding.ecommerce.cart;

import com.horseriding.ecommerce.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Cart entity.
 * Provides methods for finding carts by user and managing cart data.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find a cart by user.
     * Used for retrieving a user's shopping cart.
     *
     * @param user The user to find the cart for
     * @return Optional containing the cart if found
     */
    Optional<Cart> findByUser(User user);

    /**
     * Find a cart by user ID.
     * Used for retrieving a user's shopping cart without loading the user.
     *
     * @param userId The user ID to find the cart for
     * @return Optional containing the cart if found
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Find a cart by session ID (for guest users).
     * Used for retrieving a guest user's shopping cart.
     *
     * @param sessionId The session ID to find the cart for
     * @return Optional containing the cart if found
     */
    Optional<Cart> findBySessionId(String sessionId);

    /**
     * Check if a cart exists for a user.
     * Used for cart creation logic.
     *
     * @param user The user to check for a cart
     * @return true if a cart exists for the user, false otherwise
     */
    boolean existsByUser(User user);

    /**
     * Check if a cart exists for a user ID.
     * Used for cart creation logic without loading the user.
     *
     * @param userId The user ID to check for a cart
     * @return true if a cart exists for the user ID, false otherwise
     */
    boolean existsByUserId(Long userId);

    /**
     * Check if a cart exists for a session ID.
     * Used for guest cart creation logic.
     *
     * @param sessionId The session ID to check for a cart
     * @return true if a cart exists for the session ID, false otherwise
     */
    boolean existsBySessionId(String sessionId);

    /**
     * Delete a cart by user.
     * Used for cart cleanup when a user is deleted.
     *
     * @param user The user whose cart should be deleted
     */
    void deleteByUser(User user);

    /**
     * Delete a cart by user ID.
     * Used for cart cleanup when a user is deleted without loading the user.
     *
     * @param userId The user ID whose cart should be deleted
     */
    void deleteByUserId(Long userId);

    /**
     * Delete a cart by session ID.
     * Used for guest cart cleanup.
     *
     * @param sessionId The session ID whose cart should be deleted
     */
    void deleteBySessionId(String sessionId);

    /**
     * Find all carts that have not been updated for a specified period.
     * Used for abandoned cart cleanup and analysis.
     *
     * @param cutoffDate Carts not updated since this date will be returned
     * @return List of abandoned carts
     */
    List<Cart> findByUpdatedAtBefore(LocalDateTime cutoffDate);

    /**
     * Count carts by update date range.
     * Used for cart analytics.
     *
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return The number of carts updated in the date range
     */
    long countByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find all carts with items.
     * Used for cart analytics and abandoned cart recovery.
     *
     * @return List of non-empty carts
     */
    @Query("SELECT c FROM Cart c WHERE SIZE(c.items) > 0")
    List<Cart> findNonEmptyCarts();

    /**
     * Find all carts with items that have not been updated for a specified period.
     * Used for abandoned cart recovery campaigns.
     *
     * @param cutoffDate Carts not updated since this date will be returned
     * @return List of abandoned non-empty carts
     */
    @Query("SELECT c FROM Cart c WHERE SIZE(c.items) > 0 AND c.updatedAt < :cutoffDate")
    List<Cart> findAbandonedCarts(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count the number of carts containing a specific product.
     * Used for product popularity analytics.
     *
     * @param productId The product ID to count carts for
     * @return The number of carts containing the product
     */
    @Query("SELECT COUNT(DISTINCT c) FROM Cart c JOIN c.items ci WHERE ci.product.id = :productId")
    long countCartsByProduct(@Param("productId") Long productId);

    /**
     * Find all carts containing a specific product.
     * Used for targeted marketing and product analytics.
     *
     * @param productId The product ID to find carts for
     * @return List of carts containing the product
     */
    @Query("SELECT DISTINCT c FROM Cart c JOIN c.items ci WHERE ci.product.id = :productId")
    List<Cart> findCartsByProduct(@Param("productId") Long productId);

    /**
     * Find all carts with a total value above a threshold.
     * Used for high-value cart recovery and analytics.
     *
     * @param minTotalValue The minimum total cart value
     * @return List of high-value carts
     */
    @Query("SELECT c FROM Cart c JOIN c.items ci GROUP BY c HAVING SUM(ci.product.price * ci.quantity) >= :minTotalValue")
    List<Cart> findHighValueCarts(@Param("minTotalValue") double minTotalValue);

    /**
     * Find all carts with a specific number of items or more.
     * Used for cart analytics and targeted marketing.
     *
     * @param minItemCount The minimum number of items
     * @return List of carts with at least the specified number of items
     */
    @Query("SELECT c FROM Cart c JOIN c.items ci GROUP BY c HAVING SUM(ci.quantity) >= :minItemCount")
    List<Cart> findCartsByMinimumItemCount(@Param("minItemCount") int minItemCount);
}