package com.coopcredit.credit_application_service.domain.ports.in;

import com.coopcredit.credit_application_service.domain.model.User;

import java.util.Optional;

/**
 * Puerto de entrada: Casos de uso de Autenticación
 */
public interface AuthUseCase {
    
    /**
     * Registra un nuevo usuario
     */
    User registerUser(User user);
    
    /**
     * Autentica un usuario
     */
    Optional<User> authenticateUser(String username, String password);
    
    /**
     * Obtiene un usuario por username
     */
    Optional<User> getUserByUsername(String username);
    
    /**
     * Obtiene un usuario por documento (para afiliados)
     */
    Optional<User> getUserByDocumento(String documento);
}
