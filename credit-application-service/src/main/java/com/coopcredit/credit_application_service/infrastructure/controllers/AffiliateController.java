package com.coopcredit.credit_application_service.infrastructure.controllers;

import com.coopcredit.credit_application_service.domain.model.Affiliate;
import com.coopcredit.credit_application_service.domain.ports.in.AffiliateUseCase;
import com.coopcredit.credit_application_service.infrastructure.web.dto.AffiliateDto;
import com.coopcredit.credit_application_service.infrastructure.web.mapper.AffiliateDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST: Gestión de Afiliados
 */
@RestController
@RequestMapping("/api/affiliates")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Afiliados", description = "Endpoints para gestión de afiliados")
public class AffiliateController {
    
    private static final Logger logger = LoggerFactory.getLogger(AffiliateController.class);
    
    private final AffiliateUseCase affiliateUseCase;
    private final AffiliateDtoMapper affiliateMapper;

    public AffiliateController(AffiliateUseCase affiliateUseCase, AffiliateDtoMapper affiliateMapper) {
        this.affiliateUseCase = affiliateUseCase;
        this.affiliateMapper = affiliateMapper;
    }

    @PostMapping
    @Operation(summary = "Registrar afiliado", description = "Crea un nuevo afiliado en el sistema")
    public ResponseEntity<AffiliateDto> createAffiliate(@Valid @RequestBody AffiliateDto dto) {
        logger.info("Creando afiliado con documento: {}", dto.getDocumento());
        
        Affiliate affiliate = affiliateMapper.toDomain(dto);
        Affiliate saved = affiliateUseCase.registerAffiliate(affiliate);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(affiliateMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar afiliado", description = "Actualiza la información de un afiliado")
    public ResponseEntity<AffiliateDto> updateAffiliate(
            @PathVariable Long id,
            @Valid @RequestBody AffiliateDto dto) {
        logger.info("Actualizando afiliado ID: {}", id);
        
        Affiliate affiliate = affiliateMapper.toDomain(dto);
        Affiliate updated = affiliateUseCase.updateAffiliate(id, affiliate);
        
        return ResponseEntity.ok(affiliateMapper.toDto(updated));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener afiliado por ID", description = "Devuelve un afiliado específico")
    public ResponseEntity<AffiliateDto> getAffiliateById(@PathVariable Long id) {
        logger.info("Consultando afiliado ID: {}", id);
        
        return affiliateUseCase.getAffiliateById(id)
                .map(affiliateMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/documento/{documento}")
    @Operation(summary = "Obtener afiliado por documento", description = "Busca un afiliado por su documento")
    public ResponseEntity<AffiliateDto> getAffiliateByDocumento(@PathVariable String documento) {
        logger.info("Consultando afiliado con documento: {}", documento);
        
        return affiliateUseCase.getAffiliateByDocumento(documento)
                .map(affiliateMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Listar afiliados", description = "Devuelve todos los afiliados")
    public ResponseEntity<List<AffiliateDto>> getAllAffiliates() {
        logger.info("Listando todos los afiliados");
        
        List<AffiliateDto> affiliates = affiliateUseCase.getAllAffiliates().stream()
                .map(affiliateMapper::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(affiliates);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado", description = "Cambia el estado de un afiliado (ACTIVO/INACTIVO)")
    public ResponseEntity<AffiliateDto> changeStatus(
            @PathVariable Long id,
            @RequestParam String newStatus) {
        logger.info("Cambiando estado del afiliado {} a {}", id, newStatus);
        
        Affiliate updated = affiliateUseCase.changeStatus(id, newStatus);
        
        return ResponseEntity.ok(affiliateMapper.toDto(updated));
    }
}
