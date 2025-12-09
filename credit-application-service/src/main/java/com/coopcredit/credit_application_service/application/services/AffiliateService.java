package com.coopcredit.credit_application_service.application.services;

import com.coopcredit.credit_application_service.domain.enums.AffiliateStatus;
import com.coopcredit.credit_application_service.domain.exceptions.BusinessRuleException;
import com.coopcredit.credit_application_service.domain.exceptions.ResourceNotFoundException;
import com.coopcredit.credit_application_service.domain.model.Affiliate;
import com.coopcredit.credit_application_service.domain.ports.in.AffiliateUseCase;
import com.coopcredit.credit_application_service.domain.ports.out.AffiliateRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de aplicación: Implementa los casos de uso de Afiliados
 * Orquesta la lógica de negocio sin depender de frameworks externos
 */
public class AffiliateService implements AffiliateUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(AffiliateService.class);
    
    private final AffiliateRepositoryPort affiliateRepository;

    public AffiliateService(AffiliateRepositoryPort affiliateRepository) {
        this.affiliateRepository = affiliateRepository;
    }

    @Override
    public Affiliate registerAffiliate(Affiliate affiliate) {
        logger.info("Registrando nuevo afiliado con documento: {}", affiliate.getDocumento());
        
        // Validar reglas de negocio
        affiliate.validate();
        
        // Verificar documento único
        if (affiliateRepository.existsByDocumento(affiliate.getDocumento())) {
            throw new BusinessRuleException("Ya existe un afiliado con el documento: " + affiliate.getDocumento());
        }
        
        // Guardar
        Affiliate saved = affiliateRepository.save(affiliate);
        logger.info("Afiliado registrado exitosamente con ID: {}", saved.getId());
        
        return saved;
    }

    @Override
    public Affiliate updateAffiliate(Long id, Affiliate affiliate) {
        logger.info("Actualizando afiliado con ID: {}", id);
        
        // Verificar que existe
        Affiliate existing = affiliateRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Afiliado no encontrado con ID: " + id));
        
        // Validar reglas de negocio
        affiliate.validate();
        
        // Verificar documento único si cambió
        if (!existing.getDocumento().equals(affiliate.getDocumento())) {
            if (affiliateRepository.existsByDocumento(affiliate.getDocumento())) {
                throw new BusinessRuleException("Ya existe un afiliado con el documento: " + affiliate.getDocumento());
            }
        }
        
        // Actualizar
        affiliate.setId(id);
        Affiliate updated = affiliateRepository.save(affiliate);
        logger.info("Afiliado actualizado exitosamente");
        
        return updated;
    }

    @Override
    public Optional<Affiliate> getAffiliateById(Long id) {
        logger.debug("Buscando afiliado con ID: {}", id);
        return affiliateRepository.findById(id);
    }

    @Override
    public Optional<Affiliate> getAffiliateByDocumento(String documento) {
        logger.debug("Buscando afiliado con documento: {}", documento);
        return affiliateRepository.findByDocumento(documento);
    }

    @Override
    public List<Affiliate> getAllAffiliates() {
        logger.debug("Listando todos los afiliados");
        return affiliateRepository.findAll();
    }

    @Override
    public Affiliate changeStatus(Long id, String newStatus) {
        logger.info("Cambiando estado del afiliado {} a {}", id, newStatus);
        
        Affiliate affiliate = affiliateRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Afiliado no encontrado con ID: " + id));
        
        try {
            AffiliateStatus status = AffiliateStatus.valueOf(newStatus);
            affiliate.setEstado(status);
            
            Affiliate updated = affiliateRepository.save(affiliate);
            logger.info("Estado del afiliado actualizado exitosamente");
            
            return updated;
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Estado inválido: " + newStatus + ". Valores permitidos: ACTIVO, INACTIVO");
        }
    }
}
