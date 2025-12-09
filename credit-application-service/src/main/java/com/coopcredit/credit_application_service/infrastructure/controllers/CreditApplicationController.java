package com.coopcredit.credit_application_service.infrastructure.controllers;

import com.coopcredit.credit_application_service.domain.model.CreditApplication;
import com.coopcredit.credit_application_service.domain.ports.in.CreditApplicationUseCase;
import com.coopcredit.credit_application_service.infrastructure.web.dto.CreditApplicationDto;
import com.coopcredit.credit_application_service.infrastructure.web.mapper.CreditApplicationDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST: Gestión de Solicitudes de Crédito
 */
@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Solicitudes de Crédito", description = "Endpoints para gestión de solicitudes de crédito")
public class CreditApplicationController {
    
    private static final Logger logger = LoggerFactory.getLogger(CreditApplicationController.class);
    
    private final CreditApplicationUseCase applicationUseCase;
    private final CreditApplicationDtoMapper applicationMapper;

    public CreditApplicationController(
            CreditApplicationUseCase applicationUseCase,
            CreditApplicationDtoMapper applicationMapper) {
        this.applicationUseCase = applicationUseCase;
        this.applicationMapper = applicationMapper;
    }

    @PostMapping
    @Operation(summary = "Crear solicitud de crédito", description = "Registra una nueva solicitud de crédito")
    public ResponseEntity<CreditApplicationDto> createApplication(
            @Valid @RequestBody CreditApplicationDto dto,
            Authentication authentication) {
        logger.info("Creando solicitud de crédito para afiliado ID: {}", dto.getAfiliadoId());
        logger.debug("Usuario autenticado: {} con autoridades: {}", 
                authentication.getName(), authentication.getAuthorities());
        
        CreditApplication application = applicationMapper.toDomain(dto);
        CreditApplication saved = applicationUseCase.createApplication(application);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationMapper.toDto(saved));
    }

    @PostMapping("/{id}/evaluate")
    @Operation(summary = "Evaluar solicitud", description = "Ejecuta el proceso completo de evaluación de crédito")
    public ResponseEntity<CreditApplicationDto> evaluateApplication(@PathVariable Long id) {
        logger.info("Evaluando solicitud de crédito ID: {}", id);
        
        CreditApplication evaluated = applicationUseCase.evaluateApplication(id);
        
        return ResponseEntity.ok(applicationMapper.toDto(evaluated));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener solicitud por ID", description = "Devuelve una solicitud específica")
    public ResponseEntity<CreditApplicationDto> getApplicationById(@PathVariable Long id) {
        logger.info("Consultando solicitud ID: {}", id);
        
        return applicationUseCase.getApplicationById(id)
                .map(applicationMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar todas las solicitudes", description = "Devuelve todas las solicitudes de crédito")
    public ResponseEntity<List<CreditApplicationDto>> getAllApplications() {
        logger.info("Listando todas las solicitudes");
        
        List<CreditApplicationDto> applications = applicationUseCase.getAllApplications().stream()
                .map(applicationMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/affiliate/{afiliadoId}")
    @Operation(summary = "Listar solicitudes por afiliado", description = "Devuelve las solicitudes de un afiliado específico")
    public ResponseEntity<List<CreditApplicationDto>> getApplicationsByAffiliate(
            @PathVariable Long afiliadoId) {
        logger.info("Listando solicitudes del afiliado ID: {}", afiliadoId);
        
        List<CreditApplicationDto> applications = applicationUseCase.getApplicationsByAffiliate(afiliadoId).stream()
                .map(applicationMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/pending")
    @Operation(summary = "Listar solicitudes pendientes", description = "Devuelve todas las solicitudes en estado PENDIENTE")
    public ResponseEntity<List<CreditApplicationDto>> getPendingApplications() {
        logger.info("Listando solicitudes pendientes");
        
        List<CreditApplicationDto> applications = applicationUseCase.getPendingApplications().stream()
                .map(applicationMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar solicitudes por estado", description = "Devuelve solicitudes filtradas por estado")
    public ResponseEntity<List<CreditApplicationDto>> getApplicationsByStatus(@PathVariable String status) {
        logger.info("Listando solicitudes con estado: {}", status);
        
        List<CreditApplicationDto> applications = applicationUseCase.getApplicationsByStatus(status).stream()
                .map(applicationMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(applications);
    }
}
