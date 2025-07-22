package com.horseriding.ecommerce.cart;

import com.horseriding.ecommerce.cart.dtos.requests.AddToCartRequest;
import com.horseriding.ecommerce.cart.dtos.requests.UpdateCartItemRequest;
import com.horseriding.ecommerce.cart.dtos.responses.CartResponse;
import com.horseriding.ecommerce.common.dtos.responses.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
     * In a real application, this would use the authenticated user from the security context.
     * For now, we'll use a path variable as a placeholder.
     *
     * @param userId the user ID (placeholder for authenticated user)
     * @return the user's cart
     */
    @GetMapping
    public ResponseEntity<CartResponse> getUserCart(@RequestParam Long userId) {
        CartResponse cart = cartService.getUserCart(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Adds an item to the cart.
     *
     * @param userId the user ID (placeholder for authenticated user)
     * @param request the add to cart request
     * @return the updated cart
     */
    @PostMapping
    public ResponseEntity<CartResponse> addToCart(
            @RequestParam Long userId, @Valid @RequestBody AddToCartRequest request) {
        CartResponse cart = cartService.addToCart(userId, request);
        return ResponseEntity.ok(cart);
    }

    /**
     * Updates the quantity of an item in the cart.
     *
     * @param userId the user ID (placeholder for authenticated user)
     * @param productId the product ID
     * @param request the update cart item request
     * @return the updated cart
     */
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @RequestParam Long userId,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse cart = cartService.updateCartItemQuantity(userId, productId, request.getQuantity());
        return ResponseEntity.ok(cart);
    }

    /**
     * Removes an item from the cart.
     *
     * @param userId the user ID (placeholder for authenticated user)
     * @param productId the product ID
     * @return the updated cart
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @RequestParam Long userId, @PathVariable Long productId) {
        CartResponse cart = cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Clears all items from the cart.
     *
     * @param userId the user ID (placeholder for authenticated user)
     * @return the empty cart
     */
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(@RequestParam Long userId) {
        CartResponse cart = cartService.clearCart(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Validates the cart and returns any validation issues.
     *
     * @param userId the user ID (placeholder for authenticated user)
     * @return the validated cart
     */
    @GetMapping("/validate")
    public ResponseEntity<CartResponse> validateCart(@RequestParam Long userId) {
        CartResponse cart = cartService.validateCart(userId);
        return ResponseEntity.ok(cart);
    }
}
