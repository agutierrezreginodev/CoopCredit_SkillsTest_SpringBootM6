package com.coopcredit.credit_application_service.infrastructure.adapters.rest;

import com.coopcredit.credit_application_service.domain.enums.RiskLevel;
import com.coopcredit.credit_application_service.domain.model.RiskEvaluation;
import com.coopcredit.credit_application_service.domain.ports.out.RiskCentralPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Adaptador REST: Implementa la integración con el servicio externo de riesgo
 * Consume el microservicio risk-central-mock-service
 */
@Component
public class RiskCentralAdapter implements RiskCentralPort {
    
    private static final Logger logger = LoggerFactory.getLogger(RiskCentralAdapter.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${risk.central.url:http://localhost:8081/risk-evaluation}")
    private String riskCentralUrl;

    public RiskCentralAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public RiskEvaluation evaluateRisk(String documento, Double monto, Integer plazo) {
        logger.info("Consultando central de riesgo - Documento: {}, Monto: {}, Plazo: {}", 
                documento, monto, plazo);
        
        try {
            // Preparar request
            Map<String, Object> request = new HashMap<>();
            request.put("documento", documento);
            request.put("monto", monto);
            request.put("plazo", plazo);
            
            // Llamar al servicio externo
            RiskEvaluationResponse response = restTemplate.postForObject(
                    riskCentralUrl,
                    request,
                    RiskEvaluationResponse.class
            );
            
            if (response == null) {
                throw new RuntimeException("No se recibió respuesta de la central de riesgo");
            }
            
            logger.info("Respuesta de central de riesgo - Score: {}, Nivel: {}", 
                    response.getScore(), response.getNivelRiesgo());
            
            // Mapear a modelo de dominio
            return RiskEvaluation.builder()
                    .documento(response.getDocumento())
                    .score(response.getScore())
                    .nivelRiesgo(RiskLevel.valueOf(response.getNivelRiesgo()))
                    .detalle(response.getDetalle())
                    .fechaEvaluacion(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            logger.error("Error al consultar central de riesgo: {}", e.getMessage(), e);
            throw new RuntimeException("Error al consultar central de riesgo: " + e.getMessage(), e);
        }
    }
    
    /**
     * DTO para la respuesta del servicio de riesgo
     */
    private static class RiskEvaluationResponse {
        private String documento;
        private Integer score;
        private String nivelRiesgo;
        private String detalle;

        public String getDocumento() {
            return documento;
        }

        public void setDocumento(String documento) {
            this.documento = documento;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public String getNivelRiesgo() {
            return nivelRiesgo;
        }

        public void setNivelRiesgo(String nivelRiesgo) {
            this.nivelRiesgo = nivelRiesgo;
        }

        public String getDetalle() {
            return detalle;
        }

        public void setDetalle(String detalle) {
            this.detalle = detalle;
        }
    }
}
