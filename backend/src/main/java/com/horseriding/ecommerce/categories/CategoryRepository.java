package com.horseriding.ecommerce.categories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity with hierarchical queries.
 * Provides methods for finding categories by parent, level, and tree traversal.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find all root categories (categories without a parent).
     * Used for displaying top-level categories in navigation.
     *
     * @return List of root categories
     */
    List<Category> findByParentIsNull();

    /**
     * Find all active root categories (categories without a parent).
     * Used for displaying top-level categories in navigation.
     *
     * @return List of active root categories
     */
    List<Category> findByParentIsNullAndActiveTrue();

    /**
     * Find all root categories with pagination.
     * Used for admin category management.
     *
     * @param pageable Pagination information
     * @return Page of root categories
     */
    Page<Category> findByParentIsNull(Pageable pageable);

    /**
     * Find all active root categories with pagination.
     * Used for customer-facing category navigation.
     *
     * @param pageable Pagination information
     * @return Page of active root categories
     */
    Page<Category> findByParentIsNullAndActiveTrue(Pageable pageable);

    /**
     * Find all subcategories of a given parent category.
     * Used for hierarchical category navigation.
     *
     * @param parent The parent category
     * @return List of subcategories
     */
    List<Category> findByParent(Category parent);

    /**
     * Find all active subcategories of a given parent category.
     * Used for customer-facing category navigation.
     *
     * @param parent The parent category
     * @return List of active subcategories
     */
    List<Category> findByParentAndActiveTrue(Category parent);

    /**
     * Find all subcategories of a given parent category with pagination.
     * Used for admin category management.
     *
     * @param parent The parent category
     * @param pageable Pagination information
     * @return Page of subcategories
     */
    Page<Category> findByParent(Category parent, Pageable pageable);

    /**
     * Find all active subcategories of a given parent category with pagination.
     * Used for customer-facing category navigation with pagination.
     *
     * @param parent The parent category
     * @param pageable Pagination information
     * @return Page of active subcategories
     */
    Page<Category> findByParentAndActiveTrue(Category parent, Pageable pageable);

    /**
     * Find all subcategories of a given parent category ID.
     * Used for hierarchical category navigation without loading the parent.
     *
     * @param parentId The parent category ID
     * @return List of subcategories
     */
    List<Category> findByParentId(Long parentId);

    /**
     * Find all active subcategories of a given parent category ID.
     * Used for customer-facing category navigation without loading the parent.
     *
     * @param parentId The parent category ID
     * @return List of active subcategories
     */
    List<Category> findByParentIdAndActiveTrue(Long parentId);

    /**
     * Find a category by name.
     * Used for category lookup and validation.
     *
     * @param name The category name
     * @return Optional containing the category if found
     */
    Optional<Category> findByName(String name);

    /**
     * Find a category by name and parent.
     * Used for validating unique subcategory names within a parent.
     *
     * @param name The category name
     * @param parent The parent category
     * @return Optional containing the category if found
     */
    Optional<Category> findByNameAndParent(String name, Category parent);

    /**
     * Check if a category with the given name exists.
     * Used for category creation validation.
     *
     * @param name The category name to check
     * @return true if a category with the name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Check if a category with the given name exists within a parent category.
     * Used for subcategory creation validation.
     *
     * @param name The category name to check
     * @param parent The parent category
     * @return true if a subcategory with the name exists, false otherwise
     */
    boolean existsByNameAndParent(String name, Category parent);

    /**
     * Find all categories ordered by their hierarchical path.
     * Used for displaying the complete category tree.
     *
     * @return List of categories ordered by hierarchy
     */
    @Query("SELECT c FROM Category c ORDER BY " +
           "CASE WHEN c.parent IS NULL THEN 0 ELSE 1 END, " +
           "c.displayOrder, c.name")
    List<Category> findAllOrderByHierarchy();

    /**
     * Find all active categories ordered by their hierarchical path.
     * Used for displaying the complete active category tree.
     *
     * @return List of active categories ordered by hierarchy
     */
    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY " +
           "CASE WHEN c.parent IS NULL THEN 0 ELSE 1 END, " +
           "c.displayOrder, c.name")
    List<Category> findAllActiveOrderByHierarchy();

    /**
     * Search for categories by name with pagination.
     * Used for admin category management with search functionality.
     *
     * @param searchTerm The search term to match against category name
     * @param pageable Pagination information
     * @return Page of categories matching the search criteria
     */
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Category> searchCategories(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count the number of subcategories for a given parent category.
     * Used for category management and statistics.
     *
     * @param parentId The parent category ID
     * @return The number of subcategories
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent.id = :parentId")
    long countByParentId(@Param("parentId") Long parentId);

    /**
     * Count the number of active subcategories for a given parent category.
     * Used for category management and statistics.
     *
     * @param parentId The parent category ID
     * @return The number of active subcategories
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parent.id = :parentId AND c.active = true")
    long countActiveByParentId(@Param("parentId") Long parentId);

    /**
     * Find all categories at a specific level in the hierarchy.
     * Level 0 represents root categories, level 1 their direct children, etc.
     *
     * @param level The hierarchy level
     * @return List of categories at the specified level
     */
    @Query("SELECT c FROM Category c WHERE " +
           "(c.parent IS NULL AND :level = 0) OR " +
           "(c.parent IS NOT NULL AND " +
           "(c.parent.parent IS NULL AND :level = 1) OR " +
           "(c.parent.parent IS NOT NULL AND c.parent.parent.parent IS NULL AND :level = 2) OR " +
           "(c.parent.parent IS NOT NULL AND c.parent.parent.parent IS NOT NULL AND :level = 3))")
    List<Category> findCategoriesByLevel(@Param("level") int level);

    /**
     * Find all active categories at a specific level in the hierarchy.
     * Level 0 represents root categories, level 1 their direct children, etc.
     *
     * @param level The hierarchy level
     * @return List of active categories at the specified level
     */
    @Query("SELECT c FROM Category c WHERE c.active = true AND (" +
           "(c.parent IS NULL AND :level = 0) OR " +
           "(c.parent IS NOT NULL AND " +
           "(c.parent.parent IS NULL AND :level = 1) OR " +
           "(c.parent.parent IS NOT NULL AND c.parent.parent.parent IS NULL AND :level = 2) OR " +
           "(c.parent.parent IS NOT NULL AND c.parent.parent.parent IS NOT NULL AND :level = 3)))")
    List<Category> findActiveCategoriesByLevel(@Param("level") int level);
}