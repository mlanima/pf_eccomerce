package com.horseriding.ecommerce.cart;

import com.horseriding.ecommerce.products.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for CartItem entity.
 * Provides basic CRUD operations.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find all cart items for a specific cart.
     * Used for cart display and management.
     */
    List<CartItem> findByCart(Cart cart);

    /**
     * Find a cart item by cart and product.
     * Used for checking if a product is already in the cart.
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    /**
     * Check if a cart item exists for a cart and product.
     * Used for cart item creation logic.
     */
    boolean existsByCartAndProduct(Cart cart, Product product);

    /**
     * Delete a cart item by cart and product.
     * Used for removing a product from the cart.
     */
    void deleteByCartAndProduct(Cart cart, Product product);
}
