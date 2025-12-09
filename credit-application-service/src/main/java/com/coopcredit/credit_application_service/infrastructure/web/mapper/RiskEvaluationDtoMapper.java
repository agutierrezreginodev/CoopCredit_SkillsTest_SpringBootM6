package com.coopcredit.credit_application_service.infrastructure.web.mapper;

import com.coopcredit.credit_application_service.domain.model.RiskEvaluation;
import com.coopcredit.credit_application_service.infrastructure.web.dto.RiskEvaluationDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper: Convierte entre RiskEvaluation (dominio) y RiskEvaluationDto (API)
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RiskEvaluationDtoMapper {
    
    RiskEvaluationDto toDto(RiskEvaluation domain);
    
    RiskEvaluation toDomain(RiskEvaluationDto dto);
}
