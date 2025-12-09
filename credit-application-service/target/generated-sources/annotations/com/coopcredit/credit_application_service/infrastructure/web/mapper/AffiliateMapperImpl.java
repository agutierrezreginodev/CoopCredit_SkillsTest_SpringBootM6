package com.coopcredit.credit_application_service.infrastructure.web.mapper;

import com.coopcredit.credit_application_service.domain.model.Affiliate;
import com.coopcredit.credit_application_service.infrastructure.adapters.jpa.entities.AffiliateEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-09T17:34:08-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.17 (Ubuntu)"
)
@Component
public class AffiliateMapperImpl implements AffiliateMapper {

    @Override
    public Affiliate toDomain(AffiliateEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Affiliate.AffiliateBuilder affiliate = Affiliate.builder();

        affiliate.id( entity.getId() );
        affiliate.documento( entity.getDocumento() );
        affiliate.nombre( entity.getNombre() );
        affiliate.salario( entity.getSalario() );
        affiliate.fechaAfiliacion( entity.getFechaAfiliacion() );
        affiliate.estado( entity.getEstado() );

        return affiliate.build();
    }

    @Override
    public AffiliateEntity toEntity(Affiliate domain) {
        if ( domain == null ) {
            return null;
        }

        AffiliateEntity.AffiliateEntityBuilder affiliateEntity = AffiliateEntity.builder();

        affiliateEntity.id( domain.getId() );
        affiliateEntity.documento( domain.getDocumento() );
        affiliateEntity.nombre( domain.getNombre() );
        affiliateEntity.salario( domain.getSalario() );
        affiliateEntity.fechaAfiliacion( domain.getFechaAfiliacion() );
        affiliateEntity.estado( domain.getEstado() );

        return affiliateEntity.build();
    }
}
