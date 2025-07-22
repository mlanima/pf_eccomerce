package com.horseriding.ecommerce.categories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Category entity.
 * Provides basic CRUD operations, search, and pagination.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find all root categories (categories without a parent).
     * Used for displaying top-level categories.
     */
    List<Category> findByParentIsNull();

    /**
     * Find all subcategories of a given parent category.
     * Used for hierarchical category navigation.
     */
    List<Category> findByParent(Category parent);

    /**
     * Find a category by name.
     * Used for category lookup and validation.
     */
    Optional<Category> findByName(String name);

    /**
     * Check if a category with the given name exists.
     * Used for category creation validation.
     */
    boolean existsByName(String name);

    /**
     * Search for categories by name with pagination.
     * Used for category management with search functionality.
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Category> searchCategories(@Param("searchTerm") String searchTerm, Pageable pageable);
}
