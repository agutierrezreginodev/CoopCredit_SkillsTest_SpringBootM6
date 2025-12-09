package com.coopcredit.credit_application_service.domain.ports.in;

import com.coopcredit.credit_application_service.domain.model.CreditApplication;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada: Casos de uso de Solicitudes de Crédito
 * Define las operaciones disponibles sin detalles de implementación
 */
public interface CreditApplicationUseCase {
    
    /**
     * Crea una nueva solicitud de crédito
     */
    CreditApplication createApplication(CreditApplication application);
    
    /**
     * Evalúa una solicitud de crédito (proceso completo)
     */
    CreditApplication evaluateApplication(Long applicationId);
    
    /**
     * Obtiene una solicitud por ID
     */
    Optional<CreditApplication> getApplicationById(Long id);
    
    /**
     * Lista todas las solicitudes
     */
    List<CreditApplication> getAllApplications();
    
    /**
     * Lista solicitudes por afiliado
     */
    List<CreditApplication> getApplicationsByAffiliate(Long afiliadoId);
    
    /**
     * Lista solicitudes pendientes
     */
    List<CreditApplication> getPendingApplications();
    
    /**
     * Lista solicitudes por estado
     */
    List<CreditApplication> getApplicationsByStatus(String status);
}
