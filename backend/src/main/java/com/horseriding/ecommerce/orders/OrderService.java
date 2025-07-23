package com.horseriding.ecommerce.orders;

import com.horseriding.ecommerce.auth.SecurityUtils;
import com.horseriding.ecommerce.cart.Cart;
import com.horseriding.ecommerce.cart.CartItem;
import com.horseriding.ecommerce.cart.CartRepository;
import com.horseriding.ecommerce.cart.CartService;
import com.horseriding.ecommerce.exception.AccessDeniedException;
import com.horseriding.ecommerce.exception.ResourceNotFoundException;
import com.horseriding.ecommerce.orders.dtos.requests.OrderCreateRequest;
import com.horseriding.ecommerce.orders.dtos.requests.OrderUpdateRequest;
import com.horseriding.ecommerce.orders.dtos.responses.OrderHistoryResponse;
import com.horseriding.ecommerce.orders.dtos.responses.OrderItemResponse;
import com.horseriding.ecommerce.orders.dtos.responses.OrderResponse;
import com.horseriding.ecommerce.products.Product;
import com.horseriding.ecommerce.products.ProductRepository;
import com.horseriding.ecommerce.products.ProductService;
import com.horseriding.ecommerce.users.User;
import com.horseriding.ecommerce.users.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for order management operations.
 * Handles order creation, payment processing, and order status management.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    /** Repository for order data access. */
    private final OrderRepository orderRepository;

    /** Repository for order item data access. */
    private final OrderItemRepository orderItemRepository;

    /** Repository for product data access. */
    private final ProductRepository productRepository;

    /** Repository for user data access. */
    private final UserRepository userRepository;

    /** Repository for cart data access. */
    private final CartRepository cartRepository;

    /** Service for cart operations. */
    private final CartService cartService;

    /** Service for product operations. */
    private final ProductService productService;

    /** PayPal client ID for frontend integration. */
    @Value("${paypal.client-id:}")
    private String paypalClientId;

    /** PayPal client secret for backend integration. */
    @Value("${paypal.client-secret:}")
    private String paypalClientSecret;

    /** PayPal API base URL. */
    @Value("${paypal.api-base-url:https://api-m.sandbox.paypal.com}")
    private String paypalApiBaseUrl;

    /** Email service for order notifications. */
    // private final EmailService emailService; // Would be injected in a real application

    /**
     * Creates a new order from cart for the current user.
     *
     * @param request the order creation request
     * @return the created order
     * @throws IllegalStateException if no user is authenticated
     * @throws ResourceNotFoundException if the product is not found
     * @throws IllegalArgumentException if the cart is empty or contains invalid items
     */
    @Transactional
    public OrderResponse createOrder(final OrderCreateRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        // Validate order items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // Create new order
        Order order = new Order(currentUser, request.getTotalAmount());
        order.setSubtotalAmount(request.getSubtotalAmount());
        order.setShippingAmount(request.getShippingAmount());
        order.setTaxAmount(request.getTaxAmount());
        order.setStatus(OrderStatus.PENDING);

        // Set shipping address
        order.setShippingName(request.getShippingName());
        order.setShippingAddressLine1(request.getShippingAddressLine1());
        order.setShippingAddressLine2(request.getShippingAddressLine2());
        order.setShippingCity(request.getShippingCity());
        order.setShippingState(request.getShippingState());
        order.setShippingPostalCode(request.getShippingPostalCode());
        order.setShippingCountry(request.getShippingCountry());
        order.setShippingPhone(request.getShippingPhone());

        // Set PayPal fields if provided
        if (request.getPaypalPaymentId() != null) {
            order.setPaypalPaymentId(request.getPaypalPaymentId());
        }
        if (request.getPaypalPayerId() != null) {
            order.setPaypalPayerId(request.getPaypalPayerId());
        }
        if (request.getPaypalOrderId() != null) {
            order.setPaypalOrderId(request.getPaypalOrderId());
        }

        // Save order to get ID
        Order savedOrder = orderRepository.save(order);

        // Add order items
        for (OrderCreateRequest.OrderItemCreateRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            // Check if product is in stock
            if (!product.isInStock()) {
                throw new IllegalArgumentException("Product is out of stock: " + product.getName());
            }

            // Check if requested quantity is available
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException(
                        "Requested quantity exceeds available stock for product: " + product.getName());
            }

            // Create order item
            OrderItem orderItem = new OrderItem(
                    savedOrder, product, itemRequest.getQuantity(), itemRequest.getUnitPrice());
            savedOrder.addItem(orderItem);

            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        // Save order with items
        Order finalOrder = orderRepository.save(savedOrder);

        // Clear user's cart after successful order creation
        Cart cart = cartRepository.findByUser(currentUser).orElse(null);
        if (cart != null) {
            cart.clearCart();
            cartRepository.save(cart);
        }

        // Send order confirmation email
        // emailService.sendOrderConfirmation(finalOrder); // Would be called in a real application

        return mapToOrderResponse(finalOrder);
    }

    /**
     * Creates a new order directly from the current user's cart.
     *
     * @param shippingDetails the shipping details for the order
     * @param paypalPaymentId the PayPal payment ID
     * @param paypalPayerId the PayPal payer ID
     * @param paypalOrderId the PayPal order ID
     * @return the created order
     * @throws IllegalStateException if no user is authenticated
     * @throws ResourceNotFoundException if the cart is not found
     * @throws IllegalArgumentException if the cart is empty or contains invalid items
     */
    @Transactional
    public OrderResponse createOrderFromCart(
            final ShippingDetails shippingDetails,
            final String paypalPaymentId,
            final String paypalPayerId,
            final String paypalOrderId) {
        User currentUser = SecurityUtils.getCurrentUser();

        Cart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        // Validate cart
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Validate cart items
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product == null) {
                throw new IllegalArgumentException("Invalid product in cart");
            }
            if (!product.isInStock()) {
                throw new IllegalArgumentException("Product is out of stock: " + product.getName());
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException(
                        "Requested quantity exceeds available stock for product: " + product.getName());
            }
        }

        // Calculate totals
        BigDecimal subtotal = cart.getTotalAmount();
        BigDecimal shipping = BigDecimal.ZERO; // In a real application, this would be calculated based on shipping method
        BigDecimal tax = BigDecimal.ZERO; // In a real application, this would be calculated based on tax rules
        BigDecimal total = subtotal.add(shipping).add(tax);

        // Create new order
        Order order = new Order(currentUser, total);
        order.setSubtotalAmount(subtotal);
        order.setShippingAmount(shipping);
        order.setTaxAmount(tax);
        order.setStatus(OrderStatus.PENDING);

        // Set shipping address
        order.setShippingName(shippingDetails.getName());
        order.setShippingAddressLine1(shippingDetails.getAddressLine1());
        order.setShippingAddressLine2(shippingDetails.getAddressLine2());
        order.setShippingCity(shippingDetails.getCity());
        order.setShippingState(shippingDetails.getState());
        order.setShippingPostalCode(shippingDetails.getPostalCode());
        order.setShippingCountry(shippingDetails.getCountry());
        order.setShippingPhone(shippingDetails.getPhone());

        // Set PayPal fields
        order.setPaypalPaymentId(paypalPaymentId);
        order.setPaypalPayerId(paypalPayerId);
        order.setPaypalOrderId(paypalOrderId);

        // Save order to get ID
        Order savedOrder = orderRepository.save(order);

        // Add order items from cart
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            
            // Create order item
            OrderItem orderItem = new OrderItem(
                    savedOrder, product, cartItem.getQuantity(), product.getPrice());
            savedOrder.addItem(orderItem);

            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Save order with items
        Order finalOrder = orderRepository.save(savedOrder);

        // Clear user's cart after successful order creation
        cart.clearCart();
        cartRepository.save(cart);

        // Send order confirmation email
        // emailService.sendOrderConfirmation(finalOrder); // Would be called in a real application

        return mapToOrderResponse(finalOrder);
    }

    /**
     * Updates an order's payment status based on PayPal webhook notification.
     *
     * @param paypalPaymentId the PayPal payment ID
     * @param status the new payment status
     * @return the updated order
     * @throws ResourceNotFoundException if the order is not found
     */
    @Transactional
    public OrderResponse updatePaymentStatus(final String paypalPaymentId, final String status) {
        Order order = orderRepository.findByPaypalPaymentId(paypalPaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Update order status based on PayPal payment status
        switch (status.toLowerCase()) {
            case "completed":
                order.setStatus(OrderStatus.PAID);
                // Send payment confirmation email
                // emailService.sendPaymentConfirmation(order); // Would be called in a real application
                break;
            case "cancelled":
                order.setStatus(OrderStatus.CANCELLED);
                // Restore product stock
                restoreProductStock(order);
                break;
            case "refunded":
                order.setStatus(OrderStatus.REFUNDED);
                // Restore product stock
                restoreProductStock(order);
                break;
            default:
                // Keep current status
                break;
        }

        // Save updated order
        Order updatedOrder = orderRepository.save(order);

        return mapToOrderResponse(updatedOrder);
    }

    /**
     * Updates an order's status.
     *
     * @param orderId the ID of the order to update
     * @param request the order update request
     * @return the updated order
     * @throws ResourceNotFoundException if the order is not found
     */
    @Transactional
    public OrderResponse updateOrderStatus(final Long orderId, final OrderUpdateRequest request) {
        // Get order to update
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Update order status
        if (request.getStatus() != null) {
            // Handle stock restoration for cancelled or refunded orders
            if ((request.getStatus() == OrderStatus.CANCELLED || request.getStatus() == OrderStatus.REFUNDED)
                    && order.getStatus() != OrderStatus.CANCELLED && order.getStatus() != OrderStatus.REFUNDED) {
                restoreProductStock(order);
            }
            
            order.setStatus(request.getStatus());
        }

        // Update tracking information
        if (request.getTrackingNumber() != null) {
            order.setTrackingNumber(request.getTrackingNumber());
        }
        if (request.getCarrier() != null) {
            order.setCarrier(request.getCarrier());
        }
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        // Save updated order
        Order updatedOrder = orderRepository.save(order);

        // Send order status update email
        // emailService.sendOrderStatusUpdate(updatedOrder); // Would be called in a real application

        return mapToOrderResponse(updatedOrder);
    }

    /**
     * Gets an order by ID.
     *
     * @param orderId the ID of the order to get
     * @return the order
     * @throws ResourceNotFoundException if the order is not found
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(final Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return mapToOrderResponse(order);
    }

    /**
     * Gets all orders for the current user with pagination.
     *
     * @param pageable pagination information
     * @return page of orders for the user
     * @throws IllegalStateException if no user is authenticated
     */
    @Transactional(readOnly = true)
    public Page<OrderHistoryResponse> getCurrentUserOrders(final Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();

        Page<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable);

        return orders.map(this::mapToOrderHistoryResponse);
    }

    /**
     * Gets all orders with pagination.
     *
     * @param pageable pagination information
     * @return page of all orders
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(final Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);

        return orders.map(this::mapToOrderResponse);
    }

    /**
     * Searches for orders.
     *
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of orders matching the search criteria
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> searchOrders(final String searchTerm, final Pageable pageable) {
        Page<Order> orders = orderRepository.searchOrders(searchTerm, pageable);

        return orders.map(this::mapToOrderResponse);
    }

    /**
     * Cancels an order for the current user.
     *
     * @param orderId the ID of the order to cancel
     * @return the cancelled order
     * @throws IllegalStateException if no user is authenticated
     * @throws ResourceNotFoundException if the order is not found
     * @throws IllegalArgumentException if the order cannot be cancelled
     */
    @Transactional
    public OrderResponse cancelOrder(final Long orderId) {
        User currentUser = SecurityUtils.getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check if order belongs to current user
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to cancel this order");
        }

        // Check if order can be cancelled
        if (!order.isCancellable()) {
            throw new IllegalArgumentException("Order cannot be cancelled in its current state");
        }

        // Update order status
        order.setStatus(OrderStatus.CANCELLED);

        // Restore product stock
        restoreProductStock(order);

        // Save updated order
        Order cancelledOrder = orderRepository.save(order);

        // Send order cancellation email
        // emailService.sendOrderCancellation(cancelledOrder); // Would be called in a real application

        return mapToOrderResponse(cancelledOrder);
    }

    /**
     * Initiates a PayPal payment for an order.
     *
     * @param orderId the ID of the order
     * @return the PayPal payment information
     * @throws ResourceNotFoundException if the order is not found
     */
    public PayPalPaymentInfo initiatePayPalPayment(final Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // In a real application, this would call the PayPal API to create a payment
        // For now, we'll just return the client ID for frontend integration
        PayPalPaymentInfo paymentInfo = new PayPalPaymentInfo();
        paymentInfo.setClientId(paypalClientId);
        paymentInfo.setOrderId(order.getId().toString());
        paymentInfo.setCurrency("USD");
        paymentInfo.setAmount(order.getTotalAmount());
        
        return paymentInfo;
    }

    /**
     * Completes a PayPal payment for an order.
     *
     * @param orderId the ID of the order
     * @param paypalPaymentId the PayPal payment ID
     * @param paypalPayerId the PayPal payer ID
     * @param paypalOrderId the PayPal order ID
     * @return the updated order
     * @throws ResourceNotFoundException if the order is not found
     */
    @Transactional
    public OrderResponse completePayPalPayment(
            final Long orderId,
            final String paypalPaymentId,
            final String paypalPayerId,
            final String paypalOrderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // In a real application, this would call the PayPal API to execute the payment
        // For now, we'll just update the order status and PayPal fields

        // Update PayPal fields
        order.setPaypalPaymentId(paypalPaymentId);
        order.setPaypalPayerId(paypalPayerId);
        order.setPaypalOrderId(paypalOrderId);

        // Update order status
        order.setStatus(OrderStatus.PAID);

        // Save updated order
        Order paidOrder = orderRepository.save(order);

        // Send payment confirmation email
        // emailService.sendPaymentConfirmation(paidOrder); // Would be called in a real application

        return mapToOrderResponse(paidOrder);
    }

    /**
     * Restores product stock for all items in an order.
     * Used when an order is cancelled or refunded.
     *
     * @param order the order
     */
    private void restoreProductStock(final Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }
    }

    /**
     * Maps an Order entity to an OrderResponse DTO.
     *
     * @param order the order entity
     * @return the order response DTO
     */
    private OrderResponse mapToOrderResponse(final Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setUserEmail(order.getUser().getEmail());
        response.setUserFullName(order.getUser().getFullName());
        
        // Map shipping address
        response.setShippingName(order.getShippingName());
        response.setShippingAddressLine1(order.getShippingAddressLine1());
        response.setShippingAddressLine2(order.getShippingAddressLine2());
        response.setShippingCity(order.getShippingCity());
        response.setShippingState(order.getShippingState());
        response.setShippingPostalCode(order.getShippingPostalCode());
        response.setShippingCountry(order.getShippingCountry());
        response.setShippingPhone(order.getShippingPhone());
        
        // Map amounts
        response.setTotalAmount(order.getTotalAmount());
        response.setSubtotalAmount(order.getSubtotalAmount());
        response.setShippingAmount(order.getShippingAmount());
        response.setTaxAmount(order.getTaxAmount());
        response.setStatus(order.getStatus());
        
        // Map PayPal fields
        response.setPaypalPaymentId(order.getPaypalPaymentId());
        response.setPaypalPayerId(order.getPaypalPayerId());
        response.setPaypalOrderId(order.getPaypalOrderId());
        response.setPaymentMethod(order.getPaymentMethod());
        
        // Map tracking information
        response.setTrackingNumber(order.getTrackingNumber());
        response.setCarrier(order.getCarrier());
        response.setNotes(order.getNotes());
        
        // Map timestamps
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setShippedAt(order.getShippedAt());
        response.setDeliveredAt(order.getDeliveredAt());
        
        // Map order items
        if (order.getItems() != null) {
            List<OrderItemResponse> itemResponses = order.getItems().stream()
                    .map(this::mapToOrderItemResponse)
                    .collect(Collectors.toList());
            response.setItems(itemResponses);
        } else {
            response.setItems(new ArrayList<>());
        }
        
        return response;
    }

    /**
     * Maps an OrderItem entity to an OrderItemResponse DTO.
     *
     * @param orderItem the order item entity
     * @return the order item response DTO
     */
    private OrderItemResponse mapToOrderItemResponse(final OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setProductId(orderItem.getProduct() != null ? orderItem.getProduct().getId() : null);
        response.setQuantity(orderItem.getQuantity());
        response.setUnitPrice(orderItem.getUnitPrice());
        response.setTotalPrice(orderItem.getTotalPrice());
        response.setProductName(orderItem.getProductName());
        response.setProductSku(orderItem.getProductSku());
        response.setProductBrand(orderItem.getProductBrand());
        response.setProductModel(orderItem.getProductModel());
        response.setCreatedAt(orderItem.getCreatedAt());
        response.setUpdatedAt(orderItem.getUpdatedAt());
        return response;
    }

    /**
     * Maps an Order entity to an OrderHistoryResponse DTO.
     *
     * @param order the order entity
     * @return the order history response DTO
     */
    private OrderHistoryResponse mapToOrderHistoryResponse(final Order order) {
        OrderHistoryResponse response = new OrderHistoryResponse();
        response.setId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setTrackingNumber(order.getTrackingNumber());
        response.setCarrier(order.getCarrier());
        response.setTotalItemCount(order.getTotalItemCount());
        response.setCreatedAt(order.getCreatedAt());
        response.setShippedAt(order.getShippedAt());
        response.setDeliveredAt(order.getDeliveredAt());
        return response;
    }

    /**
     * DTO for PayPal payment information.
     */
    public static class PayPalPaymentInfo {
        private String clientId;
        private String orderId;
        private String currency;
        private BigDecimal amount;
        
        public String getClientId() {
            return clientId;
        }
        
        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
        
        public String getOrderId() {
            return orderId;
        }
        
        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
        
        public String getCurrency() {
            return currency;
        }
        
        public void setCurrency(String currency) {
            this.currency = currency;
        }
        
        public BigDecimal getAmount() {
            return amount;
        }
        
        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    /**
     * DTO for shipping details.
     */
    public static class ShippingDetails {
        private String name;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String phone;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getAddressLine1() {
            return addressLine1;
        }
        
        public void setAddressLine1(String addressLine1) {
            this.addressLine1 = addressLine1;
        }
        
        public String getAddressLine2() {
            return addressLine2;
        }
        
        public void setAddressLine2(String addressLine2) {
            this.addressLine2 = addressLine2;
        }
        
        public String getCity() {
            return city;
        }
        
        public void setCity(String city) {
            this.city = city;
        }
        
        public String getState() {
            return state;
        }
        
        public void setState(String state) {
            this.state = state;
        }
        
        public String getPostalCode() {
            return postalCode;
        }
        
        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}