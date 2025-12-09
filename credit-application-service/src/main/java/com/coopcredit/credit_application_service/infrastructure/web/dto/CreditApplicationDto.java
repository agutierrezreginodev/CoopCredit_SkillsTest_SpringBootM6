package com.coopcredit.credit_application_service.infrastructure.web.dto;

import com.coopcredit.credit_application_service.domain.enums.ApplicationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO: Solicitud de Crédito para transferencia de datos en API REST
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditApplicationDto {
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    
    @NotNull(message = "El ID del afiliado es obligatorio")
    private Long afiliadoId;
    
    @NotNull(message = "El monto solicitado es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal montoSolicitado;
    
    @NotNull(message = "El plazo es obligatorio")
    @Min(value = 1, message = "El plazo debe ser al menos 1 mes")
    @Max(value = 120, message = "El plazo máximo es 120 meses")
    private Integer plazoMeses;
    
    @NotNull(message = "La tasa propuesta es obligatoria")
    @DecimalMin(value = "0.0", message = "La tasa debe ser mayor o igual a 0")
    @DecimalMax(value = "100.0", message = "La tasa no puede exceder 100%")
    private BigDecimal tasaPropuesta;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaSolicitud;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ApplicationStatus estado;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String motivoRechazo;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private RiskEvaluationDto evaluacionRiesgo;
}
