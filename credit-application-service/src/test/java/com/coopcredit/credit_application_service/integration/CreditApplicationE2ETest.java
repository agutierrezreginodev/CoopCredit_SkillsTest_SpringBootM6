package com.coopcredit.credit_application_service.integration;

import com.coopcredit.credit_application_service.domain.enums.AffiliateStatus;
import com.coopcredit.credit_application_service.domain.enums.ApplicationStatus;
import com.coopcredit.credit_application_service.infrastructure.adapters.jpa.repositories.AffiliateJpaRepository;
import com.coopcredit.credit_application_service.infrastructure.adapters.jpa.repositories.CreditApplicationJpaRepository;
import com.coopcredit.credit_application_service.infrastructure.web.dto.AffiliateDto;
import com.coopcredit.credit_application_service.infrastructure.web.dto.CreditApplicationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests End-to-End con Testcontainers
 * Prueba el flujo completo de la aplicación con base de datos real
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("CreditApplication - End-to-End Tests with Testcontainers")
class CreditApplicationE2ETest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("risk.central.url", () -> "http://localhost:8081/risk-evaluation");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AffiliateJpaRepository affiliateRepository;

    @Autowired
    private CreditApplicationJpaRepository applicationRepository;

    private AffiliateDto testAffiliate;
    private String uniqueDocument;

    @BeforeEach
    void setUp() {
        // Generar documento único para cada test
        uniqueDocument = "DOC" + UUID.randomUUID().toString().substring(0, 8);
        
        testAffiliate = AffiliateDto.builder()
                .documento(uniqueDocument)
                .nombre("Test User E2E")
                .salario(new BigDecimal("5000000"))
                .fechaAfiliacion(LocalDate.now().minusYears(1))
                .estado(AffiliateStatus.ACTIVO)
                .build();
    }

    @AfterEach
    void tearDown() {
        // Limpiar datos de prueba después de cada test
        applicationRepository.deleteAll();
        affiliateRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("E2E: Debe crear afiliado y solicitud de crédito")
    void shouldCreateAffiliateAndCreditApplication() throws Exception {
        // Paso 1: Crear afiliado
        MvcResult affiliateResult = mockMvc.perform(post("/api/affiliates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAffiliate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.documento").value(uniqueDocument))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andReturn();

        String affiliateJson = affiliateResult.getResponse().getContentAsString();
        AffiliateDto createdAffiliate = objectMapper.readValue(affiliateJson, AffiliateDto.class);
        assertThat(createdAffiliate.getId()).isNotNull();

        // Paso 2: Crear solicitud de crédito
        CreditApplicationDto applicationDto = CreditApplicationDto.builder()
                .afiliadoId(createdAffiliate.getId())
                .montoSolicitado(new BigDecimal("10000000"))
                .plazoMeses(36)
                .tasaPropuesta(new BigDecimal("12.5"))
                .build();

        mockMvc.perform(post("/api/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.afiliadoId").value(createdAffiliate.getId()))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.montoSolicitado").value(10000000));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ANALISTA")
    @DisplayName("E2E: Debe listar solicitudes pendientes")
    void shouldListPendingApplications() throws Exception {
        mockMvc.perform(get("/api/applications/pending")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("E2E: Debe crear y consultar afiliado")
    void shouldCreateAndRetrieveAffiliate() throws Exception {
        // Crear
        MvcResult createResult = mockMvc.perform(post("/api/affiliates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAffiliate)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = createResult.getResponse().getContentAsString();
        AffiliateDto created = objectMapper.readValue(json, AffiliateDto.class);

        // Consultar por ID
        mockMvc.perform(get("/api/affiliates/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.documento").value(uniqueDocument));

        // Consultar por documento
        mockMvc.perform(get("/api/affiliates/documento/" + uniqueDocument)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documento").value(uniqueDocument));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("E2E: Debe validar datos inválidos")
    void shouldValidateInvalidData() throws Exception {
        // Afiliado con salario negativo
        AffiliateDto invalidAffiliate = AffiliateDto.builder()
                .documento("9999999999")
                .nombre("Invalid User")
                .salario(new BigDecimal("-1000"))
                .fechaAfiliacion(LocalDate.now())
                .estado(AffiliateStatus.ACTIVO)
                .build();

        mockMvc.perform(post("/api/affiliates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAffiliate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("E2E: Debe actualizar estado de afiliado")
    void shouldUpdateAffiliateStatus() throws Exception {
        // Crear afiliado
        MvcResult createResult = mockMvc.perform(post("/api/affiliates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAffiliate)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = createResult.getResponse().getContentAsString();
        AffiliateDto created = objectMapper.readValue(json, AffiliateDto.class);

        // Cambiar estado
        mockMvc.perform(patch("/api/affiliates/" + created.getId() + "/status")
                        .with(csrf())
                        .param("newStatus", "INACTIVO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_AFILIADO")
    @DisplayName("E2E: Debe denegar acceso a endpoints restringidos")
    void shouldDenyAccessToRestrictedEndpoints() throws Exception {
        // AFILIADO no puede crear afiliados
        mockMvc.perform(post("/api/affiliates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAffiliate)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("E2E: Debe manejar documento duplicado")
    void shouldHandleDuplicateDocument() throws Exception {
        // Crear primer afiliado con documento específico
        String duplicateDoc = "DUPLICATE123";
        AffiliateDto affiliate1 = AffiliateDto.builder()
                .documento(duplicateDoc)
                .nombre("First Affiliate")
                .salario(new BigDecimal("5000000"))
                .fechaAfiliacion(LocalDate.now().minusYears(1))
                .estado(AffiliateStatus.ACTIVO)
                .build();
        
        mockMvc.perform(post("/api/affiliates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(affiliate1)))
                .andExpect(status().isCreated());

        // Intentar crear con mismo documento (debe fallar)
        AffiliateDto affiliate2 = AffiliateDto.builder()
                .documento(duplicateDoc)  // Mismo documento
                .nombre("Second Affiliate")
                .salario(new BigDecimal("6000000"))
                .fechaAfiliacion(LocalDate.now().minusMonths(6))
                .estado(AffiliateStatus.ACTIVO)
                .build();
        
        mockMvc.perform(post("/api/affiliates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(affiliate2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("E2E: Debe listar todos los afiliados")
    void shouldListAllAffiliates() throws Exception {
        // Crear varios afiliados
        for (int i = 0; i < 3; i++) {
            AffiliateDto affiliate = AffiliateDto.builder()
                    .documento("100000000" + i)
                    .nombre("User " + i)
                    .salario(new BigDecimal("5000000"))
                    .fechaAfiliacion(LocalDate.now().minusYears(1))
                    .estado(AffiliateStatus.ACTIVO)
                    .build();

            mockMvc.perform(post("/api/affiliates")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(affiliate)))
                    .andExpect(status().isCreated());
        }

        // Listar todos
        mockMvc.perform(get("/api/affiliates")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));
    }
}
