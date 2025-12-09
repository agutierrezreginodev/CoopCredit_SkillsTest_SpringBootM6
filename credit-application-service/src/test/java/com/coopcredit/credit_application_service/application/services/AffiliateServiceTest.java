package com.coopcredit.credit_application_service.application.services;

import com.coopcredit.credit_application_service.domain.enums.AffiliateStatus;
import com.coopcredit.credit_application_service.domain.exceptions.BusinessRuleException;
import com.coopcredit.credit_application_service.domain.exceptions.ResourceNotFoundException;
import com.coopcredit.credit_application_service.domain.model.Affiliate;
import com.coopcredit.credit_application_service.domain.ports.out.AffiliateRepositoryPort;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AffiliateService - Tests")
class AffiliateServiceTest {

    @Mock
    private AffiliateRepositoryPort affiliateRepository;

    @InjectMocks
    private AffiliateService service;

    private Affiliate validAffiliate;

    @BeforeEach
    void setUp() {
        validAffiliate = new Affiliate();
        validAffiliate.setId(1L);
        validAffiliate.setDocumento("1017654311");
        validAffiliate.setNombre("Juan Pérez García");
        validAffiliate.setSalario(new BigDecimal("5000000"));
        validAffiliate.setFechaAfiliacion(LocalDate.now().minusMonths(6));
        validAffiliate.setEstado(AffiliateStatus.ACTIVO);
    }

