package com.horseriding.ecommerce.users;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.horseriding.ecommerce.auth.UserPrincipal;
import com.horseriding.ecommerce.users.dtos.requests.UserRegistrationRequest;
import com.horseriding.ecommerce.users.dtos.requests.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for UserController.
 * Tests user profile management and admin user operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    // Helper methods for test data creation
    private User createTestUser(String email, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(role);
        user.setFirstName("Test");
        user.setLastName("User");
        return userRepository.save(user);
    }

    // Helper method to authenticate a user in the security context
    private void authenticateUser(User user) {
        // Refresh user from database to ensure it's properly managed
        User refreshedUser = userRepository.findById(user.getId()).orElse(user);
        UserPrincipal userPrincipal = new UserPrincipal(refreshedUser);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // Task 7.1: User profile management tests

    @Test
    void shouldGetUserProfile() throws Exception {
        // Create and authenticate test user
        User testUser = createTestUser("test@example.com", UserRole.CUSTOMER);
        authenticateUser(testUser);

        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void shouldRejectProfileAccessWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/profile")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateUserProfileWithValidData() throws Exception {
        // Create and authenticate test user
        User testUser = createTestUser("test@example.com", UserRole.CUSTOMER);
        authenticateUser(testUser);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("test@example.com"); // Keep same email to avoid conflicts
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setPhoneNumber("123-456-7890");

        mockMvc.perform(
                        put("/api/users/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.phoneNumber").value("123-456-7890"));
    }

    @Test
    void shouldUpdateUserProfileWithNewEmail() throws Exception {
        // Create and authenticate test user
        User testUser = createTestUser("test@example.com", UserRole.CUSTOMER);
        authenticateUser(testUser);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("newemail@example.com"); // Change email
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setPhoneNumber("123-456-7890");

        mockMvc.perform(
                        put("/api/users/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newemail@example.com"))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.phoneNumber").value("123-456-7890"));
    }

    @Test
    void shouldRejectProfileUpdateWithInvalidEmail() throws Exception {
        // Create and authenticate test user
        User testUser = createTestUser("test@example.com", UserRole.CUSTOMER);
        authenticateUser(testUser);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("invalid-email");
        request.setFirstName("Updated");
        request.setLastName("Name");

        mockMvc.perform(
                        put("/api/users/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectProfileUpdateWithMissingFields() throws Exception {
        // Create and authenticate test user
        User testUser = createTestUser("test@example.com", UserRole.CUSTOMER);
        authenticateUser(testUser);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("test@example.com");
        // Missing firstName and lastName

        mockMvc.perform(
                        put("/api/users/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectProfileUpdateWithoutAuthentication() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("test@example.com");
        request.setFirstName("Test");
        request.setLastName("User");

        mockMvc.perform(
                        put("/api/users/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // Task 7.2: Admin user management tests (superadmin only)

    @Test
    void shouldCreateAdminUserAsSuperadmin() throws Exception {
        // Create and authenticate superadmin user
        User superadminUser = createTestUser("superadmin@example.com", UserRole.SUPERADMIN);
        authenticateUser(superadminUser);

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("admin@example.com");
        request.setPassword("SecurePass123!");
        request.setFirstName("Admin");
        request.setLastName("User");

        mockMvc.perform(
                        post("/api/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void shouldRejectAdminUserCreationAsAdmin() throws Exception {
        // Create and authenticate admin user
        User adminUser = createTestUser("admin@example.com", UserRole.ADMIN);
        authenticateUser(adminUser);

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("newadmin@example.com");
        request.setPassword("SecurePass123!");
        request.setFirstName("New");
        request.setLastName("Admin");

        mockMvc.perform(
                        post("/api/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectAdminUserCreationAsCustomer() throws Exception {
        // Create and authenticate customer user
        User customerUser = createTestUser("customer@example.com", UserRole.CUSTOMER);
        authenticateUser(customerUser);

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("admin@example.com");
        request.setPassword("SecurePass123!");
        request.setFirstName("Admin");
        request.setLastName("User");

        mockMvc.perform(
                        post("/api/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetAllAdminUsersAsSuperadmin() throws Exception {
        // Create and authenticate superadmin user
        User superadminUser = createTestUser("superadmin@example.com", UserRole.SUPERADMIN);
        authenticateUser(superadminUser);

        // Create some admin users
        createTestUser("admin1@example.com", UserRole.ADMIN);
        createTestUser("admin2@example.com", UserRole.ADMIN);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3)); // 2 admins + 1 superadmin
    }

    @Test
    void shouldRejectGetAllAdminUsersAsAdmin() throws Exception {
        // Create and authenticate admin user
        User adminUser = createTestUser("admin@example.com", UserRole.ADMIN);
        authenticateUser(adminUser);

        mockMvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());
    }

    @Test
    void shouldDeleteAdminUserAsSuperadmin() throws Exception {
        // Create and authenticate superadmin user
        User superadminUser = createTestUser("superadmin@example.com", UserRole.SUPERADMIN);
        authenticateUser(superadminUser);

        // Create admin user to delete
        User adminUser = createTestUser("admin@example.com", UserRole.ADMIN);

        mockMvc.perform(delete("/api/admin/users/" + adminUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Admin user deleted successfully"));
    }

    @Test
    void shouldSearchUsersAsSuperadmin() throws Exception {
        // Create and authenticate superadmin user (to verify the endpoint works)
        User superadminUser = createTestUser("superadmin@example.com", UserRole.SUPERADMIN);
        authenticateUser(superadminUser);

        // Create some test users
        createTestUser("john@example.com", UserRole.CUSTOMER);
        createTestUser("jane@example.com", UserRole.CUSTOMER);

        mockMvc.perform(
                        get("/api/admin/users/search")
                                .param("searchTerm", "john")
                                .param("page", "0")
                                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists());
    }

    @Test
    void shouldRejectUserSearchAsCustomer() throws Exception {
        // Create and authenticate customer user
        User customerUser = createTestUser("customer@example.com", UserRole.CUSTOMER);
        authenticateUser(customerUser);

        mockMvc.perform(get("/api/admin/users/search")).andExpect(status().isForbidden());
    }

    // Task 7.3: User validation tests

    @Test
    void shouldRejectProfileUpdateWithDuplicateEmail() throws Exception {
        // Create test users
        User testUser = createTestUser("test@example.com", UserRole.CUSTOMER);
        createTestUser("existing@example.com", UserRole.CUSTOMER);
        authenticateUser(testUser);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("existing@example.com"); // Duplicate email
        request.setFirstName("Test");
        request.setLastName("User");

        mockMvc.perform(
                        put("/api/users/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectAdminUserCreationWithInvalidPassword() throws Exception {
        // Create and authenticate superadmin user
        User superadminUser = createTestUser("superadmin@example.com", UserRole.SUPERADMIN);
        authenticateUser(superadminUser);

        // Create request with invalid password
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("admin@example.com");
        request.setPassword("short"); // Invalid password (too short)
        request.setFirstName("Admin");
        request.setLastName("User");

        mockMvc.perform(
                        post("/api/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectAdminUserCreationWithDuplicateEmail() throws Exception {
        // Create and authenticate superadmin user
        User superadminUser = createTestUser("superadmin@example.com", UserRole.SUPERADMIN);
        authenticateUser(superadminUser);

        // Create existing user
        createTestUser("existing@example.com", UserRole.ADMIN);

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("existing@example.com"); // Duplicate email
        request.setPassword("SecurePass123!");
        request.setFirstName("Admin");
        request.setLastName("User");

        mockMvc.perform(
                        post("/api/admin/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundForNonExistentUser() throws Exception {
        // Create and authenticate superadmin user
        User superadminUser = createTestUser("superadmin@example.com", UserRole.SUPERADMIN);
        authenticateUser(superadminUser);

        mockMvc.perform(delete("/api/admin/users/999999")).andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectDeleteAdminUserAsAdmin() throws Exception {
        // Create and authenticate admin user
        User adminUser = createTestUser("admin@example.com", UserRole.ADMIN);
        authenticateUser(adminUser);

        // Create another admin user
        User otherAdmin = createTestUser("other@example.com", UserRole.ADMIN);

        mockMvc.perform(delete("/api/admin/users/" + otherAdmin.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectDeleteSuperadminUser() throws Exception {
        // Create and authenticate superadmin user
        User superadminUser = createTestUser("superadmin@example.com", UserRole.SUPERADMIN);
        authenticateUser(superadminUser);

        // Create another superadmin user
        User otherSuperadmin = createTestUser("other-superadmin@example.com", UserRole.SUPERADMIN);

        mockMvc.perform(delete("/api/admin/users/" + otherSuperadmin.getId()))
                .andExpect(status().isBadRequest());
    }
}
