package com.coopcredit.credit_application_service.infrastructure.adapters.jpa;

import com.coopcredit.credit_application_service.domain.enums.ApplicationStatus;
import com.coopcredit.credit_application_service.domain.model.CreditApplication;
import com.coopcredit.credit_application_service.domain.ports.out.CreditApplicationRepositoryPort;
import com.coopcredit.credit_application_service.infrastructure.adapters.jpa.repositories.CreditApplicationJpaRepository;
import com.coopcredit.credit_application_service.infrastructure.web.mapper.CreditApplicationMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador JPA: Implementa el puerto de persistencia de Solicitudes de Crédito
 * Traduce entre el dominio y la infraestructura JPA
 */
@Component
public class CreditApplicationRepositoryAdapter implements CreditApplicationRepositoryPort {
    
    private final CreditApplicationJpaRepository jpaRepository;
    private final CreditApplicationMapper mapper;

    public CreditApplicationRepositoryAdapter(
            CreditApplicationJpaRepository jpaRepository,
            CreditApplicationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public CreditApplication save(CreditApplication application) {
        var entity = mapper.toEntity(application);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CreditApplication> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<CreditApplication> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CreditApplication> findByAfiliadoId(Long afiliadoId) {
        return jpaRepository.findByAfiliadoId(afiliadoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CreditApplication> findByEstado(ApplicationStatus estado) {
        return jpaRepository.findByEstado(estado).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
