package com.coopcredit.credit_application_service.domain.model;

import com.coopcredit.credit_application_service.domain.enums.AffiliateStatus;
import com.coopcredit.credit_application_service.domain.exceptions.BusinessRuleException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo de dominio puro: Afiliado
 * Sin dependencias de frameworks
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Affiliate {
    
    private Long id;
    private String documento;
    private String nombre;
    private BigDecimal salario;
    private LocalDate fechaAfiliacion;
    private AffiliateStatus estado;

    /**
     * Valida las reglas de negocio del afiliado
     */
    public void validate() {
        if (documento == null || documento.trim().isEmpty()) {
            throw new BusinessRuleException("El documento del afiliado es obligatorio");
        }
        
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BusinessRuleException("El nombre del afiliado es obligatorio");
        }
        
        if (salario == null || salario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("El salario debe ser mayor a 0");
        }
        
        if (fechaAfiliacion == null) {
            throw new BusinessRuleException("La fecha de afiliación es obligatoria");
        }
        
        if (estado == null) {
            throw new BusinessRuleException("El estado del afiliado es obligatorio");
        }
    }

    /**
     * Verifica si el afiliado está activo
     */
    public boolean isActive() {
        return AffiliateStatus.ACTIVO.equals(this.estado);
    }

    /**
     * Verifica si el afiliado cumple con la antigüedad mínima
     * @param mesesMinimos meses mínimos de antigüedad requeridos
     */
    public boolean cumpleAntiguedad(int mesesMinimos) {
        if (fechaAfiliacion == null) {
            return false;
        }
        LocalDate fechaMinima = LocalDate.now().minusMonths(mesesMinimos);
        return fechaAfiliacion.isBefore(fechaMinima) || fechaAfiliacion.isEqual(fechaMinima);
    }

    /**
     * Calcula el monto máximo de crédito según el salario
     * @param multiplicadorSalario multiplicador del salario (ej: 3.0 = 3 veces el salario)
     */
    public BigDecimal calcularMontoMaximoCredito(double multiplicadorSalario) {
        if (salario == null) {
            return BigDecimal.ZERO;
        }
        return salario.multiply(BigDecimal.valueOf(multiplicadorSalario));
    }
}
