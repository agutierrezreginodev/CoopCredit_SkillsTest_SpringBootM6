package com.coopcredit.credit_application_service.domain.model;

import com.coopcredit.credit_application_service.domain.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Modelo de dominio puro: Evaluación de Riesgo
 * Sin dependencias de frameworks
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskEvaluation {
    
    private Long id;
    private String documento;
    private Integer score;
    private RiskLevel nivelRiesgo;
    private String detalle;
    private LocalDateTime fechaEvaluacion;

    /**
     * Determina si la evaluación representa un riesgo aceptable
     */
    public boolean isRiskAcceptable() {
        return !RiskLevel.ALTO.equals(this.nivelRiesgo);
    }

    /**
     * Determina si el score es suficiente para aprobación
     * @param scoreMinimo score mínimo requerido
     */
    public boolean cumpleScoreMinimo(int scoreMinimo) {
        return this.score != null && this.score >= scoreMinimo;
    }
}
