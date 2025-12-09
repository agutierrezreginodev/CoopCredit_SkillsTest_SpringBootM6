package com.coopcredit.credit_application_service.application.services;

import com.coopcredit.credit_application_service.domain.enums.AffiliateStatus;
import com.coopcredit.credit_application_service.domain.enums.ApplicationStatus;
import com.coopcredit.credit_application_service.domain.enums.RiskLevel;
import com.coopcredit.credit_application_service.domain.exceptions.BusinessRuleException;
import com.coopcredit.credit_application_service.domain.exceptions.ResourceNotFoundException;
import com.coopcredit.credit_application_service.domain.model.Affiliate;
import com.coopcredit.credit_application_service.domain.model.CreditApplication;
import com.coopcredit.credit_application_service.domain.model.RiskEvaluation;
import com.coopcredit.credit_application_service.domain.ports.out.AffiliateRepositoryPort;
import com.coopcredit.credit_application_service.domain.ports.out.CreditApplicationRepositoryPort;
import com.coopcredit.credit_application_service.domain.ports.out.RiskCentralPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditApplicationService - Tests")
class CreditApplicationServiceTest {

    @Mock
    private CreditApplicationRepositoryPort applicationRepository;

    @Mock
    private AffiliateRepositoryPort affiliateRepository;

    @Mock
    private RiskCentralPort riskCentralPort;

    @InjectMocks
    private CreditApplicationService service;

    private Affiliate activeAffiliate;
    private CreditApplication pendingApplication;
    private RiskEvaluation goodRiskEvaluation;

    @BeforeEach
    void setUp() {
        // Afiliado activo con antigüedad mayor a 6 meses
        activeAffiliate = new Affiliate();
        activeAffiliate.setId(1L);
        activeAffiliate.setDocumento("1017654311");
        activeAffiliate.setNombre("Juan Pérez");
        activeAffiliate.setSalario(new BigDecimal("5000000"));
        activeAffiliate.setFechaAfiliacion(LocalDate.now().minusMonths(12));
        activeAffiliate.setEstado(AffiliateStatus.ACTIVO);

        // Solicitud pendiente
        pendingApplication = new CreditApplication();
        pendingApplication.setId(1L);
        pendingApplication.setAfiliadoId(1L);
        pendingApplication.setMontoSolicitado(new BigDecimal("10000000"));
        pendingApplication.setPlazoMeses(36);
        pendingApplication.setTasaPropuesta(new BigDecimal("12.5"));
        pendingApplication.setFechaSolicitud(LocalDateTime.now());
        pendingApplication.setEstado(ApplicationStatus.PENDIENTE);

        // Evaluación de riesgo favorable
        goodRiskEvaluation = new RiskEvaluation();
        goodRiskEvaluation.setId(1L);
        goodRiskEvaluation.setDocumento("1017654311");
        goodRiskEvaluation.setScore(750);
        goodRiskEvaluation.setNivelRiesgo(RiskLevel.MEDIO);
        goodRiskEvaluation.setDetalle("Evaluación satisfactoria");
        goodRiskEvaluation.setFechaEvaluacion(LocalDateTime.now());
    }

    // ========================================================================
    // CREATE APPLICATION TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe crear una solicitud exitosamente cuando el afiliado está activo")
    void shouldCreateApplicationSuccessfully() {
        // Given
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(activeAffiliate));
        when(applicationRepository.save(any(CreditApplication.class))).thenReturn(pendingApplication);

        // When
        CreditApplication result = service.createApplication(pendingApplication);

        // Then
        assertNotNull(result);
        assertEquals(ApplicationStatus.PENDIENTE, result.getEstado());
        assertNotNull(result.getFechaSolicitud());
        
        // Verificar que se buscó el afiliado
        verify(affiliateRepository, times(1)).findById(1L);
        
        // Verificar que se guardó la solicitud
        ArgumentCaptor<CreditApplication> captor = ArgumentCaptor.forClass(CreditApplication.class);
        verify(applicationRepository, times(1)).save(captor.capture());
        
