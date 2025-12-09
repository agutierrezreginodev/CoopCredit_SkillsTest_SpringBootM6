package com.coopcredit.risk_central_mock_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: Request para evaluación de riesgo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskEvaluationRequest {
    private String documento;
    private Double monto;
    private Integer plazo;
}
