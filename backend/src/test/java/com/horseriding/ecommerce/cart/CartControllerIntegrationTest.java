package com.horseriding.ecommerce.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.horseriding.ecommerce.auth.TokenService;
import com.horseriding.ecommerce.auth.UserPrincipal;
import com.horseriding.ecommerce.brands.Brand;
import com.horseriding.ecommerce.brands.BrandRepository;
import com.horseriding.ecommerce.cart.dtos.requests.AddToCartRequest;
import com.horseriding.ecommerce.cart.dtos.requests.UpdateCartItemRequest;
import com.horseriding.ecommerce.categories.Category;
import com.horseriding.ecommerce.categories.CategoryRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CartController.
 * Tests the complete request-response cycle for cart management endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional  // Auto-rollback after each test
@ActiveProfiles("test")
class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private CartService cartService;

    // Helper method to create test user
    private User createTestUser(String email, String password, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setFirstName("Test");
        user.setLastName("User");
        return userRepository.save(user);
    }

    // Helper method to create test category
    private Category createTestCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription("Test category description");
        return categoryRepository.save(category);
    }

    // Helper method to create test brand
    private Brand createTestBrand(String name) {
        Brand brand = new Brand();
        brand.setName(name);
        brand.setDescription("Test brand description");
        return brandRepository.save(brand);
    }

    // Helper method to create test product
    private Product createTestProduct(String name, Category category, Brand brand, BigDecimal price, Integer stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Test product description");
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setCategory(category);
        product.setBrand(brand);
        product.setSku("TEST-SKU-" + System.currentTimeMillis());
        return productRepository.save(product);
    }

    // Helper method to create tokens for a user
    private TokenService.TokenPair createTokensForUser(User user) {
        return tokenService.createTokens(user);
    }

    // Helper method to create cart with items for user using CartService
    private void addItemToUserCart(User user, Product product, Integer quantity) {
        // Create UserPrincipal for the user
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        
        // Temporarily set the security context to the user
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userPrincipal, null, userPrincipal.getAuthorities()));
        
        // Use CartService to add item to cart
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(product.getId());
        request.setQuantity(quantity);
        
        cartService.addToCart(request);
        
        // Clear security context
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    // ========== 6.1 Authenticated Cart Access Tests ==========

    @Test
    void shouldRetrieveUserCartWithItemsAndTotals() throws Exception {
        // Given - Create test user, product, and cart with items
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Brand brand = createTestBrand("Test Brand");
        Product product = createTestProduct("Test Product", category, brand, new BigDecimal("99.99"), 10);
        
        addItemToUserCart(user, product, 2);
        
        TokenService.TokenPair tokens = createTokensForUser(user);

        // When & Then
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productId").value(product.getId()))
                .andExpect(jsonPath("$.items[0].productName").value("Test Product"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(99.99))
                .andExpect(jsonPath("$.items[0].totalPrice").value(199.98))
                .andExpect(jsonPath("$.totalItemCount").value(2))
                .andExpect(jsonPath("$.uniqueItemCount").value(1))
                .andExpect(jsonPath("$.totalAmount").value(199.98));
    }

    @Test
    void shouldRejectCartAccessWithoutAuthentication() throws Exception {
        // When & Then - Access cart without Authorization header
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRetrieveEmptyCartForNewUser() throws Exception {
        // Given - Create test user without any cart items
        User user = createTestUser("newuser@example.com", "password123", UserRole.CUSTOMER);
        TokenService.TokenPair tokens = createTokensForUser(user);

        // When & Then
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalItemCount").value(0))
                .andExpect(jsonPath("$.uniqueItemCount").value(0))
                .andExpect(jsonPath("$.totalAmount").value(0));
    }

    @Test
    void shouldEnsureCartAccessIsolationBetweenUsers() throws Exception {
        // Given - Create two users with separate carts
        User user1 = createTestUser("user1@example.com", "password123", UserRole.CUSTOMER);
        User user2 = createTestUser("user2@example.com", "password123", UserRole.CUSTOMER);
        
        Category category = createTestCategory("Test Category");
        Brand brand = createTestBrand("Test Brand");
        Product product1 = createTestProduct("Product 1", category, brand, new BigDecimal("50.00"), 10);
        Product product2 = createTestProduct("Product 2", category, brand, new BigDecimal("75.00"), 10);
        
        // Create cart items for each user
        addItemToUserCart(user1, product1, 1);
        addItemToUserCart(user2, product2, 2);
        
        TokenService.TokenPair tokens1 = createTokensForUser(user1);
        TokenService.TokenPair tokens2 = createTokensForUser(user2);

        // When & Then - User 1 should only see their cart
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + tokens1.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user1.getId()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").hasJsonPath())
                .andExpect(jsonPath("$.items[0].productId").value(product1.getId()))
                .andExpect(jsonPath("$.items[0].productName").value("Product 1"))
                .andExpect(jsonPath("$.totalItemCount").value(1))
                .andExpect(jsonPath("$.totalAmount").value(50.00));

        // When & Then - User 2 should only see their cart
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + tokens2.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user2.getId()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").hasJsonPath())
                .andExpect(jsonPath("$.items[0].productId").value(product2.getId()))
                .andExpect(jsonPath("$.items[0].productName").value("Product 2"))
                .andExpect(jsonPath("$.totalItemCount").value(2))
                .andExpect(jsonPath("$.totalAmount").value(150.00));
    }

    // ========== 6.2 Cart Item Management Tests ==========

    @Test
    void shouldAddItemToCartWithValidProductAndQuantity() throws Exception {
        // Given - Create test user and product
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Brand brand = createTestBrand("Test Brand");
        Product product = createTestProduct("Test Product", category, brand, new BigDecimal("99.99"), 10);
        
        TokenService.TokenPair tokens = createTokensForUser(user);
        
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(product.getId());
        request.setQuantity(2);

        // When & Then
        mockMvc.perform(post("/api/cart")
                .header("Authorization", "Bearer " + tokens.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productId").value(product.getId()))
                .andExpect(jsonPath("$.items[0].productName").value("Test Product"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(99.99))
                .andExpect(jsonPath("$.items[0].totalPrice").value(199.98))
                .andExpect(jsonPath("$.totalItemCount").value(2))
                .andExpect(jsonPath("$.uniqueItemCount").value(1))
                .andExpect(jsonPath("$.totalAmount").value(199.98));
    }

    @Test
    void shouldRejectAddingNonExistentProductToCart() throws Exception {
        // Given - Create test user
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        TokenService.TokenPair tokens = createTokensForUser(user);
        
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(999999L); // Non-existent product ID
        request.setQuantity(1);

        // When & Then
        mockMvc.perform(post("/api/cart")
                .header("Authorization", "Bearer " + tokens.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectAddingItemWithQuantityExceedingStock() throws Exception {
        // Given - Create test user and product with limited stock
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Brand brand = createTestBrand("Test Brand");
        Product product = createTestProduct("Limited Stock Product", category, brand, new BigDecimal("50.00"), 5); // Only 5 in stock
        
        TokenService.TokenPair tokens = createTokensForUser(user);
        
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(product.getId());
        request.setQuantity(10); // Requesting more than available stock

        // When & Then
        mockMvc.perform(post("/api/cart")
                .header("Authorization", "Bearer " + tokens.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateCartItemQuantity() throws Exception {
        // Given - Create test user, product, and add item to cart
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Brand brand = createTestBrand("Test Brand");
        Product product = createTestProduct("Test Product", category, brand, new BigDecimal("99.99"), 10);
        
        addItemToUserCart(user, product, 2); // Add 2 items initially
        
        TokenService.TokenPair tokens = createTokensForUser(user);
        
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5); // Update to 5 items

        // When & Then
        mockMvc.perform(put("/api/cart/items/" + product.getId())
                .header("Authorization", "Bearer " + tokens.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productId").value(product.getId()))
                .andExpect(jsonPath("$.items[0].quantity").value(5))
                .andExpect(jsonPath("$.items[0].totalPrice").value(499.95))
                .andExpect(jsonPath("$.totalItemCount").value(5))
                .andExpect(jsonPath("$.totalAmount").value(499.95));
    }

    @Test
    void shouldRemoveItemFromCart() throws Exception {
        // Given - Create test user, product, and add item to cart
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Brand brand = createTestBrand("Test Brand");
        Product product = createTestProduct("Test Product", category, brand, new BigDecimal("99.99"), 10);
        
        addItemToUserCart(user, product, 2); // Add 2 items initially
        
        TokenService.TokenPair tokens = createTokensForUser(user);

        // When & Then
        mockMvc.perform(delete("/api/cart/items/" + product.getId())
                .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalItemCount").value(0))
                .andExpect(jsonPath("$.uniqueItemCount").value(0))
                .andExpect(jsonPath("$.totalAmount").value(0));
    }

    // ========== 6.3 Cart Validation Tests ==========

    @Test
    void shouldRejectAddingItemWithInvalidQuantityNegative() throws Exception {
        // Given - Create test user and product
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Brand brand = createTestBrand("Test Brand");
        Product product = createTestProduct("Test Product", category, brand, new BigDecimal("99.99"), 10);
        
        TokenService.TokenPair tokens = createTokensForUser(user);
        
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(product.getId());
        request.setQuantity(-1); // Invalid negative quantity

        // When & Then
        mockMvc.perform(post("/api/cart")
                .header("Authorization", "Bearer " + tokens.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectAddingItemWithInvalidQuantityZero() throws Exception {
        // Given - Create test user and product
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Brand brand = createTestBrand("Test Brand");
        Product product = createTestProduct("Test Product", category, brand, new BigDecimal("99.99"), 10);
        
        TokenService.TokenPair tokens = createTokensForUser(user);
        
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(product.getId());
        request.setQuantity(0); // Invalid zero quantity

        // When & Then
        mockMvc.perform(post("/api/cart")
                .header("Authorization", "Bearer " + tokens.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectAddingOutOfStockProduct() throws Exception {
        // Given - Create test user and out-of-stock product
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Brand brand = createTestBrand("Test Brand");
        Product product = createTestProduct("Out of Stock Product", category, brand, new BigDecimal("99.99"), 0); // 0 stock
        
        TokenService.TokenPair tokens = createTokensForUser(user);
        
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(product.getId());
        request.setQuantity(1);

        // When & Then
        mockMvc.perform(post("/api/cart")
                .header("Authorization", "Bearer " + tokens.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCalculateCartTotalAccurately() throws Exception {
        // Given - Create test user and multiple products
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Brand brand = createTestBrand("Test Brand");
        Product product1 = createTestProduct("Product 1", category, brand, new BigDecimal("25.50"), 10);
        Product product2 = createTestProduct("Product 2", category, brand, new BigDecimal("15.75"), 10);
        
        // Add items to cart using service
        addItemToUserCart(user, product1, 2); // 2 * 25.50 = 51.00
        addItemToUserCart(user, product2, 3); // 3 * 15.75 = 47.25
        // Total should be 51.00 + 47.25 = 98.25
        
        TokenService.TokenPair tokens = createTokensForUser(user);

        // When & Then
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").hasJsonPath())
                .andExpect(jsonPath("$.totalItemCount").value(5)) // 2 + 3 = 5 items
                .andExpect(jsonPath("$.uniqueItemCount").value(2)) // 2 different products
                .andExpect(jsonPath("$.totalAmount").value(98.25)); // 51.00 + 47.25 = 98.25
    }

    @Test
    void shouldValidateCartItemsDuringCheckout() throws Exception {
        // Given - Create test user and product, then add to cart
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Brand brand = createTestBrand("Test Brand");
        Product product = createTestProduct("Test Product", category, brand, new BigDecimal("99.99"), 10);
        
        addItemToUserCart(user, product, 2);
        
        TokenService.TokenPair tokens = createTokensForUser(user);

        // When & Then - Validate cart endpoint should return cart with validation info
        mockMvc.perform(get("/api/cart/validate")
                .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productId").value(product.getId()))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].inStock").value(true))
                .andExpect(jsonPath("$.items[0].quantityAvailable").value(true))
                .andExpect(jsonPath("$.items[0].productActive").value(true))
                .andExpect(jsonPath("$.totalItemCount").value(2))
                .andExpect(jsonPath("$.totalAmount").value(199.98));
    }
}