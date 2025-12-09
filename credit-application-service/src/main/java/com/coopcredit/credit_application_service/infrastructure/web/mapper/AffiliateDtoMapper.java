package com.coopcredit.credit_application_service.infrastructure.web.mapper;

import com.coopcredit.credit_application_service.domain.model.Affiliate;
import com.coopcredit.credit_application_service.infrastructure.web.dto.AffiliateDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper: Convierte entre Affiliate (dominio) y AffiliateDto (API)
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AffiliateDtoMapper {
    
    AffiliateDto toDto(Affiliate domain);
    
    Affiliate toDomain(AffiliateDto dto);
}
