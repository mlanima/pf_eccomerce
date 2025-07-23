package com.horseriding.ecommerce.categories;

import com.horseriding.ecommerce.categories.dtos.requests.CategoryCreateRequest;
import com.horseriding.ecommerce.categories.dtos.requests.CategoryUpdateRequest;
import com.horseriding.ecommerce.categories.dtos.responses.CategoryResponse;
import com.horseriding.ecommerce.categories.dtos.responses.CategoryTreeResponse;
import com.horseriding.ecommerce.exception.ResourceNotFoundException;
import com.horseriding.ecommerce.users.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for category management operations.
 * Handles category creation, update, deletion, and hierarchical management.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    /** Repository for category data access. */
    private final CategoryRepository categoryRepository;

    /** Repository for user data access (for authorization). */
    private final UserRepository userRepository;

    /**
     * Creates a new category.
     *
     * @param request the category creation request
     * @return the created category
     * @throws IllegalArgumentException if category name already exists
     */
    @Transactional
    public CategoryResponse createCategory(final CategoryCreateRequest request) {
        // Check if category name already exists
        if (categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Category name already exists");
        }

        // Create new category
        Category category = new Category(request.getName(), request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder());

        // Set parent category if provided
        if (request.getParentId() != null) {
            Category parent =
                    categoryRepository
                            .findById(request.getParentId())
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Parent category not found"));
            category.setParent(parent);
        }

        // Save category to database
        Category savedCategory = categoryRepository.save(category);

        // Return category response
        return mapToCategoryResponse(savedCategory);
    }

    /**
     * Updates an existing category.
     *
     * @param categoryId the ID of the category to update
     * @param request the category update request
     * @return the updated category
     * @throws ResourceNotFoundException if the category is not found
     * @throws IllegalArgumentException if category name already exists for another category
     */
    @Transactional
    public CategoryResponse updateCategory(
            final Long categoryId, final CategoryUpdateRequest request) {
        // Get category to update
        Category category =
                categoryRepository
                        .findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Check if name is being changed and if it already exists
        if (!category.getName().equals(request.getName())
                && categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Category name already exists");
        }

        // Update category fields
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder());

        // Update parent category if provided and different
        if (request.getParentId() != null) {
            // Prevent setting parent to self
            if (request.getParentId().equals(categoryId)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }

            // Prevent circular references
            if (wouldCreateCircularReference(category, request.getParentId())) {
                throw new IllegalArgumentException(
                        "Cannot create circular reference in category hierarchy");
            }

            Category parent =
                    categoryRepository
                            .findById(request.getParentId())
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Parent category not found"));
            category.setParent(parent);
        } else {
            // If parentId is null, make it a root category
            category.setParent(null);
        }

        // Save updated category
        Category updatedCategory = categoryRepository.save(category);

        // Return category response
        return mapToCategoryResponse(updatedCategory);
    }

    /**
     * Deletes a category.
     *
     * @param categoryId the ID of the category to delete
     * @throws ResourceNotFoundException if the category is not found
     * @throws IllegalArgumentException if the category has subcategories
     */
    @Transactional
    public void deleteCategory(final Long categoryId) {
        // Get category to delete
        Category category =
                categoryRepository
                        .findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Check if category has subcategories
        if (category.hasSubcategories()) {
            throw new IllegalArgumentException(
                    "Cannot delete category with subcategories. Delete subcategories first or"
                            + " reassign them.");
        }

        // Delete category
        categoryRepository.delete(category);
    }

    /**
     * Gets a category by ID.
     *
     * @param categoryId the ID of the category to get
     * @return the category
     * @throws ResourceNotFoundException if the category is not found
     */
    public CategoryResponse getCategoryById(final Long categoryId) {
        Category category =
                categoryRepository
                        .findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        return mapToCategoryResponse(category);
    }

    /**
     * Gets all root categories (categories without a parent).
     *
     * @return list of root categories
     */
    public List<CategoryResponse> getRootCategories() {
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return rootCategories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets all subcategories of a given parent category.
     *
     * @param parentId the ID of the parent category
     * @return list of subcategories
     * @throws ResourceNotFoundException if the parent category is not found
     */
    public List<CategoryResponse> getSubcategories(final Long parentId) {
        Category parent =
                categoryRepository
                        .findById(parentId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Parent category not found"));

        List<Category> subcategories = categoryRepository.findByParent(parent);
        return subcategories.stream().map(this::mapToCategoryResponse).collect(Collectors.toList());
    }

    /**
     * Gets all categories with pagination.
     *
     * @param pageable pagination information
     * @return page of categories
     */
    public Page<CategoryResponse> getAllCategories(final Pageable pageable) {
        Page<Category> categories = categoryRepository.findAll(pageable);
        return categories.map(this::mapToCategoryResponse);
    }

    /**
     * Searches for categories by name with pagination.
     *
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of categories matching the search criteria
     */
    public Page<CategoryResponse> searchCategories(
            final String searchTerm, final Pageable pageable) {
        Page<Category> categories = categoryRepository.searchCategories(searchTerm, pageable);
        return categories.map(this::mapToCategoryResponse);
    }

    /**
     * Gets the complete category tree starting from root categories.
     *
     * @return list of root categories with their subcategory trees
     */
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return rootCategories.stream()
                .map(category -> buildCategoryTree(category, 0))
                .collect(Collectors.toList());
    }

    /**
     * Recursively builds a category tree starting from the given category.
     *
     * @param category the starting category
     * @param level the current level in the hierarchy
     * @return the category tree response
     */
    private CategoryTreeResponse buildCategoryTree(final Category category, final int level) {
        CategoryTreeResponse response = new CategoryTreeResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setParentId(category.getParent() != null ? category.getParent().getId() : null);
        response.setActive(true); // Active status not implemented in entity yet
        response.setDisplayOrder(category.getDisplayOrder());
        response.setLevel(level);
        response.setFullPath(category.getFullPath());
        response.setProductCount(0); // Product count not implemented yet

        // Recursively build subcategory trees
        if (category.hasSubcategories()) {
            List<CategoryTreeResponse> subcategoryTrees =
                    category.getSubcategories().stream()
                            .map(subcategory -> buildCategoryTree(subcategory, level + 1))
                            .collect(Collectors.toList());
            response.setSubcategories(subcategoryTrees);
        } else {
            response.setSubcategories(new ArrayList<>());
        }

        return response;
    }

    /**
     * Maps a Category entity to a CategoryResponse DTO.
     *
     * @param category the category entity
     * @return the category response DTO
     */
    private CategoryResponse mapToCategoryResponse(final Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());

        if (category.getParent() != null) {
            response.setParentId(category.getParent().getId());
            response.setParentName(category.getParent().getName());
        }

        response.setActive(true); // Active status not implemented in entity yet
        response.setDisplayOrder(category.getDisplayOrder());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());

        // Map subcategories (first level only to avoid deep recursion)
        if (category.hasSubcategories()) {
            List<CategoryResponse> subcategoryResponses =
                    category.getSubcategories().stream()
                            .map(
                                    subcategory -> {
                                        CategoryResponse subResponse = new CategoryResponse();
                                        subResponse.setId(subcategory.getId());
                                        subResponse.setName(subcategory.getName());
                                        subResponse.setDescription(subcategory.getDescription());
                                        subResponse.setParentId(category.getId());
                                        subResponse.setParentName(category.getName());
                                        subResponse.setActive(
                                                true); // Active status not implemented in entity
                                        // yet
                                        subResponse.setDisplayOrder(subcategory.getDisplayOrder());
                                        subResponse.setCreatedAt(subcategory.getCreatedAt());
                                        subResponse.setUpdatedAt(subcategory.getUpdatedAt());
                                        subResponse.setSubcategories(
                                                new ArrayList<>()); // Don't go deeper
                                        return subResponse;
                                    })
                            .collect(Collectors.toList());
            response.setSubcategories(subcategoryResponses);
        } else {
            response.setSubcategories(new ArrayList<>());
        }

        return response;
    }

    /**
     * Checks if setting a new parent would create a circular reference in the category hierarchy.
     *
     * @param category the category being updated
     * @param newParentId the ID of the new parent category
     * @return true if it would create a circular reference, false otherwise
     */
    private boolean wouldCreateCircularReference(final Category category, final Long newParentId) {
        // If the new parent is the same as the current parent, no circular reference
        if (category.getParent() != null && category.getParent().getId().equals(newParentId)) {
            return false;
        }

        // Get the new parent category
        Category newParent =
                categoryRepository
                        .findById(newParentId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Parent category not found"));

        // Check if the category is in the ancestry of the new parent
        Category ancestor = newParent.getParent();
        while (ancestor != null) {
            if (ancestor.getId().equals(category.getId())) {
                return true; // Circular reference found
            }
            ancestor = ancestor.getParent();
        }

        return false; // No circular reference
    }
}