    // ========================================================================
    // REGISTER AFFILIATE TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe registrar un afiliado exitosamente")
    void shouldRegisterAffiliateSuccessfully() {
        // Given
        when(affiliateRepository.existsByDocumento(anyString())).thenReturn(false);
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(validAffiliate);

        // When
        Affiliate result = service.registerAffiliate(validAffiliate);

        // Then
        assertNotNull(result);
        assertEquals("1017654311", result.getDocumento());
        assertEquals("Juan Pérez García", result.getNombre());
        
        // Verificar interacciones
        verify(affiliateRepository, times(1)).existsByDocumento("1017654311");
        
        ArgumentCaptor<Affiliate> captor = ArgumentCaptor.forClass(Affiliate.class);
        verify(affiliateRepository, times(1)).save(captor.capture());
        
        Affiliate savedAffiliate = captor.getValue();
        assertEquals("1017654311", savedAffiliate.getDocumento());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el documento ya existe")
    void shouldThrowExceptionWhenDocumentAlreadyExists() {
        // Given
        when(affiliateRepository.existsByDocumento("1017654311")).thenReturn(true);

        // When & Then
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> service.registerAffiliate(validAffiliate)
        );

        assertTrue(exception.getMessage().contains("Ya existe un afiliado"));
        verify(affiliateRepository, times(1)).existsByDocumento("1017654311");
        verify(affiliateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el salario es negativo")
    void shouldThrowExceptionWhenNegativeSalary() {
        // Given
        validAffiliate.setSalario(new BigDecimal("-1000"));
        when(affiliateRepository.existsByDocumento(anyString())).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> service.registerAffiliate(validAffiliate));
        verify(affiliateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el salario es cero")
    void shouldThrowExceptionWhenZeroSalary() {
        // Given
        validAffiliate.setSalario(BigDecimal.ZERO);
        when(affiliateRepository.existsByDocumento(anyString())).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> service.registerAffiliate(validAffiliate));
        verify(affiliateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el nombre está vacío")
    void shouldThrowExceptionWhenEmptyName() {
        // Given
        validAffiliate.setNombre("");
        when(affiliateRepository.existsByDocumento(anyString())).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> service.registerAffiliate(validAffiliate));
        verify(affiliateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el documento está vacío")
    void shouldThrowExceptionWhenEmptyDocument() {
        // Given
        validAffiliate.setDocumento("");
        when(affiliateRepository.existsByDocumento(anyString())).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> service.registerAffiliate(validAffiliate));
        verify(affiliateRepository, never()).save(any());
    }

    // ========================================================================
    // UPDATE AFFILIATE TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe actualizar un afiliado exitosamente")
    void shouldUpdateAffiliateSuccessfully() {
        // Given
        Affiliate updatedData = new Affiliate();
        updatedData.setDocumento("1017654311");
        updatedData.setNombre("Juan Pérez Actualizado");
        updatedData.setSalario(new BigDecimal("6000000"));
        updatedData.setFechaAfiliacion(LocalDate.now().minusMonths(12));
        updatedData.setEstado(AffiliateStatus.ACTIVO);
        
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(validAffiliate));
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(updatedData);

        // When
        Affiliate result = service.updateAffiliate(1L, updatedData);

        // Then
        assertNotNull(result);
        assertEquals("Juan Pérez Actualizado", result.getNombre());
        assertEquals(new BigDecimal("6000000"), result.getSalario());
        
        verify(affiliateRepository, times(1)).findById(1L);
        verify(affiliateRepository, times(1)).save(any(Affiliate.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el afiliado a actualizar no existe")
    void shouldThrowExceptionWhenAffiliateToUpdateNotFound() {
        // Given
        when(affiliateRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.updateAffiliate(999L, validAffiliate)
        );

        assertTrue(exception.getMessage().contains("Afiliado no encontrado"));
        verify(affiliateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe permitir actualizar cuando el documento no cambia")
    void shouldAllowUpdateWhenDocumentDoesNotChange() {
        // Given
        Affiliate updatedData = new Affiliate();
        updatedData.setDocumento("1017654311"); // Mismo documento
        updatedData.setNombre("Nuevo Nombre");
        updatedData.setSalario(new BigDecimal("7000000"));
        updatedData.setFechaAfiliacion(LocalDate.now().minusMonths(6));
        updatedData.setEstado(AffiliateStatus.ACTIVO);
        
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(validAffiliate));
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(updatedData);

        // When
        Affiliate result = service.updateAffiliate(1L, updatedData);

        // Then
        assertNotNull(result);
        verify(affiliateRepository, never()).existsByDocumento(anyString());
        verify(affiliateRepository, times(1)).save(any(Affiliate.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando se intenta cambiar a un documento que ya existe")
    void shouldThrowExceptionWhenChangingToExistingDocument() {
        // Given
        Affiliate updatedData = new Affiliate();
        updatedData.setDocumento("9999999999"); // Nuevo documento
        updatedData.setNombre("Juan Pérez");
        updatedData.setSalario(new BigDecimal("5000000"));
        updatedData.setFechaAfiliacion(LocalDate.now().minusMonths(6));
        updatedData.setEstado(AffiliateStatus.ACTIVO);
        
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(validAffiliate));
        when(affiliateRepository.existsByDocumento("9999999999")).thenReturn(true);

        // When & Then
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> service.updateAffiliate(1L, updatedData)
        );

        assertTrue(exception.getMessage().contains("Ya existe un afiliado"));
        verify(affiliateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe permitir actualizar a un nuevo documento único")
    void shouldAllowUpdateToNewUniqueDocument() {
        // Given
        Affiliate updatedData = new Affiliate();
        updatedData.setDocumento("9999999999"); // Nuevo documento único
        updatedData.setNombre("Juan Pérez");
        updatedData.setSalario(new BigDecimal("5000000"));
        updatedData.setFechaAfiliacion(LocalDate.now().minusMonths(6));
        updatedData.setEstado(AffiliateStatus.ACTIVO);
        
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(validAffiliate));
        when(affiliateRepository.existsByDocumento("9999999999")).thenReturn(false);
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(updatedData);

        // When
        Affiliate result = service.updateAffiliate(1L, updatedData);

        // Then
        assertNotNull(result);
        assertEquals("9999999999", result.getDocumento());
        verify(affiliateRepository, times(1)).existsByDocumento("9999999999");
        verify(affiliateRepository, times(1)).save(any(Affiliate.class));
    }

    // ========================================================================
    // GET AFFILIATE TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe obtener un afiliado por ID")
    void shouldGetAffiliateById() {
        // Given
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(validAffiliate));

        // When
        Optional<Affiliate> result = service.getAffiliateById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("1017654311", result.get().getDocumento());
        
        verify(affiliateRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando el afiliado no existe")
    void shouldReturnEmptyOptionalWhenAffiliateNotFound() {
        // Given
        when(affiliateRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Affiliate> result = service.getAffiliateById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(affiliateRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe obtener un afiliado por documento")
    void shouldGetAffiliateByDocument() {
        // Given
        when(affiliateRepository.findByDocumento("1017654311")).thenReturn(Optional.of(validAffiliate));

        // When
        Optional<Affiliate> result = service.getAffiliateByDocumento("1017654311");

        // Then
        assertTrue(result.isPresent());
        assertEquals("1017654311", result.get().getDocumento());
        
        verify(affiliateRepository, times(1)).findByDocumento("1017654311");
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando el documento no existe")
    void shouldReturnEmptyOptionalWhenDocumentNotFound() {
        // Given
        when(affiliateRepository.findByDocumento("9999999999")).thenReturn(Optional.empty());

        // When
        Optional<Affiliate> result = service.getAffiliateByDocumento("9999999999");

        // Then
        assertFalse(result.isPresent());
        verify(affiliateRepository, times(1)).findByDocumento("9999999999");
    }

    @Test
    @DisplayName("Debe obtener todos los afiliados")
    void shouldGetAllAffiliates() {
        // Given
        Affiliate affiliate2 = new Affiliate();
        affiliate2.setId(2L);
        affiliate2.setDocumento("1023456789");
        affiliate2.setNombre("María González");
        affiliate2.setSalario(new BigDecimal("4500000"));
        affiliate2.setFechaAfiliacion(LocalDate.now().minusYears(1));
        affiliate2.setEstado(AffiliateStatus.ACTIVO);
        
        List<Affiliate> affiliates = Arrays.asList(validAffiliate, affiliate2);
        when(affiliateRepository.findAll()).thenReturn(affiliates);

        // When
        List<Affiliate> result = service.getAllAffiliates();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1017654311", result.get(0).getDocumento());
        assertEquals("1023456789", result.get(1).getDocumento());
        
        verify(affiliateRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay afiliados")
    void shouldReturnEmptyListWhenNoAffiliates() {
        // Given
        when(affiliateRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Affiliate> result = service.getAllAffiliates();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(affiliateRepository, times(1)).findAll();
    }

    // ========================================================================
    // CHANGE STATUS TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe cambiar el estado a INACTIVO exitosamente")
    void shouldChangeStatusToInactiveSuccessfully() {
        // Given
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(validAffiliate));
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(validAffiliate);

        // When
        Affiliate result = service.changeStatus(1L, "INACTIVO");

        // Then
        assertNotNull(result);
        
        ArgumentCaptor<Affiliate> captor = ArgumentCaptor.forClass(Affiliate.class);
        verify(affiliateRepository, times(1)).save(captor.capture());
        
        Affiliate savedAffiliate = captor.getValue();
        assertEquals(AffiliateStatus.INACTIVO, savedAffiliate.getEstado());
    }

    @Test
    @DisplayName("Debe cambiar el estado a ACTIVO exitosamente")
    void shouldChangeStatusToActiveSuccessfully() {
        // Given
        validAffiliate.setEstado(AffiliateStatus.INACTIVO);
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(validAffiliate));
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(validAffiliate);

        // When
        Affiliate result = service.changeStatus(1L, "ACTIVO");

        // Then
        assertNotNull(result);
        
        ArgumentCaptor<Affiliate> captor = ArgumentCaptor.forClass(Affiliate.class);
        verify(affiliateRepository, times(1)).save(captor.capture());
        
        Affiliate savedAffiliate = captor.getValue();
        assertEquals(AffiliateStatus.ACTIVO, savedAffiliate.getEstado());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el afiliado no existe al cambiar estado")
    void shouldThrowExceptionWhenAffiliateNotFoundOnStatusChange() {
        // Given
        when(affiliateRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> service.changeStatus(999L, "INACTIVO")
        );

        assertTrue(exception.getMessage().contains("Afiliado no encontrado"));
        verify(affiliateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el estado es inválido")
    void shouldThrowExceptionWhenInvalidStatus() {
        // Given
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(validAffiliate));

        // When & Then
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> service.changeStatus(1L, "INVALIDO")
        );

        assertTrue(exception.getMessage().contains("Estado inválido"));
        assertTrue(exception.getMessage().contains("ACTIVO, INACTIVO"));
        verify(affiliateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe ser case-sensitive con los estados")
    void shouldBeCaseSensitiveWithStatuses() {
        // Given
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(validAffiliate));

        // When & Then
        assertThrows(BusinessRuleException.class, () -> service.changeStatus(1L, "activo"));
        assertThrows(BusinessRuleException.class, () -> service.changeStatus(1L, "Activo"));
        assertThrows(BusinessRuleException.class, () -> service.changeStatus(1L, "inactivo"));
        
        verify(affiliateRepository, never()).save(any());
    }

    // ========================================================================
    // INTEGRATION TESTS WITH MULTIPLE OPERATIONS
    // ========================================================================

    @Test
    @DisplayName("Debe realizar un flujo completo: crear, actualizar y cambiar estado")
    void shouldPerformCompleteFlow() {
        // Given
        when(affiliateRepository.existsByDocumento(anyString())).thenReturn(false);
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(validAffiliate);
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(validAffiliate));

        // When - Crear
        Affiliate created = service.registerAffiliate(validAffiliate);
        assertNotNull(created);

        // When - Actualizar
        Affiliate updated = new Affiliate();
        updated.setDocumento("1017654311");
        updated.setNombre("Nombre Actualizado");
        updated.setSalario(new BigDecimal("7000000"));
        updated.setFechaAfiliacion(LocalDate.now().minusMonths(6));
        updated.setEstado(AffiliateStatus.ACTIVO);
        
        Affiliate result = service.updateAffiliate(1L, updated);
        assertNotNull(result);

        // When - Cambiar estado
        Affiliate statusChanged = service.changeStatus(1L, "INACTIVO");
        assertNotNull(statusChanged);

        // Then - Verificar todas las interacciones
        verify(affiliateRepository, times(1)).existsByDocumento("1017654311");
        verify(affiliateRepository, times(3)).save(any(Affiliate.class));
        verify(affiliateRepository, times(2)).findById(1L);
    }
}
