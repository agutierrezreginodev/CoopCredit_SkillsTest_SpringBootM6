package com.coopcredit.credit_application_service.infrastructure.web.mapper;

import com.coopcredit.credit_application_service.domain.model.User;
import com.coopcredit.credit_application_service.infrastructure.web.dto.RegisterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper: Convierte entre User (dominio) y RegisterRequest (API)
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserDtoMapper {
    
    User toDomain(RegisterRequest request);
}
