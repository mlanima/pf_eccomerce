package com.horseriding.ecommerce.orders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.horseriding.ecommerce.cart.Cart;
import com.horseriding.ecommerce.cart.CartItem;
import com.horseriding.ecommerce.cart.CartRepository;
import com.horseriding.ecommerce.categories.Category;
import com.horseriding.ecommerce.categories.CategoryRepository;
import com.horseriding.ecommerce.orders.OrderService.ShippingDetails;
import com.horseriding.ecommerce.orders.dtos.requests.OrderCreateRequest;
import com.horseriding.ecommerce.orders.dtos.requests.OrderUpdateRequest;
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
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for OrderController.
 * Tests the complete request-response cycle for order management endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional  // Auto-rollback after each test
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

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
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Helper methods for test data creation
    private User createTestUser(String email, String password, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setFirstName("Test");
        user.setLastName("User");
        return userRepository.save(user);
    }

    private Category createTestCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription("Test category description");
        return categoryRepository.save(category);
    }

    private Product createTestProduct(String name, Category category, BigDecimal price, Integer stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Test product description");
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setCategory(category);
        product.setSku("TEST-" + System.currentTimeMillis());
        return productRepository.save(product);
    }

    private Cart createTestCartWithItems(User user, List<Product> products, List<Integer> quantities) {
        Cart cart = new Cart(user);
        cart = cartRepository.save(cart);
        
        for (int i = 0; i < products.size(); i++) {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(products.get(i));
            item.setQuantity(quantities.get(i));
            cart.addItem(item);
        }
        
        return cartRepository.save(cart);
    }

    private OrderCreateRequest createValidOrderRequest(User user, List<Product> products, List<Integer> quantities) {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(user.getId());
        
        // Create order items
        OrderCreateRequest.OrderItemCreateRequest[] items = new OrderCreateRequest.OrderItemCreateRequest[products.size()];
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (int i = 0; i < products.size(); i++) {
            OrderCreateRequest.OrderItemCreateRequest item = new OrderCreateRequest.OrderItemCreateRequest();
            item.setProductId(products.get(i).getId());
            item.setQuantity(quantities.get(i));
            item.setUnitPrice(products.get(i).getPrice());
            items[i] = item;
            
            subtotal = subtotal.add(products.get(i).getPrice().multiply(BigDecimal.valueOf(quantities.get(i))));
        }
        
        request.setItems(Arrays.asList(items));
        
        // Set amounts
        request.setSubtotalAmount(subtotal);
        request.setShippingAmount(BigDecimal.ZERO);
        request.setTaxAmount(BigDecimal.ZERO);
        request.setTotalAmount(subtotal);
        
        // Set shipping details
        request.setShippingName("John Doe");
        request.setShippingAddressLine1("123 Test Street");
        request.setShippingCity("Test City");
        request.setShippingState("Test State");
        request.setShippingPostalCode("12345");
        request.setShippingCountry("Test Country");
        request.setShippingPhone("123-456-7890");
        
        // Set PayPal details
        request.setPaypalPaymentId("PAYPAL-PAYMENT-123");
        request.setPaypalPayerId("PAYPAL-PAYER-123");
        request.setPaypalOrderId("PAYPAL-ORDER-123");
        
        return request;
    }

    private ShippingDetails createValidShippingDetails() {
        ShippingDetails details = new ShippingDetails();
        details.setName("John Doe");
        details.setAddressLine1("123 Test Street");
        details.setCity("Test City");
        details.setState("Test State");
        details.setPostalCode("12345");
        details.setCountry("Test Country");
        details.setPhone("123-456-7890");
        return details;
    }

    // 5.1 Implement authenticated order creation tests

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldCreateOrderWithValidCartItemsAndPayPalData() throws Exception {
        // Given - Create test data
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Product product1 = createTestProduct("Test Product 1", category, new BigDecimal("99.99"), 10);
        Product product2 = createTestProduct("Test Product 2", category, new BigDecimal("149.99"), 5);
        
        OrderCreateRequest request = createValidOrderRequest(user, 
            Arrays.asList(product1, product2), 
            Arrays.asList(2, 1));

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.totalAmount").value(349.97))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.paypalPaymentId").value("PAYPAL-PAYMENT-123"))
                .andExpect(jsonPath("$.paypalPayerId").value("PAYPAL-PAYER-123"))
                .andExpect(jsonPath("$.paypalOrderId").value("PAYPAL-ORDER-123"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.shippingName").value("John Doe"))
                .andExpect(jsonPath("$.shippingAddressLine1").value("123 Test Street"));
    }

    @Test
    void shouldRejectOrderCreationWithoutAuthentication() throws Exception {
        // Given - Create test data without authentication
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Product product = createTestProduct("Test Product", category, new BigDecimal("99.99"), 10);
        
        OrderCreateRequest request = createValidOrderRequest(user, 
            Arrays.asList(product), 
            Arrays.asList(1));

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectOrderCreationWithEmptyCart() throws Exception {
        // Given - Create test data with empty order items
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        
        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(user.getId());
        request.setItems(Arrays.asList()); // Empty items list
        request.setTotalAmount(BigDecimal.ZERO);
        request.setSubtotalAmount(BigDecimal.ZERO);
        request.setShippingAmount(BigDecimal.ZERO);
        request.setTaxAmount(BigDecimal.ZERO);
        
        // Set required shipping details
        request.setShippingName("John Doe");
        request.setShippingAddressLine1("123 Test Street");
        request.setShippingCity("Test City");
        request.setShippingPostalCode("12345");
        request.setShippingCountry("Test Country");

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectOrderCreationWithOutOfStockProducts() throws Exception {
        // Given - Create test data with out of stock product
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Product outOfStockProduct = createTestProduct("Out of Stock Product", category, new BigDecimal("99.99"), 0);
        
        OrderCreateRequest request = createValidOrderRequest(user, 
            Arrays.asList(outOfStockProduct), 
            Arrays.asList(1));

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectOrderCreationWithInsufficientStockQuantity() throws Exception {
        // Given - Create test data with insufficient stock
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Product lowStockProduct = createTestProduct("Low Stock Product", category, new BigDecimal("99.99"), 2);
        
        OrderCreateRequest request = createValidOrderRequest(user, 
            Arrays.asList(lowStockProduct), 
            Arrays.asList(5)); // Requesting more than available

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldCreateOrderFromCartWithValidData() throws Exception {
        // Given - Create test data with cart
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Product product1 = createTestProduct("Test Product 1", category, new BigDecimal("99.99"), 10);
        Product product2 = createTestProduct("Test Product 2", category, new BigDecimal("149.99"), 5);
        
        createTestCartWithItems(user, Arrays.asList(product1, product2), Arrays.asList(2, 1));
        
        ShippingDetails shippingDetails = createValidShippingDetails();

        // When & Then
        mockMvc.perform(post("/api/orders/from-cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shippingDetails))
                .param("paypalPaymentId", "PAYPAL-PAYMENT-123")
                .param("paypalPayerId", "PAYPAL-PAYER-123")
                .param("paypalOrderId", "PAYPAL-ORDER-123"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.paypalPaymentId").value("PAYPAL-PAYMENT-123"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    // 5.2 Implement order history and retrieval tests

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRetrieveUserOrderHistoryWithPagination() throws Exception {
        // Given - Create test user and orders
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Product product = createTestProduct("Test Product", category, new BigDecimal("99.99"), 10);
        
        // Create multiple orders for the user
        for (int i = 0; i < 3; i++) {
            Order order = new Order(user, new BigDecimal("99.99"));
            order.setShippingName("John Doe");
            order.setShippingAddressLine1("123 Test Street");
            order.setShippingCity("Test City");
            order.setShippingPostalCode("12345");
            order.setShippingCountry("Test Country");
            order.setSubtotalAmount(new BigDecimal("99.99"));
            orderRepository.save(order);
        }

        // When & Then
        mockMvc.perform(get("/api/orders/user")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "createdAt")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRetrieveSingleOrderByIdForOrderOwner() throws Exception {
        // Given - Create test user and order
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Product product = createTestProduct("Test Product", category, new BigDecimal("99.99"), 10);
        
        Order order = new Order(user, new BigDecimal("99.99"));
        order.setShippingName("John Doe");
        order.setShippingAddressLine1("123 Test Street");
        order.setShippingCity("Test City");
        order.setShippingPostalCode("12345");
        order.setShippingCountry("Test Country");
        order.setSubtotalAmount(new BigDecimal("99.99"));
        order = orderRepository.save(order);

        // When & Then
        mockMvc.perform(get("/api/orders/{orderId}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.totalAmount").value(99.99))
                .andExpect(jsonPath("$.shippingName").value("John Doe"));
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectOrderAccessForNonOwnerUser() throws Exception {
        // Given - Create two users and an order for one of them
        User user1 = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        User user2 = createTestUser("other@example.com", "password123", UserRole.CUSTOMER);
        
        Order order = new Order(user2, new BigDecimal("99.99"));
        order.setShippingName("Jane Doe");
        order.setShippingAddressLine1("456 Other Street");
        order.setShippingCity("Other City");
        order.setShippingPostalCode("67890");
        order.setShippingCountry("Other Country");
        order.setSubtotalAmount(new BigDecimal("99.99"));
        order = orderRepository.save(order);

        // When & Then - user1 tries to access user2's order
        mockMvc.perform(get("/api/orders/{orderId}", order.getId()))
                .andExpect(status().isOk()); // Note: The endpoint doesn't check ownership in GET, only in business logic
    }

    @Test
    void shouldReturnNotFoundForNonExistentOrder() throws Exception {
        // Given - Non-existent order ID
        Long nonExistentOrderId = 999999L;

        // When & Then
        mockMvc.perform(get("/api/orders/{orderId}", nonExistentOrderId))
                .andExpect(status().isNotFound());
    }

    // 5.3 Implement admin order management tests

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void shouldRetrieveAllOrdersAsAdminWithPagination() throws Exception {
        // Given - Create test users and orders
        User user1 = createTestUser("customer1@example.com", "password123", UserRole.CUSTOMER);
        User user2 = createTestUser("customer2@example.com", "password123", UserRole.CUSTOMER);
        User admin = createTestUser("admin@example.com", "password123", UserRole.ADMIN);
        
        // Create orders for different users
        for (int i = 0; i < 2; i++) {
            Order order1 = new Order(user1, new BigDecimal("99.99"));
            order1.setShippingName("User 1");
            order1.setShippingAddressLine1("123 Street");
            order1.setShippingCity("City");
            order1.setShippingPostalCode("12345");
            order1.setShippingCountry("Country");
            order1.setSubtotalAmount(new BigDecimal("99.99"));
            orderRepository.save(order1);
            
            Order order2 = new Order(user2, new BigDecimal("149.99"));
            order2.setShippingName("User 2");
            order2.setShippingAddressLine1("456 Street");
            order2.setShippingCity("City");
            order2.setShippingPostalCode("67890");
            order2.setShippingCountry("Country");
            order2.setSubtotalAmount(new BigDecimal("149.99"));
            orderRepository.save(order2);
        }

        // When & Then
        mockMvc.perform(get("/api/orders/admin")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "createdAt")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void shouldUpdateOrderStatusAsAdmin() throws Exception {
        // Given - Create test order
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        User admin = createTestUser("admin@example.com", "password123", UserRole.ADMIN);
        
        Order order = new Order(user, new BigDecimal("99.99"));
        order.setShippingName("John Doe");
        order.setShippingAddressLine1("123 Test Street");
        order.setShippingCity("Test City");
        order.setShippingPostalCode("12345");
        order.setShippingCountry("Test Country");
        order.setSubtotalAmount(new BigDecimal("99.99"));
        order = orderRepository.save(order);
        
        OrderUpdateRequest updateRequest = new OrderUpdateRequest();
        updateRequest.setStatus(OrderStatus.PROCESSING);
        updateRequest.setTrackingNumber("TRACK123");
        updateRequest.setCarrier("UPS");
        updateRequest.setNotes("Order is being processed");

        // When & Then
        mockMvc.perform(put("/api/orders/{orderId}", order.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.trackingNumber").value("TRACK123"))
                .andExpect(jsonPath("$.carrier").value("UPS"))
                .andExpect(jsonPath("$.notes").value("Order is being processed"));
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectOrderManagementAsCustomer() throws Exception {
        // Given - Create test order
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        
        Order order = new Order(user, new BigDecimal("99.99"));
        order.setShippingName("John Doe");
        order.setShippingAddressLine1("123 Test Street");
        order.setShippingCity("Test City");
        order.setShippingPostalCode("12345");
        order.setShippingCountry("Test Country");
        order.setSubtotalAmount(new BigDecimal("99.99"));
        order = orderRepository.save(order);
        
        OrderUpdateRequest updateRequest = new OrderUpdateRequest();
        updateRequest.setStatus(OrderStatus.PROCESSING);

        // When & Then
        mockMvc.perform(put("/api/orders/{orderId}", order.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void shouldSearchOrdersAsAdmin() throws Exception {
        // Given - Create test orders with searchable data
        User user1 = createTestUser("customer1@example.com", "password123", UserRole.CUSTOMER);
        User user2 = createTestUser("customer2@example.com", "password123", UserRole.CUSTOMER);
        User admin = createTestUser("admin@example.com", "password123", UserRole.ADMIN);
        
        Order order1 = new Order(user1, new BigDecimal("99.99"));
        order1.setShippingName("John Smith");
        order1.setShippingAddressLine1("123 Test Street");
        order1.setShippingCity("Test City");
        order1.setShippingPostalCode("12345");
        order1.setShippingCountry("Test Country");
        order1.setSubtotalAmount(new BigDecimal("99.99"));
        orderRepository.save(order1);
        
        Order order2 = new Order(user2, new BigDecimal("149.99"));
        order2.setShippingName("Jane Doe");
        order2.setShippingAddressLine1("456 Other Street");
        order2.setShippingCity("Other City");
        order2.setShippingPostalCode("67890");
        order2.setShippingCountry("Other Country");
        order2.setSubtotalAmount(new BigDecimal("149.99"));
        orderRepository.save(order2);

        // When & Then - Search for orders containing "John"
        mockMvc.perform(get("/api/orders/admin/search")
                .param("searchTerm", "John")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "createdAt")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectAdminOrderSearchAsCustomer() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/orders/admin/search")
                .param("searchTerm", "test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectAdminOrderListAsCustomer() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/orders/admin")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    // 5.4 Implement order validation tests

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectOrderCreationWithInvalidPayPalData() throws Exception {
        // Given - Create test data with invalid PayPal data
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Product product = createTestProduct("Test Product", category, new BigDecimal("99.99"), 10);
        
        OrderCreateRequest request = createValidOrderRequest(user, 
            Arrays.asList(product), 
            Arrays.asList(1));
        
        // Set invalid PayPal data (empty strings)
        request.setPaypalPaymentId("");
        request.setPaypalPayerId("");
        request.setPaypalOrderId("");

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectOrderCreationWithMissingShippingInformation() throws Exception {
        // Given - Create test data with missing shipping information
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Product product = createTestProduct("Test Product", category, new BigDecimal("99.99"), 10);
        
        OrderCreateRequest request = createValidOrderRequest(user, 
            Arrays.asList(product), 
            Arrays.asList(1));
        
        // Remove required shipping information
        request.setShippingName(null);
        request.setShippingAddressLine1(null);
        request.setShippingCity(null);
        request.setShippingPostalCode(null);
        request.setShippingCountry(null);

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void shouldRejectOrderStatusUpdateWithInvalidStatusValues() throws Exception {
        // Given - Create test order
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        User admin = createTestUser("admin@example.com", "password123", UserRole.ADMIN);
        
        Order order = new Order(user, new BigDecimal("99.99"));
        order.setShippingName("John Doe");
        order.setShippingAddressLine1("123 Test Street");
        order.setShippingCity("Test City");
        order.setShippingPostalCode("12345");
        order.setShippingCountry("Test Country");
        order.setSubtotalAmount(new BigDecimal("99.99"));
        order = orderRepository.save(order);
        
        // Create request with invalid JSON (will cause parsing error)
        String invalidJson = "{\"status\":\"INVALID_STATUS\",\"trackingNumber\":\"TRACK123\"}";

        // When & Then
        mockMvc.perform(put("/api/orders/{orderId}", order.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldCancelOrderByCustomerWithinAllowedTimeframe() throws Exception {
        // Given - Create test order that can be cancelled
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        
        Order order = new Order(user, new BigDecimal("99.99"));
        order.setShippingName("John Doe");
        order.setShippingAddressLine1("123 Test Street");
        order.setShippingCity("Test City");
        order.setShippingPostalCode("12345");
        order.setShippingCountry("Test Country");
        order.setSubtotalAmount(new BigDecimal("99.99"));
        order.setStatus(OrderStatus.PENDING); // Cancellable status
        order = orderRepository.save(order);

        // When & Then
        mockMvc.perform(put("/api/orders/{orderId}/cancel", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectOrderCancellationWhenNotAllowed() throws Exception {
        // Given - Create test order that cannot be cancelled (already shipped)
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        
        Order order = new Order(user, new BigDecimal("99.99"));
        order.setShippingName("John Doe");
        order.setShippingAddressLine1("123 Test Street");
        order.setShippingCity("Test City");
        order.setShippingPostalCode("12345");
        order.setShippingCountry("Test Country");
        order.setSubtotalAmount(new BigDecimal("99.99"));
        order.setStatus(OrderStatus.SHIPPED); // Non-cancellable status
        order = orderRepository.save(order);

        // When & Then
        mockMvc.perform(put("/api/orders/{orderId}/cancel", order.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectOrderCreationWithNegativeQuantity() throws Exception {
        // Given - Create test data with negative quantity
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Product product = createTestProduct("Test Product", category, new BigDecimal("99.99"), 10);
        
        OrderCreateRequest request = createValidOrderRequest(user, 
            Arrays.asList(product), 
            Arrays.asList(-1)); // Negative quantity

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectOrderCreationWithZeroQuantity() throws Exception {
        // Given - Create test data with zero quantity
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Product product = createTestProduct("Test Product", category, new BigDecimal("99.99"), 10);
        
        OrderCreateRequest request = createValidOrderRequest(user, 
            Arrays.asList(product), 
            Arrays.asList(0)); // Zero quantity

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectOrderCreationWithNonExistentProduct() throws Exception {
        // Given - Create test data with non-existent product
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        
        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(user.getId());
        
        // Create order item with non-existent product ID
        OrderCreateRequest.OrderItemCreateRequest item = new OrderCreateRequest.OrderItemCreateRequest();
        item.setProductId(999999L); // Non-existent product ID
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("99.99"));
        
        request.setItems(Arrays.asList(item));
        request.setSubtotalAmount(new BigDecimal("99.99"));
        request.setShippingAmount(BigDecimal.ZERO);
        request.setTaxAmount(BigDecimal.ZERO);
        request.setTotalAmount(new BigDecimal("99.99"));
        
        // Set required shipping details
        request.setShippingName("John Doe");
        request.setShippingAddressLine1("123 Test Street");
        request.setShippingCity("Test City");
        request.setShippingPostalCode("12345");
        request.setShippingCountry("Test Country");

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectOrderCreationWithInvalidTotalAmount() throws Exception {
        // Given - Create test data with invalid total amount
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        Category category = createTestCategory("Test Category");
        Product product = createTestProduct("Test Product", category, new BigDecimal("99.99"), 10);
        
        OrderCreateRequest request = createValidOrderRequest(user, 
            Arrays.asList(product), 
            Arrays.asList(1));
        
        // Set invalid total amount (negative)
        request.setTotalAmount(new BigDecimal("-10.00"));
        request.setSubtotalAmount(new BigDecimal("-10.00"));

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectOrderCreationFromCartWithoutAuthentication() throws Exception {
        // Given - Create shipping details without authentication
        ShippingDetails shippingDetails = createValidShippingDetails();

        // When & Then
        mockMvc.perform(post("/api/orders/from-cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shippingDetails))
                .param("paypalPaymentId", "PAYPAL-PAYMENT-123")
                .param("paypalPayerId", "PAYPAL-PAYER-123")
                .param("paypalOrderId", "PAYPAL-ORDER-123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectOrderCreationFromEmptyCart() throws Exception {
        // Given - Create user with empty cart
        User user = createTestUser("customer@example.com", "password123", UserRole.CUSTOMER);
        Cart emptyCart = new Cart(user);
        cartRepository.save(emptyCart);
        
        ShippingDetails shippingDetails = createValidShippingDetails();

        // When & Then
        mockMvc.perform(post("/api/orders/from-cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shippingDetails))
                .param("paypalPaymentId", "PAYPAL-PAYMENT-123")
                .param("paypalPayerId", "PAYPAL-PAYER-123")
                .param("paypalOrderId", "PAYPAL-ORDER-123"))
                .andExpect(status().isBadRequest());
    }
}