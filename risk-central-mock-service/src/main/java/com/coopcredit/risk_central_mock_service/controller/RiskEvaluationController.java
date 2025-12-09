package com.coopcredit.risk_central_mock_service.controller;

import com.coopcredit.risk_central_mock_service.dto.RiskEvaluationRequest;
import com.coopcredit.risk_central_mock_service.dto.RiskEvaluationResponse;
import com.coopcredit.risk_central_mock_service.service.RiskEvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST: Evaluación de Riesgo Crediticio (Mock)
 * Simula una central de riesgo externa
 */
@RestController
@CrossOrigin(origins = "*")
public class RiskEvaluationController {
    
    private static final Logger logger = LoggerFactory.getLogger(RiskEvaluationController.class);
    
    private final RiskEvaluationService riskEvaluationService;

    public RiskEvaluationController(RiskEvaluationService riskEvaluationService) {
        this.riskEvaluationService = riskEvaluationService;
    }

    @PostMapping("/risk-evaluation")
    public ResponseEntity<RiskEvaluationResponse> evaluateRisk(
            @RequestBody RiskEvaluationRequest request) {
        
        logger.info("Evaluando riesgo - Documento: {}, Monto: {}, Plazo: {}", 
                request.getDocumento(), request.getMonto(), request.getPlazo());
        
        RiskEvaluationResponse response = riskEvaluationService.evaluateRisk(
                request.getDocumento(),
                request.getMonto(),
                request.getPlazo()
        );
        
        logger.info("Evaluación completada - Score: {}, Nivel: {}", 
                response.getScore(), response.getNivelRiesgo());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Risk Central Mock Service is running");
    }
}
