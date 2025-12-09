package com.coopcredit.credit_application_service.infrastructure.adapters.jpa.entities;

import com.coopcredit.credit_application_service.domain.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA: Solicitud de Crédito
 * Pertenece a la capa de infraestructura
 */
@Entity
@Table(name = "credit_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditApplicationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "afiliado_id", nullable = false)
    private Long afiliadoId;
    
    @Column(name = "monto_solicitado", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoSolicitado;
    
    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;
    
    @Column(name = "tasa_propuesta", nullable = false, precision = 5, scale = 2)
    private BigDecimal tasaPropuesta;
    
    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus estado;
    
    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluacion_riesgo_id", referencedColumnName = "id")
    private RiskEvaluationEntity evaluacionRiesgo;
}
