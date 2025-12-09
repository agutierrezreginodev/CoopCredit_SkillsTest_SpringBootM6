package com.coopcredit.credit_application_service.infrastructure.web.dto;

import com.coopcredit.credit_application_service.domain.enums.AffiliateStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO: Afiliado para transferencia de datos en API REST
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateDto {
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    
    @NotBlank(message = "El documento es obligatorio")
    @Size(max = 20, message = "El documento no puede exceder 20 caracteres")
    private String documento;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;
    
    @NotNull(message = "El salario es obligatorio")
    @DecimalMin(value = "0.01", message = "El salario debe ser mayor a 0")
    private BigDecimal salario;
    
    @NotNull(message = "La fecha de afiliación es obligatoria")
    @PastOrPresent(message = "La fecha de afiliación no puede ser futura")
    private LocalDate fechaAfiliacion;
    
    @NotNull(message = "El estado es obligatorio")
    private AffiliateStatus estado;
}
