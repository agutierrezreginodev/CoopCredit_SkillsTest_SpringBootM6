package com.coopcredit.credit_application_service.infrastructure.web.mapper;

import com.coopcredit.credit_application_service.domain.model.User;
import com.coopcredit.credit_application_service.infrastructure.adapters.jpa.entities.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper: Convierte entre User (dominio) y UserEntity (JPA)
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    
    User toDomain(UserEntity entity);
    
    UserEntity toEntity(User domain);
}
