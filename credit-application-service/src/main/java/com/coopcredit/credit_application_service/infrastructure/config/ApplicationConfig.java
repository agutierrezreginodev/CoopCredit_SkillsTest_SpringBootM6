package com.coopcredit.credit_application_service.infrastructure.config;

import com.coopcredit.credit_application_service.application.services.AffiliateService;
import com.coopcredit.credit_application_service.application.services.AuthService;
import com.coopcredit.credit_application_service.application.services.CreditApplicationService;
import com.coopcredit.credit_application_service.domain.ports.in.AffiliateUseCase;
import com.coopcredit.credit_application_service.domain.ports.in.AuthUseCase;
import com.coopcredit.credit_application_service.domain.ports.in.CreditApplicationUseCase;
import com.coopcredit.credit_application_service.domain.ports.out.AffiliateRepositoryPort;
import com.coopcredit.credit_application_service.domain.ports.out.CreditApplicationRepositoryPort;
import com.coopcredit.credit_application_service.domain.ports.out.RiskCentralPort;
import com.coopcredit.credit_application_service.domain.ports.out.UserRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de la aplicación
 * Define los beans de casos de uso (capa de aplicación)
 */
@Configuration
public class ApplicationConfig {

    /**
     * Bean del caso de uso de Afiliados
     */
    @Bean
    public AffiliateUseCase affiliateUseCase(AffiliateRepositoryPort affiliateRepository) {
        return new AffiliateService(affiliateRepository);
    }

    /**
     * Bean del caso de uso de Solicitudes de Crédito
     */
    @Bean
    public CreditApplicationUseCase creditApplicationUseCase(
            CreditApplicationRepositoryPort applicationRepository,
            AffiliateRepositoryPort affiliateRepository,
            RiskCentralPort riskCentralPort) {
        return new CreditApplicationService(applicationRepository, affiliateRepository, riskCentralPort);
    }

    /**
     * Bean del caso de uso de Autenticación
     */
    @Bean
    public AuthUseCase authUseCase(UserRepositoryPort userRepository) {
        return new AuthService(userRepository);
    }

    /**
     * Bean de RestTemplate para llamadas HTTP
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
