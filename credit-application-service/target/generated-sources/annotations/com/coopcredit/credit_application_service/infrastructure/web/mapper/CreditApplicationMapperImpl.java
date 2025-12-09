package com.coopcredit.credit_application_service.infrastructure.web.mapper;

import com.coopcredit.credit_application_service.domain.model.CreditApplication;
import com.coopcredit.credit_application_service.infrastructure.adapters.jpa.entities.CreditApplicationEntity;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-09T17:34:08-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.17 (Ubuntu)"
)
@Component
public class CreditApplicationMapperImpl implements CreditApplicationMapper {

    @Autowired
    private RiskEvaluationMapper riskEvaluationMapper;

    @Override
    public CreditApplication toDomain(CreditApplicationEntity entity) {
        if ( entity == null ) {
            return null;
        }

        CreditApplication.CreditApplicationBuilder creditApplication = CreditApplication.builder();

        creditApplication.id( entity.getId() );
        creditApplication.afiliadoId( entity.getAfiliadoId() );
        creditApplication.montoSolicitado( entity.getMontoSolicitado() );
        creditApplication.plazoMeses( entity.getPlazoMeses() );
        creditApplication.tasaPropuesta( entity.getTasaPropuesta() );
        creditApplication.fechaSolicitud( entity.getFechaSolicitud() );
        creditApplication.estado( entity.getEstado() );
        creditApplication.motivoRechazo( entity.getMotivoRechazo() );
        creditApplication.evaluacionRiesgo( riskEvaluationMapper.toDomain( entity.getEvaluacionRiesgo() ) );

        return creditApplication.build();
    }

    @Override
    public CreditApplicationEntity toEntity(CreditApplication domain) {
        if ( domain == null ) {
            return null;
        }

        CreditApplicationEntity.CreditApplicationEntityBuilder creditApplicationEntity = CreditApplicationEntity.builder();

        creditApplicationEntity.id( domain.getId() );
        creditApplicationEntity.afiliadoId( domain.getAfiliadoId() );
        creditApplicationEntity.montoSolicitado( domain.getMontoSolicitado() );
        creditApplicationEntity.plazoMeses( domain.getPlazoMeses() );
        creditApplicationEntity.tasaPropuesta( domain.getTasaPropuesta() );
        creditApplicationEntity.fechaSolicitud( domain.getFechaSolicitud() );
        creditApplicationEntity.estado( domain.getEstado() );
        creditApplicationEntity.motivoRechazo( domain.getMotivoRechazo() );
        creditApplicationEntity.evaluacionRiesgo( riskEvaluationMapper.toEntity( domain.getEvaluacionRiesgo() ) );

        return creditApplicationEntity.build();
    }
}
