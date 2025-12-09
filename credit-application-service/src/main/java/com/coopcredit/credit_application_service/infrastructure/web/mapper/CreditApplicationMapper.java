package com.coopcredit.credit_application_service.infrastructure.web.mapper;

import com.coopcredit.credit_application_service.domain.model.CreditApplication;
import com.coopcredit.credit_application_service.infrastructure.adapters.jpa.entities.CreditApplicationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper: Convierte entre CreditApplication (dominio) y CreditApplicationEntity (JPA)
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {RiskEvaluationMapper.class})
public interface CreditApplicationMapper {
    
    CreditApplication toDomain(CreditApplicationEntity entity);
    
    CreditApplicationEntity toEntity(CreditApplication domain);
}
