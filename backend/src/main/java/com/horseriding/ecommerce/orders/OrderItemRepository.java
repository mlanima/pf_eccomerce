package com.horseriding.ecommerce.orders;

import com.horseriding.ecommerce.products.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for OrderItem entity.
 * Provides basic CRUD operations, search, and pagination.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all order items for a specific order.
     * Used for order detail display.
     */
    List<OrderItem> findByOrder(Order order);

    /**
     * Find all order items for a specific order with pagination.
     * Used for order detail display with pagination.
     */
    Page<OrderItem> findByOrder(Order order, Pageable pageable);

    /**
     * Find all order items for a specific product.
     * Used for product sales history.
     */
    List<OrderItem> findByProduct(Product product);
}