package com.horseriding.ecommerce.orders;

import com.horseriding.ecommerce.common.dtos.responses.PaginationResponse;
import com.horseriding.ecommerce.common.dtos.responses.SuccessResponse;
import com.horseriding.ecommerce.orders.OrderService.PayPalPaymentInfo;
import com.horseriding.ecommerce.orders.OrderService.ShippingDetails;
import com.horseriding.ecommerce.orders.dtos.requests.OrderCreateRequest;
import com.horseriding.ecommerce.orders.dtos.requests.OrderUpdateRequest;
import com.horseriding.ecommerce.orders.dtos.responses.OrderHistoryResponse;
import com.horseriding.ecommerce.orders.dtos.responses.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for order management endpoints.
 * Handles order creation, payment processing, and order status management.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    /** Order service for order management operations. */
    private final OrderService orderService;

    /**
     * Creates a new order.
     *
     * @param request the order creation request
     * @return the created order
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    /**
     * Creates a new order from the user's cart.
     *
     * @param shippingDetails the shipping details for the order
     * @param paypalPaymentId the PayPal payment ID
     * @param paypalPayerId the PayPal payer ID
     * @param paypalOrderId the PayPal order ID
     * @return the created order
     */
    @PostMapping("/from-cart")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> createOrderFromCart(
            @RequestBody ShippingDetails shippingDetails,
            @RequestParam String paypalPaymentId,
            @RequestParam String paypalPayerId,
            @RequestParam String paypalOrderId) {
        OrderResponse order =
                orderService.createOrderFromCart(
                        shippingDetails, paypalPaymentId, paypalPayerId, paypalOrderId);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    /**
     * Updates an order's status (admin only).
     *
     * @param orderId the ID of the order to update
     * @param request the order update request
     * @return the updated order
     */
    @PutMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId, @Valid @RequestBody OrderUpdateRequest request) {
        OrderResponse order = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(order);
    }

    /**
     * Gets an order by ID.
     *
     * @param orderId the ID of the order to get
     * @return the order
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        OrderResponse order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Gets all orders for the current user with pagination.
     *
     * @param page the page number (0-based)
     * @param size the page size
     * @param sortBy the field to sort by
     * @param sortDir the sort direction
     * @return page of orders for the user
     */
    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginationResponse<OrderHistoryResponse>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction =
                sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<OrderHistoryResponse> ordersPage = orderService.getCurrentUserOrders(pageable);

        PaginationResponse<OrderHistoryResponse> response =
                new PaginationResponse<>(
                        ordersPage.getContent(),
                        ordersPage.getNumber(),
                        ordersPage.getSize(),
                        ordersPage.getTotalElements(),
                        ordersPage.getTotalPages(),
                        ordersPage.isFirst(),
                        ordersPage.isLast());

        return ResponseEntity.ok(response);
    }

    /**
     * Gets all orders with pagination (admin only).
     *
     * @param page the page number (0-based)
     * @param size the page size
     * @param sortBy the field to sort by
     * @param sortDir the sort direction
     * @return page of all orders
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction =
                sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<OrderResponse> ordersPage = orderService.getAllOrders(pageable);

        PaginationResponse<OrderResponse> response =
                new PaginationResponse<>(
                        ordersPage.getContent(),
                        ordersPage.getNumber(),
                        ordersPage.getSize(),
                        ordersPage.getTotalElements(),
                        ordersPage.getTotalPages(),
                        ordersPage.isFirst(),
                        ordersPage.isLast());

        return ResponseEntity.ok(response);
    }

    /**
     * Searches for orders (admin only).
     *
     * @param searchTerm the search term
     * @param page the page number (0-based)
     * @param size the page size
     * @param sortBy the field to sort by
     * @param sortDir the sort direction
     * @return page of orders matching the search criteria
     */
    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse<OrderResponse>> searchOrders(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction =
                sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<OrderResponse> ordersPage = orderService.searchOrders(searchTerm, pageable);

        PaginationResponse<OrderResponse> response =
                new PaginationResponse<>(
                        ordersPage.getContent(),
                        ordersPage.getNumber(),
                        ordersPage.getSize(),
                        ordersPage.getTotalElements(),
                        ordersPage.getTotalPages(),
                        ordersPage.isFirst(),
                        ordersPage.isLast());

        return ResponseEntity.ok(response);
    }

    /**
     * Cancels an order.
     *
     * @param orderId the ID of the order to cancel
     * @return the cancelled order
     */
    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        OrderResponse order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Initiates a PayPal payment for an order.
     *
     * @param orderId the ID of the order
     * @return the PayPal payment information
     */
    @GetMapping("/{orderId}/paypal/init")
    public ResponseEntity<PayPalPaymentInfo> initiatePayPalPayment(@PathVariable Long orderId) {
        PayPalPaymentInfo paymentInfo = orderService.initiatePayPalPayment(orderId);
        return ResponseEntity.ok(paymentInfo);
    }

    /**
     * Completes a PayPal payment for an order.
     *
     * @param orderId the ID of the order
     * @param paypalPaymentId the PayPal payment ID
     * @param paypalPayerId the PayPal payer ID
     * @param paypalOrderId the PayPal order ID
     * @return the updated order
     */
    @PutMapping("/{orderId}/paypal/complete")
    public ResponseEntity<OrderResponse> completePayPalPayment(
            @PathVariable Long orderId,
            @RequestParam String paypalPaymentId,
            @RequestParam String paypalPayerId,
            @RequestParam String paypalOrderId) {
        OrderResponse order =
                orderService.completePayPalPayment(
                        orderId, paypalPaymentId, paypalPayerId, paypalOrderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Handles PayPal webhook notifications for payment status updates.
     *
     * @param paypalPaymentId the PayPal payment ID
     * @param status the payment status
     * @return success response
     */
    @PostMapping("/paypal/webhook")
    public ResponseEntity<SuccessResponse> handlePayPalWebhook(
            @RequestParam String paypalPaymentId, @RequestParam String status) {
        orderService.updatePaymentStatus(paypalPaymentId, status);
        return ResponseEntity.ok(new SuccessResponse("Payment status updated successfully", 200));
    }
}
