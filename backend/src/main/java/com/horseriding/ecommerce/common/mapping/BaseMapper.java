package com.horseriding.ecommerce.common.mapping;

import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Base interface for all MapStruct mappers providing common mapping methods.
 * @param <E> Entity type
 * @param <D> DTO type
 */
public interface BaseMapper<E, D> {

    /**
     * Convert entity to DTO
     */
    D toDto(E entity);

    /**
     * Convert DTO to entity
     */
    E toEntity(D dto);

    /**
     * Convert list of entities to list of DTOs
     */
    List<D> toDtoList(List<E> entities);

    /**
     * Convert list of DTOs to list of entities
     */
    List<E> toEntityList(List<D> dtos);

    /**
     * Update existing entity with DTO data
     */
    void updateEntityFromDto(D dto, @MappingTarget E entity);
}