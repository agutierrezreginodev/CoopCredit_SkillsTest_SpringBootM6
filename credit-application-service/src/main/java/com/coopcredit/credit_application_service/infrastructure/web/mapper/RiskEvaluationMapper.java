package com.coopcredit.credit_application_service.infrastructure.web.mapper;

import com.coopcredit.credit_application_service.domain.model.RiskEvaluation;
import com.coopcredit.credit_application_service.infrastructure.adapters.jpa.entities.RiskEvaluationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper: Convierte entre RiskEvaluation (dominio) y RiskEvaluationEntity (JPA)
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RiskEvaluationMapper {
    
    RiskEvaluation toDomain(RiskEvaluationEntity entity);
    
    RiskEvaluationEntity toEntity(RiskEvaluation domain);
}
