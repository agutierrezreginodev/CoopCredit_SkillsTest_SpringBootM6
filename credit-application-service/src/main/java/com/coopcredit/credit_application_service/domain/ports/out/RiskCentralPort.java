package com.coopcredit.credit_application_service.domain.ports.out;

import com.coopcredit.credit_application_service.domain.model.RiskEvaluation;

/**
 * Puerto de salida: Integración con Central de Riesgo
 * El dominio define qué necesita, la infraestructura implementa la comunicación REST
 */
public interface RiskCentralPort {
    
    /**
     * Evalúa el riesgo crediticio de un afiliado
     * @param documento documento del afiliado
     * @param monto monto solicitado
     * @param plazo plazo en meses
     * @return evaluación de riesgo
     */
    RiskEvaluation evaluateRisk(String documento, Double monto, Integer plazo);
}
