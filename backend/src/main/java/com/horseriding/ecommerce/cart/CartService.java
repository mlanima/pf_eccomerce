package com.horseriding.ecommerce.cart;

import com.horseriding.ecommerce.cart.dtos.requests.AddToCartRequest;
import com.horseriding.ecommerce.cart.dtos.responses.CartItemResponse;
import com.horseriding.ecommerce.cart.dtos.responses.CartResponse;
import com.horseriding.ecommerce.exception.ResourceNotFoundException;
import com.horseriding.ecommerce.products.Product;
import com.horseriding.ecommerce.products.ProductRepository;
import com.horseriding.ecommerce.users.User;
import com.horseriding.ecommerce.users.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for shopping cart operations.
 * Handles cart creation, item management, and validation.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    /** Repository for cart data access. */
    private final CartRepository cartRepository;

    /** Repository for cart item data access. */
    private final CartItemRepository cartItemRepository;

    /** Repository for product data access. */
    private final ProductRepository productRepository;

    /** Repository for user data access. */
    private final UserRepository userRepository;

    /** Maximum age of cart in days before it's considered expired. */
    @Value("${app.cart.max-age-days:30}")
    private int cartMaxAgeDays;

    /**
     * Gets or creates a cart for a user.
     *
     * @param userId the ID of the user
     * @return the user's cart
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional
    public Cart getOrCreateCart(final Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart(user);
            return cartRepository.save(newCart);
        });
    }

    /**
     * Gets a user's cart.
     *
     * @param userId the ID of the user
     * @return the cart response with items and totals
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public CartResponse getUserCart(final Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user).orElse(new Cart(user));

        return mapToCartResponse(cart);
    }

    /**
     * Adds an item to the cart.
     *
     * @param userId the ID of the user
     * @param request the add to cart request
     * @return the updated cart response
     * @throws ResourceNotFoundException if the user or product is not found
     * @throws IllegalArgumentException if the product is out of stock or quantity exceeds available stock
     */
    @Transactional
    public CartResponse addToCart(final Long userId, final AddToCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check if product is in stock
        if (!product.isInStock()) {
            throw new IllegalArgumentException("Product is out of stock");
        }

        // Check if requested quantity is available
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException(
                    "Requested quantity exceeds available stock: " + product.getStockQuantity());
        }

        // Get or create cart
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart(user);
            return cartRepository.save(newCart);
        });

        // Check if product is already in cart
        CartItem existingItem = cart.findItemByProductId(product.getId());
        if (existingItem != null) {
            // Check if new total quantity is available
            int newTotalQuantity = existingItem.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newTotalQuantity) {
                throw new IllegalArgumentException(
                        "Total quantity exceeds available stock: " + product.getStockQuantity());
            }
            existingItem.setQuantity(newTotalQuantity);
        } else {
            // Add new item to cart
            CartItem newItem = new CartItem(cart, product, request.getQuantity());
            cart.addItem(newItem);
        }

        // Save cart
        Cart savedCart = cartRepository.save(cart);

        return mapToCartResponse(savedCart);
    }

    /**
     * Updates the quantity of an item in the cart.
     *
     * @param userId the ID of the user
     * @param productId the ID of the product
     * @param quantity the new quantity
     * @return the updated cart response
     * @throws ResourceNotFoundException if the user, cart, or product is not found
     * @throws IllegalArgumentException if the product is out of stock or quantity exceeds available stock
     */
    @Transactional
    public CartResponse updateCartItemQuantity(final Long userId, final Long productId, final Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check if product is in cart
        CartItem cartItem = cart.findItemByProductId(productId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("Product not found in cart");
        }

        // If quantity is 0 or negative, remove item from cart
        if (quantity <= 0) {
            cart.removeItem(cartItem);
        } else {
            // Check if requested quantity is available
            if (product.getStockQuantity() < quantity) {
                throw new IllegalArgumentException(
                        "Requested quantity exceeds available stock: " + product.getStockQuantity());
            }
            cartItem.setQuantity(quantity);
        }

        // Save cart
        Cart savedCart = cartRepository.save(cart);

        return mapToCartResponse(savedCart);
    }

    /**
     * Removes an item from the cart.
     *
     * @param userId the ID of the user
     * @param productId the ID of the product
     * @return the updated cart response
     * @throws ResourceNotFoundException if the user or cart is not found
     */
    @Transactional
    public CartResponse removeFromCart(final Long userId, final Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        // Remove item from cart
        cart.removeItemByProductId(productId);

        // Save cart
        Cart savedCart = cartRepository.save(cart);

        return mapToCartResponse(savedCart);
    }

    /**
     * Clears all items from the cart.
     *
     * @param userId the ID of the user
     * @return the empty cart response
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional
    public CartResponse clearCart(final Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user).orElse(new Cart(user));

        // Clear cart
        cart.clearCart();

        // Save cart
        Cart savedCart = cartRepository.save(cart);

        return mapToCartResponse(savedCart);
    }

    /**
     * Validates the cart and returns any validation issues.
     *
     * @param userId the ID of the user
     * @return the validated cart response
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public CartResponse validateCart(final Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user).orElse(new Cart(user));

        // No need to validate each item in the cart here
        // The validation will be done in the mapToCartItemResponse method
        // when creating the CartItemResponse objects

        return mapToCartResponse(cart);
    }

    /**
     * Maps a Cart entity to a CartResponse DTO.
     *
     * @param cart the cart entity
     * @return the cart response DTO
     */
    private CartResponse mapToCartResponse(final Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUser().getId());
        response.setTotalItemCount(cart.getTotalItemCount());
        response.setUniqueItemCount(cart.getUniqueItemCount());
        response.setTotalAmount(cart.getTotalAmount());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());

        // Map cart items
        if (cart.getItems() != null) {
            List<CartItemResponse> itemResponses = cart.getItems().stream()
                    .map(this::mapToCartItemResponse)
                    .collect(Collectors.toList());
            response.setItems(itemResponses);
        } else {
            response.setItems(new ArrayList<>());
        }

        return response;
    }

    /**
     * Maps a CartItem entity to a CartItemResponse DTO.
     *
     * @param cartItem the cart item entity
     * @return the cart item response DTO
     */
    private CartItemResponse mapToCartItemResponse(final CartItem cartItem) {
        CartItemResponse response = new CartItemResponse();
        response.setId(cartItem.getId());
        
        Product product = cartItem.getProduct();
        if (product != null) {
            response.setProductId(product.getId());
            response.setProductName(product.getName());
            response.setProductSku(product.getSku());
            response.setProductBrand(product.getBrand() != null ? product.getBrand().getName() : null);
            response.setProductModel(product.getModel());
            response.setMainImageUrl(product.getMainImageUrl());
            response.setUnitPrice(product.getPrice());
            response.setAvailableStock(product.getStockQuantity());
            response.setProductActive(true); // Active status not implemented in entity yet
            response.setInStock(product.isInStock());
            response.setQuantityAvailable(cartItem.isQuantityAvailable());
            response.setValidationMessage(cartItem.getValidationMessage());
        } else {
            response.setProductActive(false);
            response.setInStock(false);
            response.setQuantityAvailable(false);
            response.setValidationMessage("Product not found");
            response.setUnitPrice(BigDecimal.ZERO);
            response.setAvailableStock(0);
        }
        
        response.setQuantity(cartItem.getQuantity());
        response.setTotalPrice(cartItem.getTotalPrice());
        response.setCreatedAt(cartItem.getCreatedAt());
        response.setUpdatedAt(cartItem.getUpdatedAt());
        
        return response;
    }

    /**
     * Cleans up expired carts.
     * This method would typically be called by a scheduled task.
     *
     * @return the number of expired carts removed
     */
    @Transactional
    public int cleanupExpiredCarts() {
        int count = 0;
        List<Cart> allCarts = cartRepository.findAll();
        
        for (Cart cart : allCarts) {
            if (cart.isExpired(cartMaxAgeDays)) {
                cartRepository.delete(cart);
                count++;
            }
        }
        
        return count;
    }
}