package com.coopcredit.credit_application_service.infrastructure.web.mapper;

import com.coopcredit.credit_application_service.domain.model.Affiliate;
import com.coopcredit.credit_application_service.infrastructure.web.dto.AffiliateDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-09T17:34:08-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.17 (Ubuntu)"
)
@Component
public class AffiliateDtoMapperImpl implements AffiliateDtoMapper {

    @Override
    public AffiliateDto toDto(Affiliate domain) {
        if ( domain == null ) {
            return null;
        }

        AffiliateDto.AffiliateDtoBuilder affiliateDto = AffiliateDto.builder();

        affiliateDto.id( domain.getId() );
        affiliateDto.documento( domain.getDocumento() );
        affiliateDto.nombre( domain.getNombre() );
        affiliateDto.salario( domain.getSalario() );
        affiliateDto.fechaAfiliacion( domain.getFechaAfiliacion() );
        affiliateDto.estado( domain.getEstado() );

        return affiliateDto.build();
    }

    @Override
    public Affiliate toDomain(AffiliateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Affiliate.AffiliateBuilder affiliate = Affiliate.builder();

        affiliate.id( dto.getId() );
        affiliate.documento( dto.getDocumento() );
        affiliate.nombre( dto.getNombre() );
        affiliate.salario( dto.getSalario() );
        affiliate.fechaAfiliacion( dto.getFechaAfiliacion() );
        affiliate.estado( dto.getEstado() );

        return affiliate.build();
    }
}
