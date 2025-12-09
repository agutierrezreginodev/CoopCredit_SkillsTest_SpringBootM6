package com.coopcredit.credit_application_service.domain.model;

import com.coopcredit.credit_application_service.domain.enums.ApplicationStatus;
import com.coopcredit.credit_application_service.domain.exceptions.BusinessRuleException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Modelo de dominio puro: Solicitud de Crédito
 * Sin dependencias de frameworks
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditApplication {
    
    private Long id;
    private Long afiliadoId;
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;
    private BigDecimal tasaPropuesta;
    private LocalDateTime fechaSolicitud;
    private ApplicationStatus estado;
    private String motivoRechazo;
    private RiskEvaluation evaluacionRiesgo;

    /**
     * Valida las reglas de negocio de la solicitud
     */
    public void validate() {
        if (afiliadoId == null) {
            throw new BusinessRuleException("El afiliado es obligatorio");
        }
        
        if (montoSolicitado == null || montoSolicitado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("El monto solicitado debe ser mayor a 0");
        }
        
        if (plazoMeses == null || plazoMeses <= 0) {
            throw new BusinessRuleException("El plazo debe ser mayor a 0 meses");
        }
        
        if (plazoMeses > 120) {
            throw new BusinessRuleException("El plazo máximo es de 120 meses (10 años)");
        }
        
        if (tasaPropuesta == null || tasaPropuesta.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("La tasa propuesta debe ser mayor o igual a 0");
        }
        
        if (tasaPropuesta.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessRuleException("La tasa propuesta no puede ser mayor al 100%");
        }
        
        if (estado == null) {
            throw new BusinessRuleException("El estado de la solicitud es obligatorio");
        }
    }

    /**
     * Calcula la cuota mensual aproximada usando fórmula de préstamo
     */
    public BigDecimal calcularCuotaMensual() {
        if (montoSolicitado == null || plazoMeses == null || tasaPropuesta == null) {
            return BigDecimal.ZERO;
        }

        if (tasaPropuesta.compareTo(BigDecimal.ZERO) == 0) {
            return montoSolicitado.divide(BigDecimal.valueOf(plazoMeses), 2, RoundingMode.HALF_UP);
        }

        // Tasa mensual = tasa anual / 12 / 100
        BigDecimal tasaMensual = tasaPropuesta.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                                              .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        // Cuota = Monto * (i * (1+i)^n) / ((1+i)^n - 1)
        BigDecimal unoPlusTasa = BigDecimal.ONE.add(tasaMensual);
        BigDecimal potencia = unoPlusTasa.pow(plazoMeses);
        
        BigDecimal numerador = tasaMensual.multiply(potencia);
        BigDecimal denominador = potencia.subtract(BigDecimal.ONE);

        if (denominador.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return montoSolicitado.multiply(numerador).divide(denominador, 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el ratio cuota/ingreso
     * @param salarioAfiliado salario del afiliado
     */
    public BigDecimal calcularRatioCuotaIngreso(BigDecimal salarioAfiliado) {
        if (salarioAfiliado == null || salarioAfiliado.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal cuota = calcularCuotaMensual();
        return cuota.divide(salarioAfiliado, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    /**
     * Verifica si la solicitud está pendiente
     */
    public boolean isPending() {
        return ApplicationStatus.PENDIENTE.equals(this.estado);
    }

    /**
     * Aprueba la solicitud
     */
    public void aprobar() {
        this.estado = ApplicationStatus.APROBADO;
        this.motivoRechazo = null;
    }

    /**
     * Rechaza la solicitud con un motivo
     */
    public void rechazar(String motivo) {
        this.estado = ApplicationStatus.RECHAZADO;
        this.motivoRechazo = motivo;
    }
}
