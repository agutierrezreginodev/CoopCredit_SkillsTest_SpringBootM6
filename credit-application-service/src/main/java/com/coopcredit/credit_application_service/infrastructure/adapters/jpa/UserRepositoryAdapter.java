package com.coopcredit.credit_application_service.infrastructure.adapters.jpa;

import com.coopcredit.credit_application_service.domain.model.User;
import com.coopcredit.credit_application_service.domain.ports.out.UserRepositoryPort;
import com.coopcredit.credit_application_service.infrastructure.adapters.jpa.repositories.UserJpaRepository;
import com.coopcredit.credit_application_service.infrastructure.web.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador JPA: Implementa el puerto de persistencia de Usuarios
 * Incluye encriptación de contraseñas
 */
@Component
public class UserRepositoryAdapter implements UserRepositoryPort {
    
    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UserRepositoryAdapter(
            UserJpaRepository jpaRepository,
            UserMapper mapper,
            PasswordEncoder passwordEncoder) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User save(User user) {
        var entity = mapper.toEntity(user);
        
        // Encriptar contraseña si no está encriptada
        if (!entity.getPassword().startsWith("$2a$")) {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        }
        
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByDocumento(String documento) {
        return jpaRepository.findByDocumento(documento)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }
}
