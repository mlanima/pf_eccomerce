package com.horseriding.ecommerce.common.mapping;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Common MapStruct configuration to be used by all mappers.
 * This ensures consistent mapping behavior across the application.
 */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface CommonMapperConfig {}
