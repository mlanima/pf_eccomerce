package com.horseriding.ecommerce.cart;

import com.horseriding.ecommerce.cart.dtos.requests.AddToCartRequest;
import com.horseriding.ecommerce.cart.dtos.requests.UpdateCartItemRequest;
import com.horseriding.ecommerce.cart.dtos.responses.CartResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for shopping cart operations.
 * Handles cart management and checkout preparation.
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    /** Cart service for cart management operations. */
    private final CartService cartService;

    /**
     * Gets the current user's cart.
     *
     * @return the user's cart
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> getUserCart() {
        CartResponse cart = cartService.getCurrentUserCart();
        return ResponseEntity.ok(cart);
    }

    /**
     * Adds an item to the cart.
     *
     * @param request the add to cart request
     * @return the updated cart
     */
    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {
        CartResponse cart = cartService.addToCart(request);
        return ResponseEntity.ok(cart);
    }

    /**
     * Updates the quantity of an item in the cart.
     *
     * @param productId the product ID
     * @param request the update cart item request
     * @return the updated cart
     */
    @PutMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @PathVariable Long productId, @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse cart = cartService.updateCartItemQuantity(productId, request.getQuantity());
        return ResponseEntity.ok(cart);
    }

    /**
     * Removes an item from the cart.
     *
     * @param productId the product ID
     * @return the updated cart
     */
    @DeleteMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable Long productId) {
        CartResponse cart = cartService.removeFromCart(productId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Clears all items from the cart.
     *
     * @return the empty cart
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> clearCart() {
        CartResponse cart = cartService.clearCart();
        return ResponseEntity.ok(cart);
    }

    /**
     * Validates the cart and returns any validation issues.
     *
     * @return the validated cart
     */
    @GetMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> validateCart() {
        CartResponse cart = cartService.validateCart();
        return ResponseEntity.ok(cart);
    }
}
