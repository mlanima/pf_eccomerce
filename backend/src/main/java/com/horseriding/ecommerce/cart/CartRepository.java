package com.horseriding.ecommerce.cart;

import com.horseriding.ecommerce.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Cart entity.
 * Provides basic CRUD operations.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find a cart by user.
     * Used for retrieving a user's shopping cart.
     */
    Optional<Cart> findByUser(User user);

    /**
     * Find a cart by user ID.
     * Used for retrieving a user's shopping cart without loading the user.
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Check if a cart exists for a user.
     * Used for cart creation logic.
     */
    boolean existsByUser(User user);
}