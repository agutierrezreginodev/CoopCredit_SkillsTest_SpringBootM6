package com.coopcredit.credit_application_service.domain.ports.out;

import com.coopcredit.credit_application_service.domain.model.User;

import java.util.Optional;

/**
 * Puerto de salida: Persistencia de Usuarios
 */
public interface UserRepositoryPort {
    
    User save(User user);
    
    Optional<User> findById(Long id);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByDocumento(String documento);
    
    boolean existsByUsername(String username);
}
