package com.coopcredit.credit_application_service.application.services;

import com.coopcredit.credit_application_service.domain.enums.ApplicationStatus;
import com.coopcredit.credit_application_service.domain.exceptions.BusinessRuleException;
import com.coopcredit.credit_application_service.domain.exceptions.ResourceNotFoundException;
import com.coopcredit.credit_application_service.domain.model.Affiliate;
import com.coopcredit.credit_application_service.domain.model.CreditApplication;
import com.coopcredit.credit_application_service.domain.model.RiskEvaluation;
import com.coopcredit.credit_application_service.domain.ports.in.CreditApplicationUseCase;
import com.coopcredit.credit_application_service.domain.ports.out.AffiliateRepositoryPort;
import com.coopcredit.credit_application_service.domain.ports.out.CreditApplicationRepositoryPort;
import com.coopcredit.credit_application_service.domain.ports.out.RiskCentralPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de aplicación: Implementa los casos de uso de Solicitudes de Crédito
 * Orquesta la lógica completa de evaluación de crédito
 */
public class CreditApplicationService implements CreditApplicationUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(CreditApplicationService.class);
    
    // Políticas de negocio configurables
    private static final int ANTIGUEDAD_MINIMA_MESES = 6;
    private static final double MULTIPLICADOR_SALARIO = 3.0;
    private static final int SCORE_MINIMO = 500;
    private static final double RATIO_CUOTA_INGRESO_MAXIMO = 40.0; // 40%
    
    private final CreditApplicationRepositoryPort applicationRepository;
    private final AffiliateRepositoryPort affiliateRepository;
    private final RiskCentralPort riskCentralPort;

    public CreditApplicationService(
            CreditApplicationRepositoryPort applicationRepository,
            AffiliateRepositoryPort affiliateRepository,
            RiskCentralPort riskCentralPort) {
        this.applicationRepository = applicationRepository;
        this.affiliateRepository = affiliateRepository;
        this.riskCentralPort = riskCentralPort;
    }

    @Override
    @Transactional
    public CreditApplication createApplication(CreditApplication application) {
        logger.info("Creando nueva solicitud de crédito para afiliado ID: {}", application.getAfiliadoId());
        
        // Validar reglas básicas de la solicitud
        application.validate();
        
        // Verificar que el afiliado existe y está activo
        Affiliate affiliate = affiliateRepository.findById(application.getAfiliadoId())
            .orElseThrow(() -> new ResourceNotFoundException("Afiliado no encontrado con ID: " + application.getAfiliadoId()));
        
        if (!affiliate.isActive()) {
            throw new BusinessRuleException("El afiliado debe estar en estado ACTIVO para solicitar crédito");
        }
        
        // Establecer valores iniciales
        application.setFechaSolicitud(LocalDateTime.now());
        application.setEstado(ApplicationStatus.PENDIENTE);
        
        // Guardar la solicitud
        CreditApplication saved = applicationRepository.save(application);
        logger.info("Solicitud de crédito creada exitosamente con ID: {}", saved.getId());
        
        return saved;
    }

    @Override
    @Transactional
    public CreditApplication evaluateApplication(Long applicationId) {
        logger.info("Iniciando evaluación de solicitud ID: {}", applicationId);
        
        // Obtener la solicitud
        CreditApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + applicationId));
        
        // Verificar que está pendiente
        if (!application.isPending()) {
            throw new BusinessRuleException("La solicitud ya ha sido evaluada. Estado actual: " + application.getEstado());
        }
        
        // Obtener el afiliado
        Affiliate affiliate = affiliateRepository.findById(application.getAfiliadoId())
            .orElseThrow(() -> new ResourceNotFoundException("Afiliado no encontrado con ID: " + application.getAfiliadoId()));
        
        try {
            // 1. Verificar afiliado activo
            if (!affiliate.isActive()) {
                application.rechazar("El afiliado no está activo");
                return applicationRepository.save(application);
            }
            
            // 2. Verificar antigüedad mínima
            if (!affiliate.cumpleAntiguedad(ANTIGUEDAD_MINIMA_MESES)) {
                application.rechazar(String.format("El afiliado no cumple con la antigüedad mínima de %d meses", ANTIGUEDAD_MINIMA_MESES));
                return applicationRepository.save(application);
            }
            
            // 3. Verificar monto máximo según salario
            BigDecimal montoMaximo = affiliate.calcularMontoMaximoCredito(MULTIPLICADOR_SALARIO);
            if (application.getMontoSolicitado().compareTo(montoMaximo) > 0) {
                application.rechazar(String.format("El monto solicitado excede el máximo permitido de $%.2f (%.1f veces el salario)", 
                    montoMaximo, MULTIPLICADOR_SALARIO));
                return applicationRepository.save(application);
            }
            
            // 4. Evaluar riesgo con central externa
            logger.info("Consultando central de riesgo para documento: {}", affiliate.getDocumento());
            RiskEvaluation riskEvaluation = riskCentralPort.evaluateRisk(
                affiliate.getDocumento(),
                application.getMontoSolicitado().doubleValue(),
                application.getPlazoMeses()
            );
            
            application.setEvaluacionRiesgo(riskEvaluation);
            logger.info("Evaluación de riesgo recibida - Score: {}, Nivel: {}", 
                riskEvaluation.getScore(), riskEvaluation.getNivelRiesgo());
            
            // 5. Verificar score mínimo
            if (!riskEvaluation.cumpleScoreMinimo(SCORE_MINIMO)) {
                application.rechazar(String.format("Score crediticio insuficiente: %d (mínimo requerido: %d)", 
                    riskEvaluation.getScore(), SCORE_MINIMO));
                return applicationRepository.save(application);
            }
            
            // 6. Verificar nivel de riesgo aceptable
            if (!riskEvaluation.isRiskAcceptable()) {
                application.rechazar("Nivel de riesgo crediticio muy alto: " + riskEvaluation.getNivelRiesgo());
                return applicationRepository.save(application);
            }
            
            // 7. Verificar ratio cuota/ingreso
            BigDecimal ratioCuotaIngreso = application.calcularRatioCuotaIngreso(affiliate.getSalario());
            logger.info("Ratio cuota/ingreso calculado: {}%", ratioCuotaIngreso);
            
            if (ratioCuotaIngreso.compareTo(BigDecimal.valueOf(RATIO_CUOTA_INGRESO_MAXIMO)) > 0) {
                application.rechazar(String.format("Ratio cuota/ingreso excede el máximo permitido: %.2f%% (máximo: %.1f%%)", 
                    ratioCuotaIngreso, RATIO_CUOTA_INGRESO_MAXIMO));
                return applicationRepository.save(application);
            }
            
            // Todas las validaciones pasaron - APROBAR
            application.aprobar();
            logger.info("Solicitud APROBADA - ID: {}", applicationId);
            
            return applicationRepository.save(application);
            
        } catch (Exception e) {
            logger.error("Error durante la evaluación de la solicitud {}: {}", applicationId, e.getMessage(), e);
            application.rechazar("Error durante el proceso de evaluación: " + e.getMessage());
            return applicationRepository.save(application);
        }
    }

    @Override
    public Optional<CreditApplication> getApplicationById(Long id) {
        logger.debug("Buscando solicitud con ID: {}", id);
        return applicationRepository.findById(id);
    }

    @Override
    public List<CreditApplication> getAllApplications() {
        logger.debug("Listando todas las solicitudes");
        return applicationRepository.findAll();
    }

    @Override
    public List<CreditApplication> getApplicationsByAffiliate(Long afiliadoId) {
        logger.debug("Listando solicitudes del afiliado ID: {}", afiliadoId);
        return applicationRepository.findByAfiliadoId(afiliadoId);
    }

    @Override
    public List<CreditApplication> getPendingApplications() {
        logger.debug("Listando solicitudes pendientes");
        return applicationRepository.findByEstado(ApplicationStatus.PENDIENTE);
    }

    @Override
    public List<CreditApplication> getApplicationsByStatus(String status) {
        logger.debug("Listando solicitudes con estado: {}", status);
        try {
            ApplicationStatus applicationStatus = ApplicationStatus.valueOf(status);
            return applicationRepository.findByEstado(applicationStatus);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Estado inválido: " + status + ". Valores permitidos: PENDIENTE, APROBADO, RECHAZADO");
        }
    }
}
