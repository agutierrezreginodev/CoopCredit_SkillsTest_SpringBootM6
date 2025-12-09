package com.coopcredit.credit_application_service.infrastructure.adapters.jpa.entities;

import com.coopcredit.credit_application_service.domain.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA: Evaluación de Riesgo
 * Pertenece a la capa de infraestructura
 */
@Entity
@Table(name = "risk_evaluations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskEvaluationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20)
    private String documento;
    
    @Column(nullable = false)
    private Integer score;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_riesgo", nullable = false, length = 20)
    private RiskLevel nivelRiesgo;
    
    @Column(columnDefinition = "TEXT")
    private String detalle;
    
    @Column(name = "fecha_evaluacion", nullable = false)
    private LocalDateTime fechaEvaluacion;
}
