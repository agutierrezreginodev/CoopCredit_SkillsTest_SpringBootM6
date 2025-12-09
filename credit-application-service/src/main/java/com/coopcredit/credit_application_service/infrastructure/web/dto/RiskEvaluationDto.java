package com.coopcredit.credit_application_service.infrastructure.web.dto;

import com.coopcredit.credit_application_service.domain.enums.RiskLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO: Evaluación de Riesgo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskEvaluationDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String documento;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer score;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private RiskLevel nivelRiesgo;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String detalle;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaEvaluacion;
}
