package com.horseriding.ecommerce.auth;

import com.horseriding.ecommerce.users.User;
import com.horseriding.ecommerce.users.UserRole;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * UserPrincipal class that wraps the User entity and implements UserDetails.
 * This class serves as the authenticated user context in Spring Security.
 * It provides a clean separation between the domain model (User) and security concerns.
 */
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "user")
public class UserPrincipal implements UserDetails {

    @EqualsAndHashCode.Include private final User user;

    /**
     * Creates a UserPrincipal from a User entity.
     *
     * @param user the user entity
     * @return UserPrincipal instance
     */
    public static UserPrincipal create(User user) {
        return new UserPrincipal(user);
    }

    /**
     * Gets the wrapped User entity.
     *
     * @return the user entity
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the user's ID.
     *
     * @return the user's ID
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * Gets the user's email.
     *
     * @return the user's email
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * Gets the user's full name.
     *
     * @return the user's full name
     */
    public String getFullName() {
        return user.getFullName();
    }

    /**
     * Gets the user's role.
     *
     * @return the user's role
     */
    public UserRole getRole() {
        return user.getRole();
    }

    /**
     * Checks if the user has administrative privileges.
     *
     * @return true if the user has admin privileges, false otherwise
     */
    public boolean hasAdminPrivileges() {
        return user.hasAdminPrivileges();
    }

    /**
     * Checks if the user is a superadmin.
     *
     * @return true if the user is a superadmin, false otherwise
     */
    public boolean isSuperAdmin() {
        return user.isSuperAdmin();
    }

    /**
     * Checks if the user is a customer.
     *
     * @return true if the user is a customer, false otherwise
     */
    public boolean isCustomer() {
        return user.isCustomer();
    }

    /**
     * Checks if the user is the owner of the specified resource.
     *
     * @param resourceUserId the user ID associated with the resource
     * @return true if the user is the owner, false otherwise
     */
    public boolean isResourceOwner(Long resourceUserId) {
        return user.getId().equals(resourceUserId);
    }

    // UserDetails interface implementation

    /**
     * Returns the authorities granted to the user based on their role.
     * Implements role hierarchy: SUPERADMIN > ADMIN > CUSTOMER
     *
     * @return collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Add base role
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // Implement role hierarchy
        if (user.getRole() == UserRole.SUPERADMIN) {
            // SUPERADMIN also has ADMIN role
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            // All roles have CUSTOMER role
            authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        } else if (user.getRole() == UserRole.ADMIN) {
            // ADMIN also has CUSTOMER role
            authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        }

        return authorities;
    }

    /**
     * Returns the password used to authenticate the user.
     *
     * @return the user's password
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Returns the username used to authenticate the user (email in our case).
     *
     * @return the user's email
     */
    @Override
    public String getUsername() {
        return user.getEmail();
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
