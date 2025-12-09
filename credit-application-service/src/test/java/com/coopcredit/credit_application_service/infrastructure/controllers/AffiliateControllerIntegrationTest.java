package com.coopcredit.credit_application_service.infrastructure.controllers;

import com.coopcredit.credit_application_service.domain.enums.AffiliateStatus;
import com.coopcredit.credit_application_service.domain.model.Affiliate;
import com.coopcredit.credit_application_service.domain.ports.in.AffiliateUseCase;
import com.coopcredit.credit_application_service.infrastructure.security.filters.JwtAuthenticationFilter;
import com.coopcredit.credit_application_service.infrastructure.web.dto.AffiliateDto;
import com.coopcredit.credit_application_service.infrastructure.web.mapper.AffiliateDtoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AffiliateController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@DisplayName("AffiliateController - Integration Tests")
class AffiliateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AffiliateUseCase affiliateUseCase;

    @MockBean
    private AffiliateDtoMapper affiliateMapper;

    private Affiliate affiliate;
    private AffiliateDto affiliateRequest;

    @BeforeEach
    void setUp() {
        affiliate = new Affiliate();
        affiliate.setId(1L);
        affiliate.setDocumento("1017654311");
        affiliate.setNombre("Juan Pérez García");
        affiliate.setSalario(new BigDecimal("5000000"));
        affiliate.setFechaAfiliacion(LocalDate.of(2022, 1, 15));
        affiliate.setEstado(AffiliateStatus.ACTIVO);

        affiliateRequest = new AffiliateDto();
        affiliateRequest.setDocumento("1017654311");
        affiliateRequest.setNombre("Juan Pérez García");
        affiliateRequest.setSalario(new BigDecimal("5000000"));
        affiliateRequest.setFechaAfiliacion(LocalDate.of(2022, 1, 15));
        affiliateRequest.setEstado(AffiliateStatus.ACTIVO);
    }

    // ========================================================================
    // CREATE AFFILIATE TESTS
    // ========================================================================

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Debe crear un afiliado exitosamente como ADMIN")
    void shouldCreateAffiliateAsAdmin() throws Exception {
        // Given
        AffiliateDto responseDto = AffiliateDto.builder()
                .id(1L)
                .documento("1017654311")
                .nombre("Juan Pérez García")
                .salario(new BigDecimal("5000000"))
                .fechaAfiliacion(LocalDate.of(2022, 1, 15))
                .estado(AffiliateStatus.ACTIVO)
                .build();
        
        when(affiliateMapper.toDomain(any(AffiliateDto.class))).thenReturn(affiliate);
        when(affiliateUseCase.registerAffiliate(any(Affiliate.class))).thenReturn(affiliate);
        when(affiliateMapper.toDto(any(Affiliate.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/affiliates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(affiliateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.documento").value("1017654311"))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez García"))
                .andExpect(jsonPath("$.salario").value(5000000))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        verify(affiliateUseCase, times(1)).registerAffiliate(any(Affiliate.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ANALISTA")
    @DisplayName("Debe crear un afiliado exitosamente como ANALISTA")
    void shouldCreateAffiliateAsAnalista() throws Exception {
        // Given
        AffiliateDto responseDto = AffiliateDto.builder()
                .id(1L)
                .documento("1017654311")
                .nombre("Juan Pérez García")
                .salario(new BigDecimal("5000000"))
                .fechaAfiliacion(LocalDate.of(2022, 1, 15))
                .estado(AffiliateStatus.ACTIVO)
                .build();
        
        when(affiliateMapper.toDomain(any(AffiliateDto.class))).thenReturn(affiliate);
        when(affiliateUseCase.registerAffiliate(any(Affiliate.class))).thenReturn(affiliate);
        when(affiliateMapper.toDto(any(Affiliate.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/affiliates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(affiliateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(affiliateUseCase, times(1)).registerAffiliate(any(Affiliate.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_AFILIADO")
    @DisplayName("Debe retornar 403 cuando AFILIADO intenta crear afiliado")
    void shouldReturn403WhenAfiliadoTriesToCreateAffiliate() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/affiliates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(affiliateRequest)))
                .andExpect(status().isForbidden());

        verify(affiliateUseCase, never()).registerAffiliate(any(Affiliate.class));
    }

    @Test
    @DisplayName("Debe retornar 401 cuando no hay autenticación")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/affiliates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(affiliateRequest)))
                .andExpect(status().isUnauthorized());

        verify(affiliateUseCase, never()).registerAffiliate(any(Affiliate.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Debe retornar 400 cuando los datos son inválidos")
    void shouldReturn400WhenDataIsInvalid() throws Exception {
        // Given - Salario negativo
        affiliateRequest.setSalario(new BigDecimal("-1000"));

        // When & Then
        mockMvc.perform(post("/api/affiliates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(affiliateRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========================================================================
    // GET AFFILIATES TESTS
    // ========================================================================

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Debe obtener todos los afiliados")
    void shouldGetAllAffiliates() throws Exception {
        // Given
        Affiliate affiliate2 = new Affiliate();
        affiliate2.setId(2L);
        affiliate2.setDocumento("1023456789");
        affiliate2.setNombre("María González");
        affiliate2.setSalario(new BigDecimal("4500000"));
        affiliate2.setFechaAfiliacion(LocalDate.of(2021, 6, 20));
        affiliate2.setEstado(AffiliateStatus.ACTIVO);

        AffiliateDto dto1 = AffiliateDto.builder().id(1L).documento("1017654311").nombre("Juan Pérez García").salario(new BigDecimal("5000000")).fechaAfiliacion(LocalDate.of(2022, 1, 15)).estado(AffiliateStatus.ACTIVO).build();
        AffiliateDto dto2 = AffiliateDto.builder().id(2L).documento("1023456789").nombre("María González").salario(new BigDecimal("4500000")).fechaAfiliacion(LocalDate.of(2021, 6, 20)).estado(AffiliateStatus.ACTIVO).build();

        List<Affiliate> affiliates = Arrays.asList(affiliate, affiliate2);
        when(affiliateUseCase.getAllAffiliates()).thenReturn(affiliates);
        when(affiliateMapper.toDto(affiliate)).thenReturn(dto1);
        when(affiliateMapper.toDto(affiliate2)).thenReturn(dto2);

        // When & Then
        mockMvc.perform(get("/api/affiliates")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].documento").value("1017654311"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].documento").value("1023456789"));

        verify(affiliateUseCase, times(1)).getAllAffiliates();
    }

    @Test
    @WithMockUser(authorities = "ROLE_AFILIADO")
    @DisplayName("Debe permitir a AFILIADO obtener todos los afiliados")
    void shouldAllowAfiliadoToGetAllAffiliates() throws Exception {
        // Given
        AffiliateDto dto1 = AffiliateDto.builder().id(1L).documento("1017654311").nombre("Juan Pérez García").salario(new BigDecimal("5000000")).fechaAfiliacion(LocalDate.of(2022, 1, 15)).estado(AffiliateStatus.ACTIVO).build();
        when(affiliateUseCase.getAllAffiliates()).thenReturn(Arrays.asList(affiliate));
        when(affiliateMapper.toDto(affiliate)).thenReturn(dto1);

        // When & Then
        mockMvc.perform(get("/api/affiliates")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(affiliateUseCase, times(1)).getAllAffiliates();
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Debe obtener un afiliado por ID")
    void shouldGetAffiliateById() throws Exception {
        // Given
        AffiliateDto dto = AffiliateDto.builder().id(1L).documento("1017654311").nombre("Juan Pérez García").salario(new BigDecimal("5000000")).fechaAfiliacion(LocalDate.of(2022, 1, 15)).estado(AffiliateStatus.ACTIVO).build();
        when(affiliateUseCase.getAffiliateById(1L)).thenReturn(Optional.of(affiliate));
        when(affiliateMapper.toDto(affiliate)).thenReturn(dto);

        // When & Then
        mockMvc.perform(get("/api/affiliates/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.documento").value("1017654311"))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez García"));

        verify(affiliateUseCase, times(1)).getAffiliateById(1L);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Debe retornar 404 cuando el afiliado no existe")
    void shouldReturn404WhenAffiliateNotFound() throws Exception {
        // Given
        when(affiliateUseCase.getAffiliateById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/affiliates/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(affiliateUseCase, times(1)).getAffiliateById(999L);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Debe obtener un afiliado por documento")
    void shouldGetAffiliateByDocument() throws Exception {
        // Given
        AffiliateDto dto = AffiliateDto.builder().id(1L).documento("1017654311").nombre("Juan Pérez García").salario(new BigDecimal("5000000")).fechaAfiliacion(LocalDate.of(2022, 1, 15)).estado(AffiliateStatus.ACTIVO).build();
        when(affiliateUseCase.getAffiliateByDocumento("1017654311")).thenReturn(Optional.of(affiliate));
        when(affiliateMapper.toDto(affiliate)).thenReturn(dto);

        // When & Then
        mockMvc.perform(get("/api/affiliates/documento/1017654311")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.documento").value("1017654311"));

        verify(affiliateUseCase, times(1)).getAffiliateByDocumento("1017654311");
    }

    // ========================================================================
    // UPDATE AFFILIATE TESTS
    // ========================================================================

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Debe actualizar un afiliado exitosamente")
    void shouldUpdateAffiliateSuccessfully() throws Exception {
        // Given
        affiliateRequest.setNombre("Juan Pérez Actualizado");
        affiliateRequest.setSalario(new BigDecimal("6000000"));
        
        affiliate.setNombre("Juan Pérez Actualizado");
        affiliate.setSalario(new BigDecimal("6000000"));
        
        AffiliateDto responseDto = AffiliateDto.builder()
                .id(1L)
                .documento("1017654311")
                .nombre("Juan Pérez Actualizado")
                .salario(new BigDecimal("6000000"))
                .fechaAfiliacion(LocalDate.of(2022, 1, 15))
                .estado(AffiliateStatus.ACTIVO)
                .build();
        
        when(affiliateMapper.toDomain(any(AffiliateDto.class))).thenReturn(affiliate);
        when(affiliateUseCase.updateAffiliate(eq(1L), any(Affiliate.class))).thenReturn(affiliate);
        when(affiliateMapper.toDto(any(Affiliate.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(put("/api/affiliates/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(affiliateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Pérez Actualizado"))
                .andExpect(jsonPath("$.salario").value(6000000));

        verify(affiliateUseCase, times(1)).updateAffiliate(eq(1L), any(Affiliate.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ANALISTA")
    @DisplayName("Debe permitir a ANALISTA actualizar afiliado")
    void shouldAllowAnalistaToUpdateAffiliate() throws Exception {
        // Given
        AffiliateDto responseDto = AffiliateDto.builder()
                .id(1L)
                .documento("1017654311")
                .nombre("Juan Pérez García")
                .salario(new BigDecimal("5000000"))
                .fechaAfiliacion(LocalDate.of(2022, 1, 15))
                .estado(AffiliateStatus.ACTIVO)
                .build();
        
        when(affiliateMapper.toDomain(any(AffiliateDto.class))).thenReturn(affiliate);
        when(affiliateUseCase.updateAffiliate(eq(1L), any(Affiliate.class))).thenReturn(affiliate);
        when(affiliateMapper.toDto(any(Affiliate.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(put("/api/affiliates/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(affiliateRequest)))
                .andExpect(status().isOk());

        verify(affiliateUseCase, times(1)).updateAffiliate(eq(1L), any(Affiliate.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_AFILIADO")
    @DisplayName("Debe retornar 403 cuando AFILIADO intenta actualizar")
    void shouldReturn403WhenAfiliadoTriesToUpdate() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/affiliates/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(affiliateRequest)))
                .andExpect(status().isForbidden());

        verify(affiliateUseCase, never()).updateAffiliate(anyLong(), any(Affiliate.class));
    }

    // ========================================================================
    // CHANGE STATUS TESTS
    // ========================================================================

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Debe cambiar el estado de un afiliado")
    void shouldChangeAffiliateStatus() throws Exception {
        // Given
        affiliate.setEstado(AffiliateStatus.INACTIVO);
        AffiliateDto responseDto = AffiliateDto.builder()
                .id(1L)
                .documento("1017654311")
                .nombre("Juan Pérez García")
                .salario(new BigDecimal("5000000"))
                .fechaAfiliacion(LocalDate.of(2022, 1, 15))
                .estado(AffiliateStatus.INACTIVO)
                .build();
        
        when(affiliateUseCase.changeStatus(1L, "INACTIVO")).thenReturn(affiliate);
        when(affiliateMapper.toDto(any(Affiliate.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(patch("/api/affiliates/1/status")
                .with(csrf())
                .param("newStatus", "INACTIVO")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));

        verify(affiliateUseCase, times(1)).changeStatus(1L, "INACTIVO");
    }

    @Test
    @WithMockUser(authorities = "ROLE_ANALISTA")
    @DisplayName("Debe permitir a ANALISTA cambiar estado")
    void shouldAllowAnalistaToChangeStatus() throws Exception {
        // Given
        affiliate.setEstado(AffiliateStatus.INACTIVO);
        AffiliateDto responseDto = AffiliateDto.builder()
                .id(1L)
                .documento("1017654311")
                .nombre("Juan Pérez García")
                .salario(new BigDecimal("5000000"))
                .fechaAfiliacion(LocalDate.of(2022, 1, 15))
                .estado(AffiliateStatus.INACTIVO)
                .build();
        
        when(affiliateUseCase.changeStatus(1L, "INACTIVO")).thenReturn(affiliate);
        when(affiliateMapper.toDto(any(Affiliate.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(patch("/api/affiliates/1/status")
                .with(csrf())
                .param("newStatus", "INACTIVO")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(affiliateUseCase, times(1)).changeStatus(1L, "INACTIVO");
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Debe retornar 400 cuando el estado es inválido")
    void shouldReturn400WhenInvalidStatus() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/affiliates/1/status")
                .with(csrf())
                .param("newStatus", "INVALIDO")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========================================================================
    // VALIDATION TESTS
    // ========================================================================

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Debe validar que el documento no esté vacío")
    void shouldValidateDocumentoNotEmpty() throws Exception {
        // Given
        affiliateRequest.setDocumento("");

        // When & Then
        mockMvc.perform(post("/api/affiliates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(affiliateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Debe validar que el nombre no esté vacío")
    void shouldValidateNombreNotEmpty() throws Exception {
        // Given
        affiliateRequest.setNombre("");

        // When & Then
        mockMvc.perform(post("/api/affiliates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(affiliateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Debe validar que el salario sea positivo")
    void shouldValidateSalarioPositive() throws Exception {
        // Given
        affiliateRequest.setSalario(BigDecimal.ZERO);

        // When & Then
        mockMvc.perform(post("/api/affiliates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(affiliateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Debe validar que la fecha de afiliación no sea futura")
    void shouldValidateFechaAfiliacionNotFuture() throws Exception {
        // Given
        affiliateRequest.setFechaAfiliacion(LocalDate.now().plusDays(1));

        // When & Then
        mockMvc.perform(post("/api/affiliates")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(affiliateRequest)))
                .andExpect(status().isBadRequest());
    }
}
