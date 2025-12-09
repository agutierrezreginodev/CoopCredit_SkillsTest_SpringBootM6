package com.coopcredit.credit_application_service.infrastructure.web.mapper;

import com.coopcredit.credit_application_service.domain.model.Affiliate;
import com.coopcredit.credit_application_service.infrastructure.adapters.jpa.entities.AffiliateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper: Convierte entre Affiliate (dominio) y AffiliateEntity (JPA)
 * MapStruct genera la implementación automáticamente
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AffiliateMapper {
    
    Affiliate toDomain(AffiliateEntity entity);
    
    AffiliateEntity toEntity(Affiliate domain);
}
