package com.horseriding.ecommerce.categories;

import com.horseriding.ecommerce.categories.dtos.requests.CategoryCreateRequest;
import com.horseriding.ecommerce.categories.dtos.requests.CategoryUpdateRequest;
import com.horseriding.ecommerce.categories.dtos.responses.CategoryResponse;
import com.horseriding.ecommerce.categories.dtos.responses.CategoryTreeResponse;
import com.horseriding.ecommerce.common.dtos.responses.PaginationResponse;
import com.horseriding.ecommerce.common.dtos.responses.SuccessResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for category management endpoints.
 * Handles category CRUD operations and hierarchical management.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    /** Category service for category management operations. */
    private final CategoryService categoryService;

    /**
     * Creates a new category (admin only).
     *
     * @param request the category creation request
     * @return the created category
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse category = categoryService.createCategory(request);
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }

    /**
     * Updates an existing category (admin only).
     *
     * @param categoryId the ID of the category to update
     * @param request the category update request
     * @return the updated category
     */
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long categoryId, @Valid @RequestBody CategoryUpdateRequest request) {
        CategoryResponse category = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(category);
    }

    /**
     * Deletes a category (admin only).
     *
     * @param categoryId the ID of the category to delete
     * @return success response
     */
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<Void>> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(new SuccessResponse<>("Category deleted successfully", 200));
    }

    /**
     * Gets a category by ID.
     *
     * @param categoryId the ID of the category to get
     * @return the category
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long categoryId) {
        CategoryResponse category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }

    /**
     * Gets all root categories (categories without a parent).
     *
     * @return list of root categories
     */
    @GetMapping("/root")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        List<CategoryResponse> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Gets all subcategories of a given parent category.
     *
     * @param parentId the ID of the parent category
     * @return list of subcategories
     */
    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<List<CategoryResponse>> getSubcategories(@PathVariable Long parentId) {
        List<CategoryResponse> subcategories = categoryService.getSubcategories(parentId);
        return ResponseEntity.ok(subcategories);
    }

    /**
     * Gets all categories with pagination.
     *
     * @param page the page number (0-based)
     * @param size the page size
     * @param sortBy the field to sort by
     * @param sortDir the sort direction
     * @return page of categories
     */
    @GetMapping
    public ResponseEntity<PaginationResponse<CategoryResponse>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction direction =
                sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<CategoryResponse> categoriesPage = categoryService.getAllCategories(pageable);

        PaginationResponse<CategoryResponse> response =
                new PaginationResponse<>(
                        categoriesPage.getContent(),
                        categoriesPage.getNumber(),
                        categoriesPage.getSize(),
                        categoriesPage.getTotalElements(),
                        categoriesPage.getTotalPages(),
                        categoriesPage.isFirst(),
                        categoriesPage.isLast());

        return ResponseEntity.ok(response);
    }

    /**
     * Searches for categories by name with pagination.
     *
     * @param searchTerm the search term
     * @param page the page number (0-based)
     * @param size the page size
     * @param sortBy the field to sort by
     * @param sortDir the sort direction
     * @return page of categories matching the search criteria
     */
    @GetMapping("/search")
    public ResponseEntity<PaginationResponse<CategoryResponse>> searchCategories(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction direction =
                sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<CategoryResponse> categoriesPage =
                categoryService.searchCategories(searchTerm, pageable);

        PaginationResponse<CategoryResponse> response =
                new PaginationResponse<>(
                        categoriesPage.getContent(),
                        categoriesPage.getNumber(),
                        categoriesPage.getSize(),
                        categoriesPage.getTotalElements(),
                        categoriesPage.getTotalPages(),
                        categoriesPage.isFirst(),
                        categoriesPage.isLast());

        return ResponseEntity.ok(response);
    }

    /**
     * Gets the complete category tree starting from root categories.
     *
     * @return list of root categories with their subcategory trees
     */
    @GetMapping("/tree")
    public ResponseEntity<List<CategoryTreeResponse>> getCategoryTree() {
        List<CategoryTreeResponse> categoryTree = categoryService.getCategoryTree();
        return ResponseEntity.ok(categoryTree);
    }
}
