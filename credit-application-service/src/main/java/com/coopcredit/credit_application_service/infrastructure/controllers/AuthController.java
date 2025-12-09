package com.coopcredit.credit_application_service.infrastructure.controllers;

import com.coopcredit.credit_application_service.domain.model.User;
import com.coopcredit.credit_application_service.domain.ports.in.AuthUseCase;
import com.coopcredit.credit_application_service.infrastructure.security.services.JwtService;
import com.coopcredit.credit_application_service.infrastructure.web.dto.AuthResponse;
import com.coopcredit.credit_application_service.infrastructure.web.dto.LoginRequest;
import com.coopcredit.credit_application_service.infrastructure.web.dto.RegisterRequest;
import com.coopcredit.credit_application_service.infrastructure.web.mapper.UserDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST: Autenticación (Login y Registro)
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@Tag(name = "Autenticación", description = "Endpoints para login y registro de usuarios")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthUseCase authUseCase;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserDtoMapper userMapper;

    public AuthController(
            AuthUseCase authUseCase,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            UserDtoMapper userMapper) {
        this.authUseCase = authUseCase;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea un nuevo usuario en el sistema")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registrando nuevo usuario: {}", request.getUsername());
        
        User user = userMapper.toDomain(request);
        User savedUser = authUseCase.registerUser(user);
        
        String token = jwtService.generateToken(savedUser.getUsername(), savedUser.getRole().name());
        
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .role(savedUser.getRole().name())
                .mensaje("Usuario registrado exitosamente")
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Intentando login para usuario: {}", request.getUsername());
        
        User user = authUseCase.getUserByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .mensaje("Login exitoso")
                .build();
        
        return ResponseEntity.ok(response);
    }
}
