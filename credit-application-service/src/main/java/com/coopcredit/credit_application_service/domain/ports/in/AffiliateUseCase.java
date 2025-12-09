package com.coopcredit.credit_application_service.domain.ports.in;

import com.coopcredit.credit_application_service.domain.model.Affiliate;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada: Casos de uso de Afiliados
 * Define las operaciones disponibles sin detalles de implementación
 */
public interface AffiliateUseCase {
    
    /**
     * Registra un nuevo afiliado
     */
    Affiliate registerAffiliate(Affiliate affiliate);
    
    /**
     * Actualiza información de un afiliado
     */
    Affiliate updateAffiliate(Long id, Affiliate affiliate);
    
    /**
     * Obtiene un afiliado por ID
     */
    Optional<Affiliate> getAffiliateById(Long id);
    
    /**
     * Obtiene un afiliado por documento
     */
    Optional<Affiliate> getAffiliateByDocumento(String documento);
    
    /**
     * Lista todos los afiliados
     */
    List<Affiliate> getAllAffiliates();
    
    /**
     * Cambia el estado de un afiliado
     */
    Affiliate changeStatus(Long id, String newStatus);
}
