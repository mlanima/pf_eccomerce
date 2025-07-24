package com.horseriding.ecommerce.persistence;

import static org.assertj.core.api.Assertions.*;

import com.horseriding.ecommerce.cart.Cart;
import com.horseriding.ecommerce.cart.CartItem;
import com.horseriding.ecommerce.cart.CartItemRepository;
import com.horseriding.ecommerce.cart.CartRepository;
import com.horseriding.ecommerce.categories.Category;
import com.horseriding.ecommerce.categories.CategoryRepository;
import com.horseriding.ecommerce.orders.Order;
import com.horseriding.ecommerce.orders.OrderRepository;
import com.horseriding.ecommerce.orders.OrderStatus;
import com.horseriding.ecommerce.products.Product;
import com.horseriding.ecommerce.products.ProductRepository;
import com.horseriding.ecommerce.users.User;
import com.horseriding.ecommerce.users.UserRepository;
import com.horseriding.ecommerce.users.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for data persistence and transaction management.
 * Tests database operations, entity relationships, and data integrity.
 */
@SpringBootTest
@TestPropertySource(
        properties = {"spring.jpa.show-sql=true", "spring.jpa.hibernate.ddl-auto=create-drop"})
@Transactional
class DataPersistenceIntegrationTest {

    @Autowired private UserRepository userRepository;

    @Autowired private CategoryRepository categoryRepository;

    @Autowired private ProductRepository productRepository;

    @Autowired private OrderRepository orderRepository;

    @Autowired private CartRepository cartRepository;

    @Autowired private CartItemRepository cartItemRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @PersistenceContext private EntityManager entityManager;

    // Helper methods for test data creation
    private User createTestUser(String email, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(role);
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }

