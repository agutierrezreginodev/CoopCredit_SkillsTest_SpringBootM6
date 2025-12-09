package com.coopcredit.credit_application_service.infrastructure.web.mapper;

import com.coopcredit.credit_application_service.domain.model.CreditApplication;
import com.coopcredit.credit_application_service.infrastructure.web.dto.CreditApplicationDto;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-09T17:34:08-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.17 (Ubuntu)"
)
@Component
public class CreditApplicationDtoMapperImpl implements CreditApplicationDtoMapper {

    @Autowired
    private RiskEvaluationDtoMapper riskEvaluationDtoMapper;

    @Override
    public CreditApplicationDto toDto(CreditApplication domain) {
        if ( domain == null ) {
            return null;
        }

        CreditApplicationDto.CreditApplicationDtoBuilder creditApplicationDto = CreditApplicationDto.builder();

        creditApplicationDto.id( domain.getId() );
        creditApplicationDto.afiliadoId( domain.getAfiliadoId() );
        creditApplicationDto.montoSolicitado( domain.getMontoSolicitado() );
        creditApplicationDto.plazoMeses( domain.getPlazoMeses() );
        creditApplicationDto.tasaPropuesta( domain.getTasaPropuesta() );
        creditApplicationDto.fechaSolicitud( domain.getFechaSolicitud() );
        creditApplicationDto.estado( domain.getEstado() );
        creditApplicationDto.motivoRechazo( domain.getMotivoRechazo() );
        creditApplicationDto.evaluacionRiesgo( riskEvaluationDtoMapper.toDto( domain.getEvaluacionRiesgo() ) );

        return creditApplicationDto.build();
    }

    @Override
    public CreditApplication toDomain(CreditApplicationDto dto) {
        if ( dto == null ) {
            return null;
        }

        CreditApplication.CreditApplicationBuilder creditApplication = CreditApplication.builder();

        creditApplication.id( dto.getId() );
        creditApplication.afiliadoId( dto.getAfiliadoId() );
        creditApplication.montoSolicitado( dto.getMontoSolicitado() );
        creditApplication.plazoMeses( dto.getPlazoMeses() );
        creditApplication.tasaPropuesta( dto.getTasaPropuesta() );
        creditApplication.fechaSolicitud( dto.getFechaSolicitud() );
        creditApplication.estado( dto.getEstado() );
        creditApplication.motivoRechazo( dto.getMotivoRechazo() );
        creditApplication.evaluacionRiesgo( riskEvaluationDtoMapper.toDomain( dto.getEvaluacionRiesgo() ) );

        return creditApplication.build();
    }
}
