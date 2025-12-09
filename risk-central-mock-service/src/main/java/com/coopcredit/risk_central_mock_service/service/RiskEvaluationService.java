package com.coopcredit.risk_central_mock_service.service;

import com.coopcredit.risk_central_mock_service.dto.RiskEvaluationResponse;
import org.springframework.stereotype.Service;

/**
 * Servicio de Evaluación de Riesgo (Mock)
 * Genera scores consistentes basados en el documento
 */
@Service
public class RiskEvaluationService {
    
    /**
     * Evalúa el riesgo crediticio de forma determinística
     * Un mismo documento siempre devuelve el mismo score y nivel
     */
    public RiskEvaluationResponse evaluateRisk(String documento, Double monto, Integer plazo) {
        // Generar seed basado en el documento
        int seed = generateSeedFromDocumento(documento);
        
        // Generar score entre 300 y 950 basado en seed
        int score = 300 + (seed % 651);
        
        // Determinar nivel de riesgo basado en score
        String nivelRiesgo;
        String detalle;
        
        if (score <= 500) {
            nivelRiesgo = "ALTO";
            detalle = "Historial crediticio deficiente. Alto riesgo de incumplimiento.";
        } else if (score <= 700) {
            nivelRiesgo = "MEDIO";
            detalle = "Historial crediticio moderado. Riesgo medio de incumplimiento.";
        } else {
            nivelRiesgo = "BAJO";
            detalle = "Excelente historial crediticio. Bajo riesgo de incumplimiento.";
        }
        
        return RiskEvaluationResponse.builder()
                .documento(documento)
                .score(score)
                .nivelRiesgo(nivelRiesgo)
                .detalle(detalle)
                .build();
    }
    
    /**
     * Genera un seed numérico consistente basado en el documento
     */
    private int generateSeedFromDocumento(String documento) {
        if (documento == null || documento.isEmpty()) {
            return 500;
        }
        
        int hash = documento.hashCode();
        // Convertir a positivo y limitar al rango
        return Math.abs(hash) % 1000;
    }
}