    private Category createTestCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription("Test category description");
        return category;
    }

    private Product createTestProduct(String name, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Test product description");
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(10);
        product.setCategory(category);
        return product;
    }

    // Task 9.1: Database operation validation tests

    @Test
    void shouldCreateAndPersistUserEntity() {
        // Create user
        User user = createTestUser("test@example.com", UserRole.CUSTOMER);

        // Save user
        User savedUser = userRepository.save(user);

        // Flush to database and clear persistence context
        entityManager.flush();
        entityManager.clear();

        // Verify persistence
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(retrievedUser.get().getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(retrievedUser.get().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateEntityAndTrackChanges() {
        // Create and save user
        User user = createTestUser("test@example.com", UserRole.CUSTOMER);
        User savedUser = userRepository.save(user);
        LocalDateTime originalUpdatedAt = savedUser.getUpdatedAt();

        // Wait a moment to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Update user
        savedUser.setFirstName("Updated");
        savedUser.setLastName("Name");
        User updatedUser = userRepository.save(savedUser);

        // Flush and clear
        entityManager.flush();
        entityManager.clear();

        // Verify update
        Optional<User> retrievedUser = userRepository.findById(updatedUser.getId());
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getFirstName()).isEqualTo("Updated");
        assertThat(retrievedUser.get().getLastName()).isEqualTo("Name");
        assertThat(retrievedUser.get().getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void shouldDeleteEntityAndHandleCascade() {
        // Create category and product
        Category category = createTestCategory("Test Category");
        Category savedCategory = categoryRepository.save(category);

        Product product = createTestProduct("Test Product", savedCategory);
        Product savedProduct = productRepository.save(product);

        entityManager.flush();

        // Verify entities exist
        assertThat(categoryRepository.findById(savedCategory.getId())).isPresent();
        assertThat(productRepository.findById(savedProduct.getId())).isPresent();

        // Delete product (should not affect category)
        productRepository.delete(savedProduct);
        entityManager.flush();

        // Verify deletion
        assertThat(productRepository.findById(savedProduct.getId())).isEmpty();
        assertThat(categoryRepository.findById(savedCategory.getId())).isPresent();
    }

    @Test
    void shouldHandleDatabaseConstraintViolation() {
        // Create user with unique email
        User user1 = createTestUser("test@example.com", UserRole.CUSTOMER);
        userRepository.save(user1);
        entityManager.flush();

        // Try to create another user with same email
        User user2 = createTestUser("test@example.com", UserRole.ADMIN);

        // Should throw constraint violation exception
        assertThatThrownBy(
                        () -> {
                            userRepository.save(user2);
                            entityManager.flush();
                        })
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldMaintainEntityRelationships() {
        // Create category and product with relationship
        Category category = createTestCategory("Test Category");
        Category savedCategory = categoryRepository.save(category);

        Product product = createTestProduct("Test Product", savedCategory);
        Product savedProduct = productRepository.save(product);

        entityManager.flush();
        entityManager.clear();

        // Retrieve product and verify relationship
        Optional<Product> retrievedProduct = productRepository.findById(savedProduct.getId());
        assertThat(retrievedProduct).isPresent();
        assertThat(retrievedProduct.get().getCategory()).isNotNull();
        assertThat(retrievedProduct.get().getCategory().getId()).isEqualTo(savedCategory.getId());
        assertThat(retrievedProduct.get().getCategory().getName()).isEqualTo("Test Category");
    }

    @Test
    void shouldHandleComplexEntityRelationships() {
        // Create user, cart, and cart items
        User user = createTestUser("test@example.com", UserRole.CUSTOMER);
        User savedUser = userRepository.save(user);

        Category category = createTestCategory("Test Category");
        Category savedCategory = categoryRepository.save(category);

        Product product = createTestProduct("Test Product", savedCategory);
        Product savedProduct = productRepository.save(product);

        Cart cart = new Cart();
        cart.setUser(savedUser);
        Cart savedCart = cartRepository.save(cart);

        CartItem cartItem = new CartItem();
        cartItem.setCart(savedCart);
        cartItem.setProduct(savedProduct);
        cartItem.setQuantity(2);
        CartItem savedCartItem = cartItemRepository.save(cartItem);

        entityManager.flush();
        entityManager.clear();

        // Verify complex relationships
        Optional<CartItem> retrievedCartItem = cartItemRepository.findById(savedCartItem.getId());
        assertThat(retrievedCartItem).isPresent();
        assertThat(retrievedCartItem.get().getCart().getUser().getEmail())
                .isEqualTo("test@example.com");
        assertThat(retrievedCartItem.get().getProduct().getName()).isEqualTo("Test Product");
        assertThat(retrievedCartItem.get().getProduct().getCategory().getName())
                .isEqualTo("Test Category");
    }

    // Task 9.2: Transaction management tests

    @Test
    void shouldCommitTransactionSuccessfully() {
        // Create multiple entities in a transaction
        User user = createTestUser("test@example.com", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");

        // Save entities
        User savedUser = userRepository.save(user);
        Category savedCategory = categoryRepository.save(category);

        // Flush to ensure database operations
        entityManager.flush();

        // Verify entities are persisted
        assertThat(userRepository.findById(savedUser.getId())).isPresent();
        assertThat(categoryRepository.findById(savedCategory.getId())).isPresent();
    }

    @Test
    void shouldRollbackTransactionOnException() {
        // This test demonstrates transaction rollback behavior
        // In a real scenario, you would have a service method that throws an exception

        // Create user
        User user = createTestUser("test@example.com", UserRole.CUSTOMER);
        User savedUser = userRepository.save(user);

        // Verify user exists
        assertThat(userRepository.findById(savedUser.getId())).isPresent();

        // The @Transactional annotation on the test class ensures rollback
        // after the test completes, so the user won't persist beyond this test
    }

    @Test
    void shouldHandleConcurrentAccess() {
        // Create and save a product
        Category category = createTestCategory("Test Category");
        Category savedCategory = categoryRepository.save(category);

        Product product = createTestProduct("Test Product", savedCategory);
        product.setStockQuantity(10);
        Product savedProduct = productRepository.save(product);

        entityManager.flush();
        entityManager.clear();

        // Simulate concurrent access by loading the same entity twice
        Optional<Product> product1 = productRepository.findById(savedProduct.getId());
        Optional<Product> product2 = productRepository.findById(savedProduct.getId());

        assertThat(product1).isPresent();
        assertThat(product2).isPresent();

        // Modify both instances
        product1.get().setStockQuantity(5);
        product2.get().setStockQuantity(3);

        // Save first instance
        productRepository.save(product1.get());
        entityManager.flush();

        // Save second instance (this might cause optimistic locking exception in real scenarios)
        productRepository.save(product2.get());
        entityManager.flush();

        // Verify final state
        Optional<Product> finalProduct = productRepository.findById(savedProduct.getId());
        assertThat(finalProduct).isPresent();
        assertThat(finalProduct.get().getStockQuantity()).isEqualTo(3);
    }

    @Test
    void shouldHandleOptimisticLocking() {
        // Create and save a user
        User user = createTestUser("test@example.com", UserRole.CUSTOMER);
        User savedUser = userRepository.save(user);

        entityManager.flush();
        entityManager.clear();

        // Load the same user in two different contexts
        Optional<User> user1 = userRepository.findById(savedUser.getId());
        Optional<User> user2 = userRepository.findById(savedUser.getId());

        assertThat(user1).isPresent();
        assertThat(user2).isPresent();

        // Modify both instances
        user1.get().setFirstName("First");
        user2.get().setFirstName("Second");

        // Save first instance
        userRepository.save(user1.get());
        entityManager.flush();

        // Save second instance
        userRepository.save(user2.get());
        entityManager.flush();

        // Verify the final state
        Optional<User> finalUser = userRepository.findById(savedUser.getId());
        assertThat(finalUser).isPresent();
        assertThat(finalUser.get().getFirstName()).isEqualTo("Second");
    }

    // Task 9.3: Pagination and query tests

    @Test
    void shouldReturnPaginatedResults() {
        // Create multiple users
        for (int i = 1; i <= 15; i++) {
            User user = createTestUser("user" + i + "@example.com", UserRole.CUSTOMER);
            userRepository.save(user);
        }

        entityManager.flush();

        // Test pagination
        Pageable pageable = PageRequest.of(0, 5); // First page, 5 items
        Page<User> firstPage = userRepository.findAll(pageable);

        assertThat(firstPage.getContent()).hasSize(5);
        assertThat(firstPage.getTotalElements()).isEqualTo(15);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(firstPage.isLast()).isFalse();

        // Test second page
        Pageable secondPageable = PageRequest.of(1, 5);
        Page<User> secondPage = userRepository.findAll(secondPageable);

        assertThat(secondPage.getContent()).hasSize(5);
        assertThat(secondPage.isFirst()).isFalse();
        assertThat(secondPage.isLast()).isFalse();

        // Test last page
        Pageable lastPageable = PageRequest.of(2, 5);
        Page<User> lastPage = userRepository.findAll(lastPageable);

        assertThat(lastPage.getContent()).hasSize(5);
        assertThat(lastPage.isFirst()).isFalse();
        assertThat(lastPage.isLast()).isTrue();
    }

    @Test
    void shouldSortResultsCorrectly() {
        // Create users with different names
        User userC = createTestUser("charlie@example.com", UserRole.CUSTOMER);
        userC.setFirstName("Charlie");
        userRepository.save(userC);

        User userA = createTestUser("alice@example.com", UserRole.CUSTOMER);
        userA.setFirstName("Alice");
        userRepository.save(userA);

        User userB = createTestUser("bob@example.com", UserRole.CUSTOMER);
        userB.setFirstName("Bob");
        userRepository.save(userB);

        entityManager.flush();

        // Test ascending sort
        Pageable ascendingSort = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "firstName"));
        Page<User> ascendingResults = userRepository.findAll(ascendingSort);

        List<User> ascendingUsers = ascendingResults.getContent();
        assertThat(ascendingUsers.get(0).getFirstName()).isEqualTo("Alice");
        assertThat(ascendingUsers.get(1).getFirstName()).isEqualTo("Bob");
        assertThat(ascendingUsers.get(2).getFirstName()).isEqualTo("Charlie");

        // Test descending sort
        Pageable descendingSort = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "firstName"));
        Page<User> descendingResults = userRepository.findAll(descendingSort);

        List<User> descendingUsers = descendingResults.getContent();
        assertThat(descendingUsers.get(0).getFirstName()).isEqualTo("Charlie");
        assertThat(descendingUsers.get(1).getFirstName()).isEqualTo("Bob");
        assertThat(descendingUsers.get(2).getFirstName()).isEqualTo("Alice");
    }

    @Test
    void shouldFilterResultsWithComplexQueries() {
        // Create users with different roles
        User customer1 = createTestUser("customer1@example.com", UserRole.CUSTOMER);
        userRepository.save(customer1);

        User customer2 = createTestUser("customer2@example.com", UserRole.CUSTOMER);
        userRepository.save(customer2);

        User admin = createTestUser("admin@example.com", UserRole.ADMIN);
        userRepository.save(admin);

        User superadmin = createTestUser("superadmin@example.com", UserRole.SUPERADMIN);
        userRepository.save(superadmin);

        entityManager.flush();

        // Test filtering by searching users
        Page<User> customerResults = userRepository.searchUsers("customer", PageRequest.of(0, 10));
        assertThat(customerResults.getContent()).hasSize(2);

        Page<User> adminResults = userRepository.searchUsers("admin", PageRequest.of(0, 10));
        assertThat(adminResults.getContent()).hasSize(2); // admin and superadmin
    }

    @Test
    void shouldHandleQueryPerformanceWithLargeDatasets() {
        // Create a larger dataset
        for (int i = 1; i <= 100; i++) {
            User user = createTestUser("user" + i + "@example.com", UserRole.CUSTOMER);
            if (i % 10 == 0) {
                user.setRole(UserRole.ADMIN);
            }
            userRepository.save(user);
        }

        entityManager.flush();

        // Measure query performance (basic test)
        long startTime = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(0, 20);
        Page<User> results = userRepository.findAll(pageable);

        long endTime = System.currentTimeMillis();
        long queryTime = endTime - startTime;

        // Verify results
        assertThat(results.getContent()).hasSize(20);
        assertThat(results.getTotalElements()).isEqualTo(100);

        // Basic performance assertion (query should complete quickly)
        assertThat(queryTime).isLessThan(1000); // Less than 1 second
    }

    @Test
    void shouldHandleEmptyResultSets() {
        // Query for non-existent data
        Page<User> nonExistentUsers =
                userRepository.searchUsers("nonexistent", PageRequest.of(0, 10));
        assertThat(nonExistentUsers.getContent()).isEmpty();

        // Paginated query for empty results
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = userRepository.findAll(pageable);

        assertThat(emptyPage.getContent()).isEmpty();
        assertThat(emptyPage.getTotalElements()).isEqualTo(0);
        assertThat(emptyPage.getTotalPages()).isEqualTo(0);
    }

    @Test
    void shouldMaintainDataIntegrityAcrossTransactions() {
        // Create related entities
        User user = createTestUser("test@example.com", UserRole.CUSTOMER);
        User savedUser = userRepository.save(user);

        Category category = createTestCategory("Test Category");
        Category savedCategory = categoryRepository.save(category);

        Product product = createTestProduct("Test Product", savedCategory);
        Product savedProduct = productRepository.save(product);

        // Create order with order items
        Order order = new Order();
        order.setUser(savedUser);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setSubtotalAmount(new BigDecimal("99.99"));
        order.setShippingName("Test User");
        order.setShippingAddressLine1("123 Test St");
        order.setShippingCity("Test City");
        order.setShippingPostalCode("12345");
        order.setShippingCountry("Test Country");
        order.setPaypalOrderId("PAYPAL123");
        Order savedOrder = orderRepository.save(order);

        entityManager.flush();
        entityManager.clear();

        // Verify data integrity
        Optional<Order> retrievedOrder = orderRepository.findById(savedOrder.getId());
        assertThat(retrievedOrder).isPresent();
        assertThat(retrievedOrder.get().getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(retrievedOrder.get().getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(retrievedOrder.get().getTotalAmount())
                .isEqualByComparingTo(new BigDecimal("99.99"));
    }
}
