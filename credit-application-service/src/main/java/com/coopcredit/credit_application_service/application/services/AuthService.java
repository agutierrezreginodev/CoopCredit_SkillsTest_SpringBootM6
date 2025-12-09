package com.coopcredit.credit_application_service.application.services;

import com.coopcredit.credit_application_service.domain.exceptions.BusinessRuleException;
import com.coopcredit.credit_application_service.domain.model.User;
import com.coopcredit.credit_application_service.domain.ports.in.AuthUseCase;
import com.coopcredit.credit_application_service.domain.ports.out.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Servicio de aplicación: Implementa los casos de uso de Autenticación
 * La encriptación de contraseñas se maneja en la capa de infraestructura
 */
public class AuthService implements AuthUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepositoryPort userRepository;

    public AuthService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User registerUser(User user) {
        logger.info("Registrando nuevo usuario: {}", user.getUsername());
        
        // Validar reglas de negocio
        user.validate();
        
        // Verificar username único
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessRuleException("Ya existe un usuario con el username: " + user.getUsername());
        }
        
        // Verificar documento único si es afiliado
        if (user.getDocumento() != null && !user.getDocumento().isEmpty()) {
            Optional<User> existingByDoc = userRepository.findByDocumento(user.getDocumento());
            if (existingByDoc.isPresent()) {
                throw new BusinessRuleException("Ya existe un usuario con el documento: " + user.getDocumento());
            }
        }
        
        // El password será encriptado en el adaptador de infraestructura
        User saved = userRepository.save(user);
        logger.info("Usuario registrado exitosamente con ID: {}", saved.getId());
        
        return saved;
    }

    @Override
    public Optional<User> authenticateUser(String username, String password) {
        logger.debug("Autenticando usuario: {}", username);
        // La verificación de contraseña se hace en la capa de infraestructura con PasswordEncoder
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        logger.debug("Buscando usuario: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> getUserByDocumento(String documento) {
        logger.debug("Buscando usuario por documento: {}", documento);
        return userRepository.findByDocumento(documento);
    }
}
