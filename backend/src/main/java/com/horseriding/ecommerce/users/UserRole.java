package com.horseriding.ecommerce.users;

/**
 * Enumeration defining the different user roles in the system.
 * CUSTOMER - Regular customers who can browse and purchase products.
 * ADMIN - Administrators who can manage products, orders, and inventory.
 * SUPERADMIN - Super administrators who can manage admin users and system
 * configuration.
 */
public enum UserRole {
    /** Regular customers who can browse and purchase products. */
    CUSTOMER,
    /** Administrators who can manage products, orders, and inventory. */
    ADMIN,
    /** Super administrators who can manage admin users and system config. */
    SUPERADMIN
}