        CreditApplication savedApp = captor.getValue();
        assertEquals(ApplicationStatus.PENDIENTE, savedApp.getEstado());
        assertNotNull(savedApp.getFechaSolicitud());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el afiliado no existe")
    void shouldThrowExceptionWhenAffiliateNotFound() {
        // Given
        when(affiliateRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.createApplication(pendingApplication)
        );

        assertTrue(exception.getMessage().contains("Afiliado no encontrado"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el afiliado está inactivo")
    void shouldThrowExceptionWhenAffiliateIsInactive() {
        // Given
        activeAffiliate.setEstado(AffiliateStatus.INACTIVO);
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(activeAffiliate));

        // When & Then
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> service.createApplication(pendingApplication)
        );

        assertTrue(exception.getMessage().contains("debe estar en estado ACTIVO"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe validar que el monto solicitado sea positivo")
    void shouldValidatePositiveAmount() {
        // Given
        pendingApplication.setMontoSolicitado(new BigDecimal("-1000"));
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(activeAffiliate));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> service.createApplication(pendingApplication));
        verify(applicationRepository, never()).save(any());
    }

    // ========================================================================
    // EVALUATE APPLICATION TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe aprobar solicitud cuando cumple todas las reglas de negocio")
    void shouldApproveApplicationWhenAllRulesPassed() {
        // Given
        pendingApplication.setMontoSolicitado(new BigDecimal("10000000")); // 2x salario (dentro del límite)
        
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(activeAffiliate));
        when(riskCentralPort.evaluateRisk(anyString(), any(BigDecimal.class))).thenReturn(goodRiskEvaluation);
        when(applicationRepository.save(any(CreditApplication.class))).thenReturn(pendingApplication);

        // When
        CreditApplication result = service.evaluateApplication(1L);

        // Then
        assertNotNull(result);
        assertEquals(ApplicationStatus.APROBADO, result.getEstado());
        assertNull(result.getMotivoRechazo());
        
        // Verificar interacciones
        verify(applicationRepository, times(1)).findById(1L);
        verify(affiliateRepository, times(1)).findById(1L);
        verify(riskCentralPort, times(1)).evaluateRisk(eq("1017654311"), eq(new BigDecimal("10000000")));
        verify(applicationRepository, times(1)).save(any(CreditApplication.class));
    }

    @Test
    @DisplayName("Debe rechazar solicitud cuando el afiliado está inactivo")
    void shouldRejectApplicationWhenAffiliateInactive() {
        // Given
        activeAffiliate.setEstado(AffiliateStatus.INACTIVO);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(activeAffiliate));
        when(applicationRepository.save(any(CreditApplication.class))).thenReturn(pendingApplication);

        // When
        CreditApplication result = service.evaluateApplication(1L);

        // Then
        assertEquals(ApplicationStatus.RECHAZADO, result.getEstado());
        assertTrue(result.getMotivoRechazo().contains("no está activo"));
        
        verify(riskCentralPort, never()).evaluateRisk(anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Debe rechazar solicitud cuando la antigüedad es menor a 6 meses")
    void shouldRejectApplicationWhenInsufficientSeniority() {
        // Given
        activeAffiliate.setFechaAfiliacion(LocalDate.now().minusMonths(3)); // Solo 3 meses
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(activeAffiliate));
        when(applicationRepository.save(any(CreditApplication.class))).thenReturn(pendingApplication);

        // When
        CreditApplication result = service.evaluateApplication(1L);

        // Then
        assertEquals(ApplicationStatus.RECHAZADO, result.getEstado());
        assertTrue(result.getMotivoRechazo().contains("antigüedad mínima"));
        
        verify(riskCentralPort, never()).evaluateRisk(anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Debe rechazar solicitud cuando el monto excede 3 veces el salario")
    void shouldRejectApplicationWhenAmountExceedsSalaryLimit() {
        // Given
        pendingApplication.setMontoSolicitado(new BigDecimal("20000000")); // 4x salario
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(activeAffiliate));
        when(applicationRepository.save(any(CreditApplication.class))).thenReturn(pendingApplication);

        // When
        CreditApplication result = service.evaluateApplication(1L);

        // Then
        assertEquals(ApplicationStatus.RECHAZADO, result.getEstado());
        assertTrue(result.getMotivoRechazo().contains("monto máximo"));
        
        verify(riskCentralPort, never()).evaluateRisk(anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Debe rechazar solicitud cuando el score es menor a 500")
    void shouldRejectApplicationWhenLowScore() {
        // Given
        goodRiskEvaluation.setScore(450); // Score bajo
        pendingApplication.setMontoSolicitado(new BigDecimal("10000000"));
        
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(activeAffiliate));
        when(riskCentralPort.evaluateRisk(anyString(), any(BigDecimal.class))).thenReturn(goodRiskEvaluation);
        when(applicationRepository.save(any(CreditApplication.class))).thenReturn(pendingApplication);

        // When
        CreditApplication result = service.evaluateApplication(1L);

        // Then
        assertEquals(ApplicationStatus.RECHAZADO, result.getEstado());
        assertTrue(result.getMotivoRechazo().contains("score crediticio"));
        
        verify(riskCentralPort, times(1)).evaluateRisk(anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Debe rechazar solicitud cuando el nivel de riesgo es ALTO")
    void shouldRejectApplicationWhenHighRisk() {
        // Given
        goodRiskEvaluation.setNivelRiesgo(RiskLevel.ALTO);
        pendingApplication.setMontoSolicitado(new BigDecimal("10000000"));
        
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(activeAffiliate));
        when(riskCentralPort.evaluateRisk(anyString(), any(BigDecimal.class))).thenReturn(goodRiskEvaluation);
        when(applicationRepository.save(any(CreditApplication.class))).thenReturn(pendingApplication);

        // When
        CreditApplication result = service.evaluateApplication(1L);

        // Then
        assertEquals(ApplicationStatus.RECHAZADO, result.getEstado());
        assertTrue(result.getMotivoRechazo().contains("nivel de riesgo"));
    }

    @Test
    @DisplayName("Debe rechazar solicitud cuando el ratio cuota/ingreso excede el 40%")
    void shouldRejectApplicationWhenExcessivePaymentRatio() {
        // Given
        pendingApplication.setMontoSolicitado(new BigDecimal("15000000"));
        pendingApplication.setPlazoMeses(12); // Plazo corto = cuota alta
        
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(activeAffiliate));
        when(riskCentralPort.evaluateRisk(anyString(), any(BigDecimal.class))).thenReturn(goodRiskEvaluation);
        when(applicationRepository.save(any(CreditApplication.class))).thenReturn(pendingApplication);

        // When
        CreditApplication result = service.evaluateApplication(1L);

        // Then
        assertEquals(ApplicationStatus.RECHAZADO, result.getEstado());
        assertTrue(result.getMotivoRechazo().contains("ratio cuota/ingreso"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la solicitud no existe")
    void shouldThrowExceptionWhenApplicationNotFound() {
        // Given
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.evaluateApplication(999L)
        );

        assertTrue(exception.getMessage().contains("Solicitud no encontrada"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando se intenta evaluar una solicitud ya evaluada")
    void shouldThrowExceptionWhenApplicationAlreadyEvaluated() {
        // Given
        pendingApplication.setEstado(ApplicationStatus.APROBADO);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));

        // When & Then
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> service.evaluateApplication(1L)
        );

        assertTrue(exception.getMessage().contains("ya ha sido evaluada"));
        verify(affiliateRepository, never()).findById(anyLong());
    }

    // ========================================================================
    // GET APPLICATIONS TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe obtener todas las solicitudes pendientes")
    void shouldGetPendingApplications() {
        // Given
        CreditApplication app1 = new CreditApplication();
        app1.setEstado(ApplicationStatus.PENDIENTE);
        CreditApplication app2 = new CreditApplication();
        app2.setEstado(ApplicationStatus.PENDIENTE);
        
        List<CreditApplication> pendingApps = Arrays.asList(app1, app2);
        when(applicationRepository.findByEstado(ApplicationStatus.PENDIENTE)).thenReturn(pendingApps);

        // When
        List<CreditApplication> result = service.getPendingApplications();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(app -> app.getEstado() == ApplicationStatus.PENDIENTE));
        
        verify(applicationRepository, times(1)).findByEstado(ApplicationStatus.PENDIENTE);
    }

    @Test
    @DisplayName("Debe obtener solicitudes por afiliado")
    void shouldGetApplicationsByAffiliate() {
        // Given
        List<CreditApplication> affiliateApps = Arrays.asList(pendingApplication);
        when(applicationRepository.findByAfiliadoId(1L)).thenReturn(affiliateApps);

        // When
        List<CreditApplication> result = service.getApplicationsByAffiliate(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getAfiliadoId());
        
        verify(applicationRepository, times(1)).findByAfiliadoId(1L);
    }

    @Test
    @DisplayName("Debe obtener una solicitud por ID")
    void shouldGetApplicationById() {
        // Given
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));

        // When
        CreditApplication result = service.getApplicationById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        
        verify(applicationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no encuentra la solicitud por ID")
    void shouldThrowExceptionWhenApplicationByIdNotFound() {
        // Given
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> service.getApplicationById(999L));
    }

    // ========================================================================
    // EDGE CASES AND BOUNDARY TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe aprobar solicitud en el límite exacto de 3 veces el salario")
    void shouldApproveApplicationAtExactSalaryLimit() {
        // Given
        pendingApplication.setMontoSolicitado(new BigDecimal("15000000")); // Exactamente 3x salario
        pendingApplication.setPlazoMeses(60); // Plazo largo para ratio bajo
        
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(activeAffiliate));
        when(riskCentralPort.evaluateRisk(anyString(), any(BigDecimal.class))).thenReturn(goodRiskEvaluation);
        when(applicationRepository.save(any(CreditApplication.class))).thenReturn(pendingApplication);

        // When
        CreditApplication result = service.evaluateApplication(1L);

        // Then
        assertEquals(ApplicationStatus.APROBADO, result.getEstado());
    }

    @Test
    @DisplayName("Debe aprobar solicitud con score exactamente en 500")
    void shouldApproveApplicationWithMinimumScore() {
        // Given
        goodRiskEvaluation.setScore(500); // Score mínimo exacto
        pendingApplication.setMontoSolicitado(new BigDecimal("10000000"));
        pendingApplication.setPlazoMeses(60);
        
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(activeAffiliate));
        when(riskCentralPort.evaluateRisk(anyString(), any(BigDecimal.class))).thenReturn(goodRiskEvaluation);
        when(applicationRepository.save(any(CreditApplication.class))).thenReturn(pendingApplication);

        // When
        CreditApplication result = service.evaluateApplication(1L);

        // Then
        assertEquals(ApplicationStatus.APROBADO, result.getEstado());
    }

    @Test
    @DisplayName("Debe aprobar solicitud con antigüedad exactamente de 6 meses")
    void shouldApproveApplicationWithExactSixMonthsSeniority() {
        // Given
        activeAffiliate.setFechaAfiliacion(LocalDate.now().minusMonths(6)); // Exactamente 6 meses
        pendingApplication.setMontoSolicitado(new BigDecimal("10000000"));
        pendingApplication.setPlazoMeses(60);
        
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(pendingApplication));
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(activeAffiliate));
        when(riskCentralPort.evaluateRisk(anyString(), any(BigDecimal.class))).thenReturn(goodRiskEvaluation);
        when(applicationRepository.save(any(CreditApplication.class))).thenReturn(pendingApplication);

        // When
        CreditApplication result = service.evaluateApplication(1L);

        // Then
        assertEquals(ApplicationStatus.APROBADO, result.getEstado());
    }
}
