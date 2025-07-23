package com.horseriding.ecommerce.products;

import com.horseriding.ecommerce.brands.Brand;
import com.horseriding.ecommerce.brands.BrandRepository;
import com.horseriding.ecommerce.categories.Category;
import com.horseriding.ecommerce.categories.CategoryRepository;
import com.horseriding.ecommerce.exception.ResourceNotFoundException;
import com.horseriding.ecommerce.products.dtos.requests.ProductCreateRequest;
import com.horseriding.ecommerce.products.dtos.requests.ProductUpdateRequest;
import com.horseriding.ecommerce.products.dtos.responses.ProductDetailResponse;
import com.horseriding.ecommerce.products.dtos.responses.ProductResponse;
import com.horseriding.ecommerce.products.dtos.responses.ProductSearchResponse;
import com.horseriding.ecommerce.users.UserRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for product management operations.
 * Handles product creation, update, deletion, and inventory management.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    /** Repository for product data access. */
    private final ProductRepository productRepository;

    /** Repository for category data access. */
    private final CategoryRepository categoryRepository;

    /** Repository for brand data access. */
    private final BrandRepository brandRepository;

    /** Repository for user data access (for authorization). */
    private final UserRepository userRepository;

    /** Base directory for product image storage. */
    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;

    /** Low stock notification threshold. */
    @Value("${app.product.low-stock-threshold:10}")
    private int defaultLowStockThreshold;

    /**
     * Creates a new product.
     *
     * @param request the product creation request
     * @return the created product
     * @throws ResourceNotFoundException if the category is not found
     * @throws IllegalArgumentException if SKU already exists
     */
    @Transactional
    public ProductDetailResponse createProduct(final ProductCreateRequest request) {
        // Check if SKU already exists
        if (request.getSku() != null && productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("SKU already exists");
        }

        // Get category
        Category category =
                categoryRepository
                        .findById(request.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Create new product
        Product product =
                new Product(
                        request.getName(), request.getDescription(), request.getPrice(), category);
        product.setStockQuantity(request.getStockQuantity());
        product.setLowStockThreshold(
                request.getLowStockThreshold() != null
                        ? request.getLowStockThreshold()
                        : defaultLowStockThreshold);
        product.setSku(request.getSku());
        product.setFeatured(request.isFeatured());
        product.setWeightKg(request.getWeightKg());
        product.setDimensions(request.getDimensions());
        product.setModel(request.getModel());

        // Set brand if provided
        if (request.getBrand() != null && !request.getBrand().isEmpty()) {
            Optional<Brand> existingBrand =
                    brandRepository.findByNameIgnoreCase(request.getBrand());
            Brand brand =
                    existingBrand.orElseGet(
                            () -> {
                                Brand newBrand = new Brand(request.getBrand());
                                return brandRepository.save(newBrand);
                            });
            product.setBrand(brand);
        }

        // Set image URLs if provided
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            product.setImageUrls(new ArrayList<>(request.getImageUrls()));
        }

        // Set specifications if provided
        if (request.getSpecifications() != null && !request.getSpecifications().isEmpty()) {
            product.setSpecifications(new HashMap<>(request.getSpecifications()));
        }

        // Save product to database
        Product savedProduct = productRepository.save(product);

        // Return product response
        return mapToProductDetailResponse(savedProduct);
    }

    /**
     * Updates an existing product.
     *
     * @param productId the ID of the product to update
     * @param request the product update request
     * @return the updated product
     * @throws ResourceNotFoundException if the product or category is not found
     * @throws IllegalArgumentException if SKU already exists for another product
     */
    @Transactional
    public ProductDetailResponse updateProduct(
            final Long productId, final ProductUpdateRequest request) {
        // Get product to update
        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check if SKU is being changed and if it already exists
        if (request.getSku() != null
                && !request.getSku().equals(product.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("SKU already exists");
        }

        // Get category if changed
        if (!request.getCategoryId().equals(product.getCategory().getId())) {
            Category category =
                    categoryRepository
                            .findById(request.getCategoryId())
                            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        // Update product fields
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setLowStockThreshold(request.getLowStockThreshold());
        product.setSku(request.getSku());
        product.setFeatured(request.isFeatured());
        product.setWeightKg(request.getWeightKg());
        product.setDimensions(request.getDimensions());
        product.setModel(request.getModel());

        // Update brand if provided
        if (request.getBrand() != null) {
            if (request.getBrand().isEmpty()) {
                product.setBrand(null);
            } else {
                Optional<Brand> existingBrand =
                        brandRepository.findByNameIgnoreCase(request.getBrand());
                Brand brand =
                        existingBrand.orElseGet(
                                () -> {
                                    Brand newBrand = new Brand(request.getBrand());
                                    return brandRepository.save(newBrand);
                                });
                product.setBrand(brand);
            }
        }

        // Update image URLs if provided
        if (request.getImageUrls() != null) {
            product.setImageUrls(new ArrayList<>(request.getImageUrls()));
        }

        // Update specifications if provided
        if (request.getSpecifications() != null) {
            product.setSpecifications(new HashMap<>(request.getSpecifications()));
        }

        // Save updated product
        Product updatedProduct = productRepository.save(product);

        // Return product response
        return mapToProductDetailResponse(updatedProduct);
    }

    /**
     * Deletes a product.
     *
     * @param productId the ID of the product to delete
     * @throws ResourceNotFoundException if the product is not found
     */
    @Transactional
    public void deleteProduct(final Long productId) {
        // Get product to delete
        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Delete product
        productRepository.delete(product);
    }

    /**
     * Gets a product by ID.
     *
     * @param productId the ID of the product to get
     * @return the product
     * @throws ResourceNotFoundException if the product is not found
     */
    public ProductDetailResponse getProductById(final Long productId) {
        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return mapToProductDetailResponse(product);
    }

    /**
     * Gets a product by SKU.
     *
     * @param sku the SKU of the product to get
     * @return the product
     * @throws ResourceNotFoundException if the product is not found
     */
    public ProductDetailResponse getProductBySku(final String sku) {
        Product product =
                productRepository
                        .findBySku(sku)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return mapToProductDetailResponse(product);
    }

    /**
     * Gets all products with pagination.
     *
     * @param pageable pagination information
     * @return page of products
     */
    public Page<ProductResponse> getAllProducts(final Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::mapToProductResponse);
    }

    /**
     * Gets all products in a category with pagination.
     *
     * @param categoryId the ID of the category
     * @param pageable pagination information
     * @return page of products in the category
     * @throws ResourceNotFoundException if the category is not found
     */
    public Page<ProductResponse> getProductsByCategory(
            final Long categoryId, final Pageable pageable) {
        Category category =
                categoryRepository
                        .findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Page<Product> products = productRepository.findByCategory(category, pageable);
        return products.map(this::mapToProductResponse);
    }

    /**
     * Gets all products from a brand with pagination.
     *
     * @param brandId the ID of the brand
     * @param pageable pagination information
     * @return page of products from the brand
     * @throws ResourceNotFoundException if the brand is not found
     */
    public Page<ProductResponse> getProductsByBrand(final Long brandId, final Pageable pageable) {
        Brand brand =
                brandRepository
                        .findById(brandId)
                        .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        Page<Product> products = productRepository.findByBrand(brand, pageable);
        return products.map(this::mapToProductResponse);
    }

    /**
     * Searches for products by name or description with pagination.
     *
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of products matching the search criteria
     */
    public Page<ProductSearchResponse> searchProducts(
            final String searchTerm, final Pageable pageable) {
        Page<Product> products = productRepository.searchProducts(searchTerm, pageable);
        return products.map(this::mapToProductSearchResponse);
    }

    /**
     * Updates product stock quantity.
     *
     * @param productId the ID of the product to update
     * @param quantity the new stock quantity
     * @return the updated product
     * @throws ResourceNotFoundException if the product is not found
     * @throws IllegalArgumentException if quantity is negative
     */
    @Transactional
    public ProductDetailResponse updateProductStock(final Long productId, final Integer quantity) {
        // Validate quantity
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        // Get product to update
        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Update stock quantity
        product.setStockQuantity(quantity);

        // Save updated product
        Product updatedProduct = productRepository.save(product);

        // Check if stock is low and notify (in a real application, this would send notifications)
        if (updatedProduct.isLowStock()) {
            // Log low stock for now
            System.out.println(
                    "Low stock alert: Product "
                            + updatedProduct.getName()
                            + " (ID: "
                            + updatedProduct.getId()
                            + ") has low stock: "
                            + updatedProduct.getStockQuantity());
        }

        // Return product response
        return mapToProductDetailResponse(updatedProduct);
    }

    /**
     * Gets all products with low stock.
     *
     * @param pageable pagination information
     * @return page of products with low stock
     */
    public Page<ProductResponse> getLowStockProducts(final Pageable pageable) {
        // Get all products and filter for low stock
        // This is not efficient for large datasets, but works for now
        // In a real application, we would add a custom query to the repository
        Page<Product> allProducts = productRepository.findAll(pageable);

        // Filter products and convert to list
        List<Product> lowStockProducts =
                allProducts.getContent().stream()
                        .filter(Product::isLowStock)
                        .collect(Collectors.toList());

        // Create a new Page from the filtered list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), lowStockProducts.size());
        List<Product> pageContent =
                start < lowStockProducts.size()
                        ? lowStockProducts.subList(start, end)
                        : new ArrayList<>();

        Page<Product> lowStockPage = new PageImpl<>(pageContent, pageable, lowStockProducts.size());

        return lowStockPage.map(this::mapToProductResponse);
    }

    /**
     * Gets all out of stock products.
     *
     * @param pageable pagination information
     * @return page of out of stock products
     */
    public Page<ProductResponse> getOutOfStockProducts(final Pageable pageable) {
        // Get all products and filter for out of stock
        // This is not efficient for large datasets, but works for now
        // In a real application, we would add a custom query to the repository
        Page<Product> allProducts = productRepository.findAll(pageable);

        // Filter products and convert to list
        List<Product> outOfStockProducts =
                allProducts.getContent().stream()
                        .filter(Product::isOutOfStock)
                        .collect(Collectors.toList());

        // Create a new Page from the filtered list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), outOfStockProducts.size());
        List<Product> pageContent =
                start < outOfStockProducts.size()
                        ? outOfStockProducts.subList(start, end)
                        : new ArrayList<>();

        Page<Product> outOfStockPage =
                new org.springframework.data.domain.PageImpl<>(
                        pageContent, pageable, outOfStockProducts.size());

        return outOfStockPage.map(this::mapToProductResponse);
    }

    /**
     * Uploads a product image.
     *
     * @param productId the ID of the product
     * @param file the image file to upload
     * @return the URL of the uploaded image
     * @throws ResourceNotFoundException if the product is not found
     * @throws IOException if an I/O error occurs
     */
    @Transactional
    public String uploadProductImage(final Long productId, final MultipartFile file)
            throws IOException {
        // Get product
        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Create upload directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generate unique filename
        String timestamp =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uniqueFilename =
                productId
                        + "_"
                        + timestamp
                        + "_"
                        + UUID.randomUUID().toString()
                        + "_"
                        + file.getOriginalFilename();
        Path targetPath = Paths.get(uploadDir).resolve(uniqueFilename);

        // Save file
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Generate URL
        String imageUrl = "/uploads/products/" + uniqueFilename;

        // Add image URL to product
        product.addImage(imageUrl);
        productRepository.save(product);

        return imageUrl;
    }

    /**
     * Removes a product image.
     *
     * @param productId the ID of the product
     * @param imageUrl the URL of the image to remove
     * @throws ResourceNotFoundException if the product is not found
     * @throws IllegalArgumentException if the image URL is not found
     */
    @Transactional
    public void removeProductImage(final Long productId, final String imageUrl) {
        // Get product
        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check if image URL exists
        if (product.getImageUrls() == null || !product.getImageUrls().contains(imageUrl)) {
            throw new IllegalArgumentException("Image URL not found");
        }

        // Remove image URL from product
        product.removeImage(imageUrl);
        productRepository.save(product);

        // Delete file from disk if it exists
        try {
            String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but don't fail the operation
            System.err.println("Error deleting image file: " + e.getMessage());
        }
    }

    /**
     * Maps a Product entity to a ProductResponse DTO.
     *
     * @param product the product entity
     * @return the product response DTO
     */
    private ProductResponse mapToProductResponse(final Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());
        response.setImageUrls(product.getImageUrls());
        response.setActive(true); // Active status not implemented in entity yet
        response.setFeatured(product.isFeatured());
        response.setBrand(product.getBrand() != null ? product.getBrand().getName() : null);
        response.setModel(product.getModel());
        response.setSku(product.getSku());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }

    /**
     * Maps a Product entity to a ProductDetailResponse DTO.
     *
     * @param product the product entity
     * @return the product detail response DTO
     */
    private ProductDetailResponse mapToProductDetailResponse(final Product product) {
        ProductDetailResponse response = new ProductDetailResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setLowStockThreshold(product.getLowStockThreshold());
        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());
        response.setImageUrls(product.getImageUrls());
        response.setSpecifications(product.getSpecifications());
        response.setActive(true); // Active status not implemented in entity yet
        response.setFeatured(product.isFeatured());
        response.setWeightKg(product.getWeightKg());
        response.setDimensions(product.getDimensions());
        response.setBrand(product.getBrand() != null ? product.getBrand().getName() : null);
        response.setModel(product.getModel());
        response.setSku(product.getSku());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }

    /**
     * Maps a Product entity to a ProductSearchResponse DTO.
     *
     * @param product the product entity
     * @return the product search response DTO
     */
    private ProductSearchResponse mapToProductSearchResponse(final Product product) {
        ProductSearchResponse response = new ProductSearchResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());
        response.setMainImageUrl(product.getMainImageUrl());
        response.setBrand(product.getBrand() != null ? product.getBrand().getName() : null);
        response.setSku(product.getSku());
        return response;
    }
}
