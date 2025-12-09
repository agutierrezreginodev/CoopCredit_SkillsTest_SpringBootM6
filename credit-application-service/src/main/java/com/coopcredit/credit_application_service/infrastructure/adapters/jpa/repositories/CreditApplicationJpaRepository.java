package com.coopcredit.credit_application_service.infrastructure.adapters.jpa.repositories;

import com.coopcredit.credit_application_service.domain.enums.ApplicationStatus;
import com.coopcredit.credit_application_service.infrastructure.adapters.jpa.entities.CreditApplicationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA Spring Data: Solicitudes de Crédito
 * Usa @EntityGraph para evitar problema N+1 con evaluación de riesgo
 */
@Repository
public interface CreditApplicationJpaRepository extends JpaRepository<CreditApplicationEntity, Long> {
    
    @EntityGraph(attributePaths = {"evaluacionRiesgo"})
    @Override
    Optional<CreditApplicationEntity> findById(Long id);
    
    @EntityGraph(attributePaths = {"evaluacionRiesgo"})
    @Override
    List<CreditApplicationEntity> findAll();
    
    @EntityGraph(attributePaths = {"evaluacionRiesgo"})
    List<CreditApplicationEntity> findByAfiliadoId(Long afiliadoId);
    
    @EntityGraph(attributePaths = {"evaluacionRiesgo"})
    List<CreditApplicationEntity> findByEstado(ApplicationStatus estado);
}
