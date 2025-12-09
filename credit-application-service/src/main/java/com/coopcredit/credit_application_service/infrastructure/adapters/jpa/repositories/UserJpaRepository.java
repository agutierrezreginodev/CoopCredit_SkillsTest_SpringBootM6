package com.coopcredit.credit_application_service.infrastructure.adapters.jpa.repositories;

import com.coopcredit.credit_application_service.infrastructure.adapters.jpa.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA Spring Data: Usuarios
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    
    Optional<UserEntity> findByUsername(String username);
    
    Optional<UserEntity> findByDocumento(String documento);
    
    boolean existsByUsername(String username);
}
