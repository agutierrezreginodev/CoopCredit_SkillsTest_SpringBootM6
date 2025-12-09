package com.coopcredit.credit_application_service.domain.ports.out;

import com.coopcredit.credit_application_service.domain.model.Affiliate;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: Persistencia de Afiliados
 * El dominio define qué necesita, la infraestructura lo implementa
 */
public interface AffiliateRepositoryPort {
    
    Affiliate save(Affiliate affiliate);
    
    Optional<Affiliate> findById(Long id);
    
    Optional<Affiliate> findByDocumento(String documento);
    
    List<Affiliate> findAll();
    
    boolean existsByDocumento(String documento);
    
    void deleteById(Long id);
}
