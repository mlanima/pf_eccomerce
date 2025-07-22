package com.horseriding.ecommerce.products;

import com.horseriding.ecommerce.common.dtos.responses.PaginationResponse;
import com.horseriding.ecommerce.common.dtos.responses.SuccessResponse;
import com.horseriding.ecommerce.products.dtos.requests.ProductCreateRequest;
import com.horseriding.ecommerce.products.dtos.requests.ProductUpdateRequest;
import com.horseriding.ecommerce.products.dtos.responses.ProductDetailResponse;
import com.horseriding.ecommerce.products.dtos.responses.ProductResponse;
import com.horseriding.ecommerce.products.dtos.responses.ProductSearchResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for product operations.
 * Provides endpoints for browsing, searching, and managing products.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public final class ProductController {

    /** Product service for product management operations. */
    private final ProductService productService;

    /**
     * Creates a new product (admin only).
     *
     * @param currentUserId the ID of the user making the request (placeholder for authenticated user)
     * @param request the product creation request
     * @return the created product
     */
    @PostMapping
    public ResponseEntity<ProductDetailResponse> createProduct(
            @RequestParam Long currentUserId, @Valid @RequestBody ProductCreateRequest request) {
        ProductDetailResponse product = productService.createProduct(currentUserId, request);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    /**
     * Updates an existing product (admin only).
     *
     * @param currentUserId the ID of the user making the request (placeholder for authenticated user)
     * @param productId the ID of the product to update
     * @param request the product update request
     * @return the updated product
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> updateProduct(
            @RequestParam Long currentUserId,
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request) {
        ProductDetailResponse product = productService.updateProduct(currentUserId, productId, request);
        return ResponseEntity.ok(product);
    }

    /**
     * Deletes a product (admin only).
     *
     * @param currentUserId the ID of the user making the request (placeholder for authenticated user)
     * @param productId the ID of the product to delete
     * @return success response
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<SuccessResponse> deleteProduct(
            @RequestParam Long currentUserId, @PathVariable Long productId) {
        productService.deleteProduct(currentUserId, productId);
        return ResponseEntity.ok(new SuccessResponse("Product deleted successfully", 200));
    }

    /**
     * Gets a product by ID.
     *
     * @param productId the ID of the product to get
     * @return the product
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductById(@PathVariable Long productId) {
        ProductDetailResponse product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    /**
     * Gets a product by SKU.
     *
     * @param sku the SKU of the product to get
     * @return the product
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDetailResponse> getProductBySku(@PathVariable String sku) {
        ProductDetailResponse product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    /**
     * Gets all products with pagination.
     *
     * @param page the page number (0-based)
     * @param size the page size
     * @param sortBy the field to sort by
     * @param sortDir the sort direction
     * @return page of products
     */
    @GetMapping
    public ResponseEntity<PaginationResponse<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ProductResponse> productsPage = productService.getAllProducts(pageable);
        
        PaginationResponse<ProductResponse> response = new PaginationResponse<>(
                productsPage.getContent(),
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.isFirst(),
                productsPage.isLast());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all products in a category with pagination.
     *
     * @param categoryId the ID of the category
     * @param page the page number (0-based)
     * @param size the page size
     * @param sortBy the field to sort by
     * @param sortDir the sort direction
     * @return page of products in the category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PaginationResponse<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ProductResponse> productsPage = productService.getProductsByCategory(categoryId, pageable);
        
        PaginationResponse<ProductResponse> response = new PaginationResponse<>(
                productsPage.getContent(),
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.isFirst(),
                productsPage.isLast());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all products from a brand with pagination.
     *
     * @param brandId the ID of the brand
     * @param page the page number (0-based)
     * @param size the page size
     * @param sortBy the field to sort by
     * @param sortDir the sort direction
     * @return page of products from the brand
     */
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<PaginationResponse<ProductResponse>> getProductsByBrand(
            @PathVariable Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ProductResponse> productsPage = productService.getProductsByBrand(brandId, pageable);
        
        PaginationResponse<ProductResponse> response = new PaginationResponse<>(
                productsPage.getContent(),
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.isFirst(),
                productsPage.isLast());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Searches for products by name or description with pagination.
     *
     * @param searchTerm the search term
     * @param page the page number (0-based)
     * @param size the page size
     * @param sortBy the field to sort by
     * @param sortDir the sort direction
     * @return page of products matching the search criteria
     */
    @GetMapping("/search")
    public ResponseEntity<PaginationResponse<ProductSearchResponse>> searchProducts(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ProductSearchResponse> productsPage = productService.searchProducts(searchTerm, pageable);
        
        PaginationResponse<ProductSearchResponse> response = new PaginationResponse<>(
                productsPage.getContent(),
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.isFirst(),
                productsPage.isLast());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Updates product stock quantity (admin only).
     *
     * @param currentUserId the ID of the user making the request (placeholder for authenticated user)
     * @param productId the ID of the product to update
     * @param quantity the new stock quantity
     * @return the updated product
     */
    @PutMapping("/{productId}/stock")
    public ResponseEntity<ProductDetailResponse> updateProductStock(
            @RequestParam Long currentUserId,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        ProductDetailResponse product = productService.updateProductStock(currentUserId, productId, quantity);
        return ResponseEntity.ok(product);
    }

    /**
     * Gets all products with low stock (admin only).
     *
     * @param currentUserId the ID of the user making the request (placeholder for authenticated user)
     * @param page the page number (0-based)
     * @param size the page size
     * @param sortBy the field to sort by
     * @param sortDir the sort direction
     * @return page of products with low stock
     */
    @GetMapping("/low-stock")
    public ResponseEntity<PaginationResponse<ProductResponse>> getLowStockProducts(
            @RequestParam Long currentUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "stockQuantity") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ProductResponse> productsPage = productService.getLowStockProducts(currentUserId, pageable);
        
        PaginationResponse<ProductResponse> response = new PaginationResponse<>(
                productsPage.getContent(),
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.isFirst(),
                productsPage.isLast());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all out of stock products (admin only).
     *
     * @param currentUserId the ID of the user making the request (placeholder for authenticated user)
     * @param page the page number (0-based)
     * @param size the page size
     * @param sortBy the field to sort by
     * @param sortDir the sort direction
     * @return page of out of stock products
     */
    @GetMapping("/out-of-stock")
    public ResponseEntity<PaginationResponse<ProductResponse>> getOutOfStockProducts(
            @RequestParam Long currentUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ProductResponse> productsPage = productService.getOutOfStockProducts(currentUserId, pageable);
        
        PaginationResponse<ProductResponse> response = new PaginationResponse<>(
                productsPage.getContent(),
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.isFirst(),
                productsPage.isLast());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Uploads a product image (admin only).
     *
     * @param currentUserId the ID of the user making the request (placeholder for authenticated user)
     * @param productId the ID of the product
     * @param file the image file to upload
     * @return the URL of the uploaded image
     * @throws IOException if an I/O error occurs
     */
    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadProductImage(
            @RequestParam Long currentUserId,
            @PathVariable Long productId,
            @RequestPart("file") MultipartFile file) throws IOException {
        String imageUrl = productService.uploadProductImage(currentUserId, productId, file);
        return ResponseEntity.ok(imageUrl);
    }

    /**
     * Removes a product image (admin only).
     *
     * @param currentUserId the ID of the user making the request (placeholder for authenticated user)
     * @param productId the ID of the product
     * @param imageUrl the URL of the image to remove
     * @return success response
     */
    @DeleteMapping("/{productId}/images")
    public ResponseEntity<SuccessResponse> removeProductImage(
            @RequestParam Long currentUserId,
            @PathVariable Long productId,
            @RequestParam String imageUrl) {
        productService.removeProductImage(currentUserId, productId, imageUrl);
        return ResponseEntity.ok(new SuccessResponse("Image removed successfully", 200));
    }
}
