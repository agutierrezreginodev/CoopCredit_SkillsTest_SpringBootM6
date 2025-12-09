package com.coopcredit.credit_application_service.domain.ports.out;

import com.coopcredit.credit_application_service.domain.enums.ApplicationStatus;
import com.coopcredit.credit_application_service.domain.model.CreditApplication;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: Persistencia de Solicitudes de Crédito
 */
public interface CreditApplicationRepositoryPort {
    
    CreditApplication save(CreditApplication application);
    
    Optional<CreditApplication> findById(Long id);
    
    List<CreditApplication> findAll();
    
    List<CreditApplication> findByAfiliadoId(Long afiliadoId);
    
    List<CreditApplication> findByEstado(ApplicationStatus estado);
    
    void deleteById(Long id);
}
