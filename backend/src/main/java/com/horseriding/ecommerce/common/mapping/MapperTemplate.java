package com.horseriding.ecommerce.common.mapping;

import org.mapstruct.Mapper;

/**
 * Template showing how to create MapStruct mappers using the common configuration.
 * This serves as an example for implementing feature-specific mappers.
 *
 * To create a new mapper:
 * 1. Extend BaseMapper<Entity, DTO>
 * 2. Use @Mapper(config = CommonMapperConfig.class)
 * 3. Add custom mappings as needed with @Mapping annotations
 * 4. Implement any custom mapping logic in default methods
 *
 * Example usage:
 *
 * @Mapper(config = CommonMapperConfig.class)
 * public interface UserMapper extends BaseMapper<User, UserResponse> {
 *
 *     @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
 *     @Mapping(target = "password", ignore = true)
 *     UserResponse toDto(User user);
 *
 *     @Mapping(target = "id", ignore = true)
 *     @Mapping(target = "createdAt", ignore = true)
 *     User toEntity(UserCreateRequest dto);
 *
 *     default String mapRole(UserRole role) {
 *         return role != null ? role.name() : null;
 *     }
 * }
 */
@Mapper(config = CommonMapperConfig.class)
public interface MapperTemplate {

    // This is a template interface - actual mappers should be created in their respective feature
    // packages
    // following the pattern shown in the comments above
}
