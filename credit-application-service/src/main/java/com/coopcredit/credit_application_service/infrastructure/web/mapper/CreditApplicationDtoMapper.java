package com.coopcredit.credit_application_service.infrastructure.web.mapper;

import com.coopcredit.credit_application_service.domain.model.CreditApplication;
import com.coopcredit.credit_application_service.infrastructure.web.dto.CreditApplicationDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper: Convierte entre CreditApplication (dominio) y CreditApplicationDto (API)
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {RiskEvaluationDtoMapper.class})
public interface CreditApplicationDtoMapper {
    
    CreditApplicationDto toDto(CreditApplication domain);
    
    CreditApplication toDomain(CreditApplicationDto dto);
}
