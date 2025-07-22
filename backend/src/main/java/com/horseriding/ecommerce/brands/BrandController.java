package com.horseriding.ecommerce.brands;

import com.horseriding.ecommerce.brands.dto.BrandResponse;
import com.horseriding.ecommerce.brands.dto.CreateBrandRequest;
import com.horseriding.ecommerce.brands.dto.UpdateBrandRequest;
import com.horseriding.ecommerce.brands.mapper.BrandMapper;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * REST controller for Brand entity operations.
 * Provides CRUD endpoints for brand management.
 */
@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Slf4j
public final class BrandController {

    /** Default page size for pagination. */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /** Default page number for pagination. */
    private static final int DEFAULT_PAGE_NUMBER = 0;

    /** Brand service for business logic. */
    private final BrandService brandService;

    /** Brand mapper for DTO conversions. */
    private final BrandMapper brandMapper;

    /**
     * Get all brands with pagination.
     *
     * @param page Page number (zero-based)
     * @param size Number of items per page
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc or desc)
     * @return Page of brands
     */
    @GetMapping
    public ResponseEntity<Page<BrandResponse>> getAllBrands(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size,
            @RequestParam(defaultValue = "name") final String sortBy,
            @RequestParam(defaultValue = "asc") final String sortDir) {

        log.debug(
                "GET /api/brands - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page,
                size,
                sortBy,
                sortDir);

        Sort sort =
                sortDir.equalsIgnoreCase("desc")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Brand> brandsPage = brandService.getAllBrands(pageable);

        List<BrandResponse> brandResponses = brandMapper.toResponseList(brandsPage.getContent());
        Page<BrandResponse> responsePage =
                new PageImpl<>(brandResponses, pageable, brandsPage.getTotalElements());

        return ResponseEntity.ok(responsePage);
    }

    /**
     * Get brand by ID.
     *
     * @param id Brand ID
     * @return Brand details
     */
    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable final Long id) {
        log.debug("GET /api/brands/{} - Fetching brand by ID", id);

        Brand brand = brandService.getBrandById(id);
        BrandResponse response = brandMapper.toResponse(brand);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new brand.
     *
     * @param request Brand creation request
     * @return Created brand
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<BrandResponse> createBrand(
            @Valid @RequestBody final CreateBrandRequest request) {
        log.info("POST /api/brands - Creating new brand: {}", request.getName());

        Brand brand = brandMapper.toEntity(request);
        Brand createdBrand = brandService.createBrand(brand);
        BrandResponse response = brandMapper.toResponse(createdBrand);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing brand.
     *
     * @param id Brand ID
     * @param request Brand update request
     * @return Updated brand
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<BrandResponse> updateBrand(
            @PathVariable final Long id, @Valid @RequestBody final UpdateBrandRequest request) {
        log.info("PUT /api/brands/{} - Updating brand", id);

        Brand existingBrand = brandService.getBrandById(id);
        Brand brandToUpdate = brandMapper.updateEntityFromRequest(existingBrand, request);
        Brand updatedBrand = brandService.updateBrand(id, brandToUpdate);
        BrandResponse response = brandMapper.toResponse(updatedBrand);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a brand.
     *
     * @param id Brand ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Void> deleteBrand(@PathVariable final Long id) {
        log.info("DELETE /api/brands/{} - Deleting brand", id);

        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}
