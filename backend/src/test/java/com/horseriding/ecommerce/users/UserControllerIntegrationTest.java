package com.horseriding.ecommerce.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.horseriding.ecommerce.users.dtos.requests.UserRegistrationRequest;
import com.horseriding.ecommerce.users.dtos.requests.UserUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController.
 * Tests user profile management and admin user operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    // Task 7.1: User profile management tests

    @Test
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void shouldGetUserProfile() throws Exception {
        // Create test user
        createTestUser("test@example.com", UserRole.CUSTOMER);

        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void shouldRejectProfileAccessWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void shouldUpdateUserProfileWithValidData() throws Exception {
        // Create test user
        createTestUser("test@example.com", UserRole.CUSTOMER);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("updated@example.com");
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setPhoneNumber("123-456-7890");

        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"))
                .andExpect(jsonPath("$.phoneNumber").value("123-456-7890"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void shouldRejectProfileUpdateWithInvalidEmail() throws Exception {
        // Create test user
        createTestUser("test@example.com", UserRole.CUSTOMER);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("invalid-email");
        request.setFirstName("Updated");
        request.setLastName("Name");

        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void shouldRejectProfileUpdateWithMissingFields() throws Exception {
        // Create test user
        createTestUser("test@example.com", UserRole.CUSTOMER);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("test@example.com");
        // Missing firstName and lastName

        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void shouldRejectProfileUpdateWithoutAuthentication() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("test@example.com");
        request.setFirstName("Test");
        request.setLastName("User");

        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // Task 7.2: Admin user management tests (superadmin only)

    @Test
    @WithMockUser(username = "superadmin@example.com", roles = "SUPERADMIN")
    void shouldCreateAdminUserAsSuperadmin() throws Exception {
        // Create superadmin user
        createTestUser("superadmin@example.com", UserRole.SUPERADMIN);

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("admin@example.com");
        request.setPassword("SecurePass123!");
        request.setFirstName("Admin");
        request.setLastName("User");

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void shouldRejectAdminUserCreationAsAdmin() throws Exception {
        // Create admin user
        createTestUser("admin@example.com", UserRole.ADMIN);

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("newadmin@example.com");
        request.setPassword("SecurePass123!");
        request.setFirstName("New");
        request.setLastName("Admin");

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectAdminUserCreationAsCustomer() throws Exception {
        // Create customer user
        createTestUser("customer@example.com", UserRole.CUSTOMER);

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("admin@example.com");
        request.setPassword("SecurePass123!");
        request.setFirstName("Admin");
        request.setLastName("User");

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "superadmin@example.com", roles = "SUPERADMIN")
    void shouldGetAllAdminUsersAsSuperadmin() throws Exception {
        // Create superadmin user
        createTestUser("superadmin@example.com", UserRole.SUPERADMIN);
        // Create some admin users
        createTestUser("admin1@example.com", UserRole.ADMIN);
        createTestUser("admin2@example.com", UserRole.ADMIN);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void shouldRejectGetAllAdminUsersAsAdmin() throws Exception {
        // Create admin user
        createTestUser("admin@example.com", UserRole.ADMIN);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "superadmin@example.com", roles = "SUPERADMIN")
    void shouldDeleteAdminUserAsSuperadmin() throws Exception {
        // Create superadmin user
        createTestUser("superadmin@example.com", UserRole.SUPERADMIN);
        // Create admin user to delete
        User adminUser = createTestUser("admin@example.com", UserRole.ADMIN);

        mockMvc.perform(delete("/api/admin/users/" + adminUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Admin user deleted successfully"));
    }

    @Test
    @WithMockUser(username = "superadmin@example.com", roles = "SUPERADMIN")
    void shouldUpdateAdminUserRoleAsSuperadmin() throws Exception {
        // Create superadmin user
        createTestUser("superadmin@example.com", UserRole.SUPERADMIN);
        // Create admin user to update
        User adminUser = createTestUser("admin@example.com", UserRole.ADMIN);

        mockMvc.perform(put("/api/admin/users/" + adminUser.getId() + "/role")
                .param("role", "SUPERADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SUPERADMIN"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void shouldSearchUsersAsAdmin() throws Exception {
        // Create admin user
        createTestUser("admin@example.com", UserRole.ADMIN);
        // Create some test users
        createTestUser("john@example.com", UserRole.CUSTOMER);
        createTestUser("jane@example.com", UserRole.CUSTOMER);

        mockMvc.perform(get("/api/admin/users/search")
                .param("searchTerm", "john")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void shouldRejectUserSearchAsCustomer() throws Exception {
        // Create customer user
        createTestUser("customer@example.com", UserRole.CUSTOMER);

        mockMvc.perform(get("/api/admin/users/search"))
                .andExpect(status().isForbidden());
    }

    // Task 7.3: User validation tests

    @Test
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void shouldRejectProfileUpdateWithDuplicateEmail() throws Exception {
        // Create test users
        createTestUser("test@example.com", UserRole.CUSTOMER);
        createTestUser("existing@example.com", UserRole.CUSTOMER);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("existing@example.com"); // Duplicate email
        request.setFirstName("Test");
        request.setLastName("User");

        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "superadmin@example.com", roles = "SUPERADMIN")
    void shouldRejectAdminUserCreationWithInvalidRole() throws Exception {
        // Create superadmin user
        createTestUser("superadmin@example.com", UserRole.SUPERADMIN);

        // Create request with invalid data (this would be handled by validation)
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("admin@example.com");
        request.setPassword("short"); // Invalid password
        request.setFirstName("Admin");
        request.setLastName("User");

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(username = "superadmin@example.com", roles = "SUPERADMIN")
    void shouldRejectUserRoleUpdateWithInvalidRole() throws Exception {
        // Create superadmin user
        createTestUser("superadmin@example.com", UserRole.SUPERADMIN);
        // Create admin user to update
        User adminUser = createTestUser("admin@example.com", UserRole.ADMIN);

        mockMvc.perform(put("/api/admin/users/" + adminUser.getId() + "/role")
                .param("role", "INVALID_ROLE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "superadmin@example.com", roles = "SUPERADMIN")
    void shouldRejectAdminUserCreationWithDuplicateEmail() throws Exception {
        // Create superadmin user
        createTestUser("superadmin@example.com", UserRole.SUPERADMIN);
        // Create existing user
        createTestUser("existing@example.com", UserRole.ADMIN);

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("existing@example.com"); // Duplicate email
        request.setPassword("SecurePass123!");
        request.setFirstName("Admin");
        request.setLastName("User");

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "superadmin@example.com", roles = "SUPERADMIN")
    void shouldReturnNotFoundForNonExistentUser() throws Exception {
        // Create superadmin user
        createTestUser("superadmin@example.com", UserRole.SUPERADMIN);

        mockMvc.perform(delete("/api/admin/users/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "superadmin@example.com", roles = "SUPERADMIN")
    void shouldReturnNotFoundForNonExistentUserRoleUpdate() throws Exception {
        // Create superadmin user
        createTestUser("superadmin@example.com", UserRole.SUPERADMIN);

        mockMvc.perform(put("/api/admin/users/999999/role")
                .param("role", "ADMIN"))
                .andExpect(status().isNotFound());
    }
}