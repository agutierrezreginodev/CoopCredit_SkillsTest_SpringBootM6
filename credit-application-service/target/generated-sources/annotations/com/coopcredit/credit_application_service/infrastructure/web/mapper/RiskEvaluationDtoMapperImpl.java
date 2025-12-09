package com.coopcredit.credit_application_service.infrastructure.web.mapper;

import com.coopcredit.credit_application_service.domain.model.RiskEvaluation;
import com.coopcredit.credit_application_service.infrastructure.web.dto.RiskEvaluationDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-09T17:34:08-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.17 (Ubuntu)"
)
@Component
public class RiskEvaluationDtoMapperImpl implements RiskEvaluationDtoMapper {

    @Override
    public RiskEvaluationDto toDto(RiskEvaluation domain) {
        if ( domain == null ) {
            return null;
        }

        RiskEvaluationDto.RiskEvaluationDtoBuilder riskEvaluationDto = RiskEvaluationDto.builder();

        riskEvaluationDto.id( domain.getId() );
        riskEvaluationDto.documento( domain.getDocumento() );
        riskEvaluationDto.score( domain.getScore() );
        riskEvaluationDto.nivelRiesgo( domain.getNivelRiesgo() );
        riskEvaluationDto.detalle( domain.getDetalle() );
        riskEvaluationDto.fechaEvaluacion( domain.getFechaEvaluacion() );

        return riskEvaluationDto.build();
    }

    @Override
    public RiskEvaluation toDomain(RiskEvaluationDto dto) {
        if ( dto == null ) {
            return null;
        }

        RiskEvaluation.RiskEvaluationBuilder riskEvaluation = RiskEvaluation.builder();

        riskEvaluation.id( dto.getId() );
        riskEvaluation.documento( dto.getDocumento() );
        riskEvaluation.score( dto.getScore() );
        riskEvaluation.nivelRiesgo( dto.getNivelRiesgo() );
        riskEvaluation.detalle( dto.getDetalle() );
        riskEvaluation.fechaEvaluacion( dto.getFechaEvaluacion() );

        return riskEvaluation.build();
    }
}
