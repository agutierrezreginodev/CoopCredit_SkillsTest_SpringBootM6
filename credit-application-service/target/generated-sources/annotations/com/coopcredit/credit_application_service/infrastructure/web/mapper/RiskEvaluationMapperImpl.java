package com.coopcredit.credit_application_service.infrastructure.web.mapper;

import com.coopcredit.credit_application_service.domain.model.RiskEvaluation;
import com.coopcredit.credit_application_service.infrastructure.adapters.jpa.entities.RiskEvaluationEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-09T17:34:08-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.17 (Ubuntu)"
)
@Component
public class RiskEvaluationMapperImpl implements RiskEvaluationMapper {

    @Override
    public RiskEvaluation toDomain(RiskEvaluationEntity entity) {
        if ( entity == null ) {
            return null;
        }

        RiskEvaluation.RiskEvaluationBuilder riskEvaluation = RiskEvaluation.builder();

        riskEvaluation.id( entity.getId() );
        riskEvaluation.documento( entity.getDocumento() );
        riskEvaluation.score( entity.getScore() );
        riskEvaluation.nivelRiesgo( entity.getNivelRiesgo() );
        riskEvaluation.detalle( entity.getDetalle() );
        riskEvaluation.fechaEvaluacion( entity.getFechaEvaluacion() );

        return riskEvaluation.build();
    }

    @Override
    public RiskEvaluationEntity toEntity(RiskEvaluation domain) {
        if ( domain == null ) {
            return null;
        }

        RiskEvaluationEntity.RiskEvaluationEntityBuilder riskEvaluationEntity = RiskEvaluationEntity.builder();

        riskEvaluationEntity.id( domain.getId() );
        riskEvaluationEntity.documento( domain.getDocumento() );
        riskEvaluationEntity.score( domain.getScore() );
        riskEvaluationEntity.nivelRiesgo( domain.getNivelRiesgo() );
        riskEvaluationEntity.detalle( domain.getDetalle() );
        riskEvaluationEntity.fechaEvaluacion( domain.getFechaEvaluacion() );

        return riskEvaluationEntity.build();
    }
}
