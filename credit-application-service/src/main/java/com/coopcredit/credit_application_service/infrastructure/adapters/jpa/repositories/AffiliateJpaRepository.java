package com.coopcredit.credit_application_service.infrastructure.adapters.jpa.repositories;

import com.coopcredit.credit_application_service.infrastructure.adapters.jpa.entities.AffiliateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA Spring Data: Afiliados
 * Extiende JpaRepository para operaciones CRUD automáticas
 */
@Repository
public interface AffiliateJpaRepository extends JpaRepository<AffiliateEntity, Long> {
    
    Optional<AffiliateEntity> findByDocumento(String documento);
    
    boolean existsByDocumento(String documento);
}
