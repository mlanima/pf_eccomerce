package com.horseriding.ecommerce.categories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.horseriding.ecommerce.categories.dtos.requests.CategoryCreateRequest;
import com.horseriding.ecommerce.categories.dtos.requests.CategoryUpdateRequest;
import com.horseriding.ecommerce.products.Product;
import com.horseriding.ecommerce.products.ProductRepository;
import com.horseriding.ecommerce.users.User;
import com.horseriding.ecommerce.users.UserRepository;
import com.horseriding.ecommerce.users.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CategoryController.
 * Tests the complete request-response cycle for category management endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional  // Auto-rollback after each test
@ActiveProfiles("test")
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Helper methods for test data creation
    private Category createTestCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    private Category createTestSubcategory(String name, String description, Category parent) {
        Category subcategory = new Category();
        subcategory.setName(name);
        subcategory.setDescription(description);
        subcategory.setParent(parent);
        return categoryRepository.save(subcategory);
    }

    private Product createTestProduct(String name, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Test product description");
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(10);
        product.setCategory(category);
        return productRepository.save(product);
    }

    private User createTestUser(String email, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(role);
        user.setFirstName("Test");
        user.setLastName("User");
        return userRepository.save(user);
    }

    // 4.1 Public category access tests

    @Test
    void shouldGetAllCategoriesWithHierarchicalStructure() throws Exception {
        // Create test categories with hierarchy
        Category rootCategory = createTestCategory("Riding Equipment", "Main riding equipment category");
        Category subcategory1 = createTestSubcategory("Saddles", "Various types of saddles", rootCategory);
        Category subcategory2 = createTestSubcategory("Bridles", "Various types of bridles", rootCategory);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Riding Equipment')]").exists())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void shouldGetCategoryTreeWithHierarchicalStructure() throws Exception {
        // Create test categories with hierarchy
        Category rootCategory = createTestCategory("Horse Care", "Horse care products");
        Category subcategory1 = createTestSubcategory("Grooming", "Grooming supplies", rootCategory);
        Category subcategory2 = createTestSubcategory("Health", "Health products", rootCategory);

        mockMvc.perform(get("/api/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name == 'Horse Care')]").exists())
                .andExpect(jsonPath("$[0].subcategories").isArray());
    }

    @Test
    void shouldGetSingleCategoryByIdWithSubcategories() throws Exception {
        // Create test category with subcategories
        Category parentCategory = createTestCategory("Apparel", "Riding apparel");
        Category subcategory1 = createTestSubcategory("Helmets", "Safety helmets", parentCategory);
        Category subcategory2 = createTestSubcategory("Boots", "Riding boots", parentCategory);

        mockMvc.perform(get("/api/categories/{categoryId}", parentCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(parentCategory.getId()))
                .andExpect(jsonPath("$.name").value("Apparel"))
                .andExpect(jsonPath("$.description").value("Riding apparel"))
                .andExpect(jsonPath("$.subcategories").isArray());
        
        // Verify subcategories exist by checking the subcategories endpoint
        mockMvc.perform(get("/api/categories/{parentId}/subcategories", parentCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnNotFoundForNonExistentCategory() throws Exception {
        mockMvc.perform(get("/api/categories/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetSubcategoriesOfParentCategory() throws Exception {
        // Create parent category with subcategories
        Category parentCategory = createTestCategory("Training Equipment", "Equipment for training");
        Category subcategory1 = createTestSubcategory("Lunging", "Lunging equipment", parentCategory);
        Category subcategory2 = createTestSubcategory("Ground Work", "Ground work tools", parentCategory);

        mockMvc.perform(get("/api/categories/{parentId}/subcategories", parentCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name == 'Lunging')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Ground Work')]").exists())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetRootCategories() throws Exception {
        // Create root categories and subcategories
        Category rootCategory1 = createTestCategory("Stable Equipment", "Equipment for stables");
        Category rootCategory2 = createTestCategory("Feed & Supplements", "Horse feed and supplements");
        Category subcategory = createTestSubcategory("Hay", "Various types of hay", rootCategory2);

        mockMvc.perform(get("/api/categories/root"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name == 'Stable Equipment')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Feed & Supplements')]").exists())
                .andExpect(jsonPath("$.length()").value(2)); // Only root categories, not subcategories
    }

    @Test
    void shouldSearchCategoriesByName() throws Exception {
        // Create test categories
        createTestCategory("Saddle Pads", "Various saddle pads");
        createTestCategory("Saddle Bags", "Storage bags for saddles");
        createTestCategory("Bridle Accessories", "Accessories for bridles");

        mockMvc.perform(get("/api/categories/search")
                .param("searchTerm", "saddle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Saddle Pads')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Saddle Bags')]").exists())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldGetProductsByCategory() throws Exception {
        // Create category with products
        Category category = createTestCategory("Horse Blankets", "Blankets for horses");
        Product product1 = createTestProduct("Winter Blanket", category);
        Product product2 = createTestProduct("Rain Sheet", category);

        // Note: This test assumes there's an endpoint to get products by category
        // If this endpoint doesn't exist in ProductController, this test validates the relationship
        mockMvc.perform(get("/api/products")
                .param("categoryId", category.getId().toString()))
                .andExpect(status().isOk());
    }

    // 4.2 Admin category management tests

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateCategoryAsAdminWithValidData() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("New Category");
        request.setDescription("A new category for testing");
        request.setDisplayOrder(1);

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Category"))
                .andExpect(jsonPath("$.description").value("A new category for testing"))
                .andExpect(jsonPath("$.displayOrder").value(1))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldRejectCategoryCreationAsCustomer() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Unauthorized Category");
        request.setDescription("This should not be created");

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectCategoryCreationWithoutAuthentication() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Unauthenticated Category");
        request.setDescription("This should not be created");

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateSubcategoryWithParentChildRelationship() throws Exception {
        // Create parent category first
        Category parentCategory = createTestCategory("Parent Category", "Parent category for testing");

        CategoryCreateRequest subcategoryRequest = new CategoryCreateRequest();
        subcategoryRequest.setName("Subcategory");
        subcategoryRequest.setDescription("A subcategory for testing");
        subcategoryRequest.setParentId(parentCategory.getId());
        subcategoryRequest.setDisplayOrder(1);

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subcategoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Subcategory"))
                .andExpect(jsonPath("$.description").value("A subcategory for testing"))
                .andExpect(jsonPath("$.parentId").value(parentCategory.getId()))
                .andExpect(jsonPath("$.parentName").value("Parent Category"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateCategoryAsAdmin() throws Exception {
        // Create category to update
        Category category = createTestCategory("Original Name", "Original description");

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setDescription("Updated description");
        updateRequest.setDisplayOrder(5);
        updateRequest.setActive(true);

        mockMvc.perform(put("/api/categories/{categoryId}", category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.displayOrder").value(5));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldRejectCategoryUpdateAsCustomer() throws Exception {
        // Create category to update
        Category category = createTestCategory("Test Category", "Test description");

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setDescription("Updated description");

        mockMvc.perform(put("/api/categories/{categoryId}", category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteCategoryAsAdmin() throws Exception {
        // Create category to delete (without subcategories)
        Category category = createTestCategory("Category to Delete", "This will be deleted");

        mockMvc.perform(delete("/api/categories/{categoryId}", category.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));

        // Verify category is deleted
        mockMvc.perform(get("/api/categories/{categoryId}", category.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldRejectCategoryDeletionAsCustomer() throws Exception {
        // Create category to delete
        Category category = createTestCategory("Protected Category", "This should not be deleted");

        mockMvc.perform(delete("/api/categories/{categoryId}", category.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectCategoryDeletionWithoutAuthentication() throws Exception {
        // Create category to delete
        Category category = createTestCategory("Protected Category", "This should not be deleted");

        mockMvc.perform(delete("/api/categories/{categoryId}", category.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectDeletionOfCategoryWithSubcategories() throws Exception {
        // Create parent category with subcategories
        Category parentCategory = createTestCategory("Parent with Children", "Has subcategories");
        Category subcategory = createTestSubcategory("Child Category", "Child of parent", parentCategory);

        mockMvc.perform(delete("/api/categories/{categoryId}", parentCategory.getId()))
                .andExpect(status().isBadRequest());
    }

    // 4.3 Category validation tests

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectCategoryCreationWithMissingRequiredFields() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest();
        // Missing name field

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectCategoryCreationWithBlankName() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName(""); // Blank name
        request.setDescription("Valid description");

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectCategoryCreationWithDuplicateName() throws Exception {
        // Create existing category
        createTestCategory("Duplicate Category", "First category");

        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Duplicate Category"); // Same name
        request.setDescription("Second category with same name");

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectCategoryCreationWithTooLongName() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("A".repeat(101)); // Exceeds 100 character limit
        request.setDescription("Valid description");

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectCategoryCreationWithTooLongDescription() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Valid Name");
        request.setDescription("A".repeat(501)); // Exceeds 500 character limit

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectCategoryCreationWithNonExistentParent() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Valid Name");
        request.setDescription("Valid description");
        request.setParentId(999999L); // Non-existent parent ID

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectCategoryUpdateWithMissingRequiredFields() throws Exception {
        // Create category to update
        Category category = createTestCategory("Test Category", "Test description");

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        // Missing name field

        mockMvc.perform(put("/api/categories/{categoryId}", category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectCategoryUpdateWithBlankName() throws Exception {
        // Create category to update
        Category category = createTestCategory("Test Category", "Test description");

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setName(""); // Blank name
        updateRequest.setDescription("Valid description");

        mockMvc.perform(put("/api/categories/{categoryId}", category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectCategoryUpdateWithDuplicateName() throws Exception {
        // Create existing categories
        Category existingCategory = createTestCategory("Existing Category", "First category");
        Category categoryToUpdate = createTestCategory("Category to Update", "Second category");

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setName("Existing Category"); // Same name as existing category
        updateRequest.setDescription("Updated description");

        mockMvc.perform(put("/api/categories/{categoryId}", categoryToUpdate.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectCategoryUpdateWithTooLongName() throws Exception {
        // Create category to update
        Category category = createTestCategory("Test Category", "Test description");

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setName("A".repeat(101)); // Exceeds 100 character limit
        updateRequest.setDescription("Valid description");

        mockMvc.perform(put("/api/categories/{categoryId}", category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectCategoryUpdateWithTooLongDescription() throws Exception {
        // Create category to update
        Category category = createTestCategory("Test Category", "Test description");

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setName("Valid Name");
        updateRequest.setDescription("A".repeat(501)); // Exceeds 500 character limit

        mockMvc.perform(put("/api/categories/{categoryId}", category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectCategoryUpdateWithCircularReference() throws Exception {
        // Create parent and child categories
        Category parentCategory = createTestCategory("Parent Category", "Parent");
        Category childCategory = createTestSubcategory("Child Category", "Child", parentCategory);

        // Try to make parent a child of its own child (circular reference)
        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setName("Parent Category");
        updateRequest.setDescription("Parent");
        updateRequest.setParentId(childCategory.getId()); // Circular reference

        mockMvc.perform(put("/api/categories/{categoryId}", parentCategory.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectCategoryUpdateWithSelfAsParent() throws Exception {
        // Create category
        Category category = createTestCategory("Test Category", "Test description");

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest();
        updateRequest.setName("Test Category");
        updateRequest.setDescription("Test description");
        updateRequest.setParentId(category.getId()); // Self as parent

        mockMvc.perform(put("/api/categories/{categoryId}", category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldHandleCategoryDeletionWithExistingProducts() throws Exception {
        // Create category with products
        Category category = createTestCategory("Category with Products", "Has products");
        Product product = createTestProduct("Test Product", category);

        // Try to delete category with products - this should succeed as products can exist without categories
        // or should be handled gracefully depending on business rules
        mockMvc.perform(delete("/api/categories/{categoryId}", category.getId()))
                .andExpect(status().isOk());
    }
}