package com.horseriding.ecommerce.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * User entity representing customers and administrators in the system.
 * Supports role-based access control with CUSTOMER, ADMIN, and SUPERADMIN roles.
 */
@Entity
@Table(
        name = "users",
        indexes = {
            @Index(name = "idx_user_email", columnList = "email"),
            @Index(name = "idx_user_role", columnList = "role")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "password")
public final class User implements UserDetails {

    /** Minimum password length constant. */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /** Unique identifier for the user. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** User's email address, used for authentication. */
    @Column(unique = true, nullable = false)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    /** User's encrypted password. */
    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = MIN_PASSWORD_LENGTH, message = "Password must be at least 8 characters long")
    private String password;

    /** User's first name. */
    @Column(name = "first_name")
    @NotBlank(message = "First name is required")
    private String firstName;

    /** User's last name. */
    @Column(name = "last_name")
    @NotBlank(message = "Last name is required")
    private String lastName;

    /** User's phone number (optional). */
    @Column(name = "phone_number")
    private String phoneNumber;

    /** User's role in the system. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "User role is required")
    private UserRole role = UserRole.CUSTOMER;

    /** Timestamp when the user was created. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the user was last updated. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating new users.
     *
     * @param email the user's email address
     * @param password the user's password
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param role the user's role in the system
     */
    public User(
            final String email,
            final String password,
            final String firstName,
            final String lastName,
            final UserRole role) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role != null ? role : UserRole.CUSTOMER;
    }

    /**
     * JPA lifecycle callback executed before persisting the entity.
     * Sets the creation and update timestamps.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback executed before updating the entity.
     * Updates the last modified timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Returns the user's full name by combining first and last name.
     *
     * @return the user's full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Checks if the user has the CUSTOMER role.
     *
     * @return true if the user is a customer, false otherwise
     */
    public boolean isCustomer() {
        return role == UserRole.CUSTOMER;
    }

    /**
     * Checks if the user has the ADMIN role.
     *
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    /**
     * Checks if the user has the SUPERADMIN role.
     *
     * @return true if the user is a superadmin, false otherwise
     */
    public boolean isSuperAdmin() {
        return role == UserRole.SUPERADMIN;
    }

    /**
     * Checks if the user has administrative privileges.
     *
     * @return true if the user is an admin or superadmin, false otherwise
     */
    public boolean hasAdminPrivileges() {
        return role == UserRole.ADMIN || role == UserRole.SUPERADMIN;
    }

    // UserDetails interface implementation

    /**
     * Returns the authorities granted to the user based on their role.
     *
     * @return collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Returns the password used to authenticate the user.
     *
     * @return the user's password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate the user (email in our case).
     *
     * @return the user's email
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indicates whether the user's account has expired.
     *
     * @return true if the user's account is valid (non-expired), false otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // We don't implement account expiration
    }

    /**
     * Indicates whether the user is locked or unlocked.
     *
     * @return true if the user is not locked, false otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return true; // We don't implement account locking
    }

    /**
     * Indicates whether the user's credentials (password) has expired.
     *
     * @return true if the user's credentials are valid (non-expired), false otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // We don't implement credential expiration
    }

    /**
     * Indicates whether the user is enabled or disabled.
     *
     * @return true if the user is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return true; // We don't implement user disabling yet
    }
}
