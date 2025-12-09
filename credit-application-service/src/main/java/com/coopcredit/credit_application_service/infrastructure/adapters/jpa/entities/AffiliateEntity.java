package com.coopcredit.credit_application_service.infrastructure.adapters.jpa.entities;

import com.coopcredit.credit_application_service.domain.enums.AffiliateStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad JPA: Afiliado
 * Pertenece a la capa de infraestructura
 */
@Entity
@Table(name = "affiliates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 20)
    private String documento;
    
    @Column(nullable = false, length = 200)
    private String nombre;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal salario;
    
    @Column(name = "fecha_afiliacion", nullable = false)
    private LocalDate fechaAfiliacion;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AffiliateStatus estado;
}
