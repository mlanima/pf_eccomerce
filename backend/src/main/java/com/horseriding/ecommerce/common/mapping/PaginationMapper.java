package com.horseriding.ecommerce.common.mapping;

import com.horseriding.ecommerce.common.dtos.responses.PaginationResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Utility class for mapping Spring Data Page objects to PaginationResponse DTOs.
 */
@Component
public class PaginationMapper {

    /**
     * Convert Spring Data Page to PaginationResponse DTO
     * @param page Spring Data Page object
     * @param content List of mapped DTO content
     * @param <T> Type of DTO content
     * @return PaginationResponse with mapped content and pagination metadata
     */
    public <T> PaginationResponse<T> toPaginationResponse(Page<?> page, List<T> content) {
        return new PaginationResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }

    /**
     * Create PaginationResponse from manual pagination data
     * @param content List of DTO content
     * @param page Current page number (0-based)
     * @param size Page size
     * @param totalElements Total number of elements
     * @param <T> Type of DTO content
     * @return PaginationResponse with provided data
     */
    public <T> PaginationResponse<T> toPaginationResponse(List<T> content, int page, int size, long totalElements) {
        return new PaginationResponse<>(content, page, size, totalElements);
    }
}