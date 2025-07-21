package com.horseriding.ecommerce.cart;

import com.horseriding.ecommerce.products.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CartItem entity.
 * Provides methods for finding cart items by cart and product.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find all cart items for a specific cart.
     * Used for cart display and management.
     *
     * @param cart The cart to find items for
     * @return List of cart items for the cart
     */
    List<CartItem> findByCart(Cart cart);

    /**
     * Find all cart items for a specific cart ID.
     * Used for cart display and management without loading the cart.
     *
     * @param cartId The cart ID to find items for
     * @return List of cart items for the cart ID
     */
    List<CartItem> findByCartId(Long cartId);

    /**
     * Find a cart item by cart and product.
     * Used for checking if a product is already in the cart.
     *
     * @param cart The cart to find the item in
     * @param product The product to find in the cart
     * @return Optional containing the cart item if found
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    /**
     * Find a cart item by cart ID and product ID.
     * Used for checking if a product is already in the cart without loading the cart and product.
     *
     * @param cartId The cart ID to find the item in
     * @param productId The product ID to find in the cart
     * @return Optional containing the cart item if found
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    /**
     * Check if a cart item exists for a cart and product.
     * Used for cart item creation logic.
     *
     * @param cart The cart to check for the item
     * @param product The product to check for in the cart
     * @return true if a cart item exists for the cart and product, false otherwise
     */
    boolean existsByCartAndProduct(Cart cart, Product product);

    /**
     * Check if a cart item exists for a cart ID and product ID.
     * Used for cart item creation logic without loading the cart and product.
     *
     * @param cartId The cart ID to check for the item
     * @param productId The product ID to check for in the cart
     * @return true if a cart item exists for the cart ID and product ID, false otherwise
     */
    boolean existsByCartIdAndProductId(Long cartId, Long productId);

    /**
     * Delete a cart item by cart and product.
     * Used for removing a product from the cart.
     *
     * @param cart The cart to remove the item from
     * @param product The product to remove from the cart
     */
    void deleteByCartAndProduct(Cart cart, Product product);

    /**
     * Delete a cart item by cart ID and product ID.
     * Used for removing a product from the cart without loading the cart and product.
     *
     * @param cartId The cart ID to remove the item from
     * @param productId The product ID to remove from the cart
     */
    void deleteByCartIdAndProductId(Long cartId, Long productId);

    /**
     * Delete all cart items for a specific cart.
     * Used for clearing a cart.
     *
     * @param cart The cart to clear
     */
    void deleteByCart(Cart cart);

    /**
     * Delete all cart items for a specific cart ID.
     * Used for clearing a cart without loading the cart.
     *
     * @param cartId The cart ID to clear
     */
    void deleteByCartId(Long cartId);

    /**
     * Find all cart items for a specific product.
     * Used for product removal and inventory management.
     *
     * @param product The product to find cart items for
     * @return List of cart items for the product
     */
    List<CartItem> findByProduct(Product product);

    /**
     * Find all cart items for a specific product ID.
     * Used for product removal and inventory management without loading the product.
     *
     * @param productId The product ID to find cart items for
     * @return List of cart items for the product ID
     */
    List<CartItem> findByProductId(Long productId);

    /**
     * Count cart items by product.
     * Used for product popularity analytics.
     *
     * @param product The product to count cart items for
     * @return The number of cart items for the product
     */
    long countByProduct(Product product);

    /**
     * Count cart items by product ID.
     * Used for product popularity analytics without loading the product.
     *
     * @param productId The product ID to count cart items for
     * @return The number of cart items for the product ID
     */
    long countByProductId(Long productId);

    /**
     * Update the quantity of a cart item.
     * Used for cart item quantity management.
     *
     * @param cartId The cart ID containing the item
     * @param productId The product ID of the item
     * @param quantity The new quantity
     * @return The number of rows affected
     */
    @Modifying
    @Transactional
    @Query("UPDATE CartItem ci SET ci.quantity = :quantity, ci.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    int updateQuantity(@Param("cartId") Long cartId, @Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Find the most frequently added products to carts.
     * Used for product popularity analytics and recommendations.
     *
     * @param limit The maximum number of products to return
     * @return List of product IDs and their cart item counts
     */
    @Query("SELECT ci.product.id, COUNT(ci) as itemCount " +
           "FROM CartItem ci GROUP BY ci.product.id ORDER BY itemCount DESC")
    List<Object[]> findMostFrequentCartItems(@Param("limit") int limit);

    /**
     * Find products that are frequently added to carts together.
     * Used for product recommendations.
     *
     * @param productId The product ID to find related products for
     * @param limit The maximum number of related products to return
     * @return List of related product IDs and co-occurrence counts
     */
    @Query("SELECT ci2.product.id, COUNT(ci2) as frequency " +
           "FROM CartItem ci1 JOIN ci1.cart c JOIN c.items ci2 " +
           "WHERE ci1.product.id = :productId AND ci2.product.id != :productId " +
           "GROUP BY ci2.product.id ORDER BY frequency DESC")
    List<Object[]> findFrequentlyAddedTogether(@Param("productId") Long productId, @Param("limit") int limit);
}