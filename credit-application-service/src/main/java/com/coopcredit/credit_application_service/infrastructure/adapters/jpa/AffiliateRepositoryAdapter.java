package com.coopcredit.credit_application_service.infrastructure.adapters.jpa;

import com.coopcredit.credit_application_service.domain.model.Affiliate;
import com.coopcredit.credit_application_service.domain.ports.out.AffiliateRepositoryPort;
import com.coopcredit.credit_application_service.infrastructure.adapters.jpa.repositories.AffiliateJpaRepository;
import com.coopcredit.credit_application_service.infrastructure.web.mapper.AffiliateMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador JPA: Implementa el puerto de persistencia de Afiliados
 * Traduce entre el dominio y la infraestructura JPA
 */
@Component
public class AffiliateRepositoryAdapter implements AffiliateRepositoryPort {
    
    private final AffiliateJpaRepository jpaRepository;
    private final AffiliateMapper mapper;

    public AffiliateRepositoryAdapter(AffiliateJpaRepository jpaRepository, AffiliateMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Affiliate save(Affiliate affiliate) {
        var entity = mapper.toEntity(affiliate);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Affiliate> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Affiliate> findByDocumento(String documento) {
        return jpaRepository.findByDocumento(documento)
                .map(mapper::toDomain);
    }

    @Override
    public List<Affiliate> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByDocumento(String documento) {
        return jpaRepository.existsByDocumento(documento);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
