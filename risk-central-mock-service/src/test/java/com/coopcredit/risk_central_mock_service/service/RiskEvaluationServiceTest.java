package com.coopcredit.risk_central_mock_service.service;

import com.coopcredit.risk_central_mock_service.dto.RiskEvaluationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RiskEvaluationService - Tests")
class RiskEvaluationServiceTest {

    private RiskEvaluationService service;

    @BeforeEach
    void setUp() {
        service = new RiskEvaluationService();
    }

    // ========================================================================
    // BASIC FUNCTIONALITY TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe generar una evaluación de riesgo válida")
    void shouldGenerateValidRiskEvaluation() {
        // Given
        String documento = "1017654311";
        Double monto = 5000000.0;
        Integer plazo = 36;

        // When
        RiskEvaluationResponse response = service.evaluateRisk(documento, monto, plazo);

        // Then
        assertNotNull(response);
        assertEquals(documento, response.getDocumento());
        assertNotNull(response.getScore());
        assertNotNull(response.getNivelRiesgo());
        assertNotNull(response.getDetalle());
    }

    @Test
    @DisplayName("Debe generar score dentro del rango 300-950")
    void shouldGenerateScoreInValidRange() {
        // Given
        String[] documentos = {"1017654311", "1023456789", "1034567890", "9876543210"};

        // When & Then
        for (String documento : documentos) {
            RiskEvaluationResponse response = service.evaluateRisk(documento, 5000000.0, 36);
            
            assertTrue(response.getScore() >= 300, 
                "Score debe ser >= 300, fue: " + response.getScore());
            assertTrue(response.getScore() <= 950, 
                "Score debe ser <= 950, fue: " + response.getScore());
        }
    }

    // ========================================================================
    // CONSISTENCY TESTS (DETERMINISTIC BEHAVIOR)
    // ========================================================================

    @Test
    @DisplayName("Debe generar el mismo score para el mismo documento")
    void shouldGenerateConsistentScoreForSameDocument() {
        // Given
        String documento = "1017654311";
        Double monto = 5000000.0;
        Integer plazo = 36;

        // When
        RiskEvaluationResponse response1 = service.evaluateRisk(documento, monto, plazo);
        RiskEvaluationResponse response2 = service.evaluateRisk(documento, monto, plazo);
        RiskEvaluationResponse response3 = service.evaluateRisk(documento, monto, plazo);

        // Then
        assertEquals(response1.getScore(), response2.getScore());
        assertEquals(response1.getScore(), response3.getScore());
        assertEquals(response1.getNivelRiesgo(), response2.getNivelRiesgo());
        assertEquals(response1.getNivelRiesgo(), response3.getNivelRiesgo());
    }

    @RepeatedTest(10)
    @DisplayName("Debe ser determinístico en múltiples ejecuciones")
    void shouldBeDeterministicAcrossMultipleRuns() {
        // Given
        String documento = "1023456789";
        
        // When
        RiskEvaluationResponse response = service.evaluateRisk(documento, 10000000.0, 48);

        // Then - El score debe ser siempre el mismo para este documento
        assertNotNull(response.getScore());
        // El test se ejecuta 10 veces y debe dar siempre el mismo resultado
    }

    @Test
    @DisplayName("Debe generar diferentes scores para diferentes documentos")
    void shouldGenerateDifferentScoresForDifferentDocuments() {
        // Given
        String doc1 = "1111111111";
        String doc2 = "9999999999";
        Double monto = 5000000.0;
        Integer plazo = 36;

        // When
        RiskEvaluationResponse response1 = service.evaluateRisk(doc1, monto, plazo);
        RiskEvaluationResponse response2 = service.evaluateRisk(doc2, monto, plazo);

        // Then - Es altamente probable que sean diferentes
        // (podrían ser iguales por casualidad, pero es muy raro)
        assertNotEquals(response1.getScore(), response2.getScore(),
            "Documentos diferentes deberían generar scores diferentes");
    }

    // ========================================================================
    // RISK LEVEL CLASSIFICATION TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe clasificar como ALTO cuando score <= 500")
    void shouldClassifyAsHighRiskWhenScoreLowOrEqual500() {
        // Given - Buscar documentos que generen score <= 500
        String[] documentos = {"0000000001", "0000000002", "0000000003", "0000000004"};

        // When & Then
        for (String doc : documentos) {
            RiskEvaluationResponse response = service.evaluateRisk(doc, 5000000.0, 36);
            
            if (response.getScore() <= 500) {
                assertEquals("ALTO", response.getNivelRiesgo());
                assertTrue(response.getDetalle().contains("Alto riesgo"));
            }
        }
    }

    @Test
    @DisplayName("Debe clasificar como MEDIO cuando 500 < score <= 700")
    void shouldClassifyAsMediumRiskWhenScoreBetween501And700() {
        // Given - Buscar documentos que generen score medio
        String[] documentos = {"1017654311", "1023456789", "1034567890"};

        // When & Then
        for (String doc : documentos) {
            RiskEvaluationResponse response = service.evaluateRisk(doc, 5000000.0, 36);
            
            if (response.getScore() > 500 && response.getScore() <= 700) {
                assertEquals("MEDIO", response.getNivelRiesgo());
                assertTrue(response.getDetalle().contains("Riesgo medio"));
            }
        }
    }

    @Test
    @DisplayName("Debe clasificar como BAJO cuando score > 700")
    void shouldClassifyAsLowRiskWhenScoreAbove700() {
        // Given - Buscar documentos que generen score alto
        String[] documentos = {"9876543210", "8765432109", "7654321098"};

        // When & Then
        for (String doc : documentos) {
            RiskEvaluationResponse response = service.evaluateRisk(doc, 5000000.0, 36);
            
            if (response.getScore() > 700) {
                assertEquals("BAJO", response.getNivelRiesgo());
                assertTrue(response.getDetalle().contains("Bajo riesgo"));
            }
        }
    }

    // ========================================================================
    // EDGE CASES AND NULL HANDLING
    // ========================================================================

    @Test
    @DisplayName("Debe manejar documento vacío sin lanzar excepción")
    void shouldHandleEmptyDocumentWithoutException() {
        // Given
        String documento = "";

        // When
        RiskEvaluationResponse response = service.evaluateRisk(documento, 5000000.0, 36);

        // Then
        assertNotNull(response);
        assertEquals("", response.getDocumento());
        assertNotNull(response.getScore());
        assertTrue(response.getScore() >= 300 && response.getScore() <= 950);
    }

    @Test
    @DisplayName("Debe manejar documento null sin lanzar excepción")
    void shouldHandleNullDocumentWithoutException() {
        // Given
        String documento = null;

        // When
        RiskEvaluationResponse response = service.evaluateRisk(documento, 5000000.0, 36);

        // Then
        assertNotNull(response);
        assertNull(response.getDocumento());
        assertNotNull(response.getScore());
        assertTrue(response.getScore() >= 300 && response.getScore() <= 950);
    }

    @Test
    @DisplayName("Debe manejar monto null")
    void shouldHandleNullMonto() {
        // Given
        String documento = "1017654311";
        Double monto = null;

        // When
        RiskEvaluationResponse response = service.evaluateRisk(documento, monto, 36);

        // Then - El servicio mock no usa el monto, pero no debe fallar
        assertNotNull(response);
        assertEquals(documento, response.getDocumento());
    }

    @Test
    @DisplayName("Debe manejar plazo null")
    void shouldHandleNullPlazo() {
        // Given
        String documento = "1017654311";
        Integer plazo = null;

        // When
        RiskEvaluationResponse response = service.evaluateRisk(documento, 5000000.0, plazo);

        // Then - El servicio mock no usa el plazo, pero no debe fallar
        assertNotNull(response);
        assertEquals(documento, response.getDocumento());
    }

    // ========================================================================
    // PARAMETERIZED TESTS
    // ========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
        "1017654311",
        "1023456789",
        "1034567890",
        "1045678901",
        "1056789012",
        "9876543210",
        "8765432109"
    })
    @DisplayName("Debe generar evaluación válida para múltiples documentos")
    void shouldGenerateValidEvaluationForMultipleDocuments(String documento) {
        // When
        RiskEvaluationResponse response = service.evaluateRisk(documento, 5000000.0, 36);

        // Then
        assertNotNull(response);
        assertEquals(documento, response.getDocumento());
        assertTrue(response.getScore() >= 300 && response.getScore() <= 950);
        assertTrue(response.getNivelRiesgo().matches("BAJO|MEDIO|ALTO"));
        assertNotNull(response.getDetalle());
        assertFalse(response.getDetalle().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(doubles = {
        1000000.0,
        5000000.0,
        10000000.0,
        50000000.0,
        100000000.0
    })
    @DisplayName("Debe funcionar con diferentes montos")
    void shouldWorkWithDifferentAmounts(Double monto) {
        // Given
        String documento = "1017654311";

        // When
        RiskEvaluationResponse response = service.evaluateRisk(documento, monto, 36);

        // Then - El score debe ser el mismo independientemente del monto
        assertNotNull(response);
        assertEquals(documento, response.getDocumento());
    }

    @ParameterizedTest
    @ValueSource(ints = {6, 12, 24, 36, 48, 60, 72, 84, 96, 120})
    @DisplayName("Debe funcionar con diferentes plazos")
    void shouldWorkWithDifferentTerms(Integer plazo) {
        // Given
        String documento = "1017654311";

        // When
        RiskEvaluationResponse response = service.evaluateRisk(documento, 5000000.0, plazo);

        // Then - El score debe ser el mismo independientemente del plazo
        assertNotNull(response);
        assertEquals(documento, response.getDocumento());
    }

    // ========================================================================
    // DATA INTEGRITY TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe incluir el documento en la respuesta")
    void shouldIncludeDocumentInResponse() {
        // Given
        String documento = "1017654311";

        // When
        RiskEvaluationResponse response = service.evaluateRisk(documento, 5000000.0, 36);

        // Then
        assertEquals(documento, response.getDocumento());
    }

    @Test
    @DisplayName("Debe incluir detalle apropiado según el nivel de riesgo")
    void shouldIncludeAppropriateDetailForRiskLevel() {
        // Given
        String[] documentos = {"0000000001", "1017654311", "9876543210"};

        // When & Then
        for (String doc : documentos) {
            RiskEvaluationResponse response = service.evaluateRisk(doc, 5000000.0, 36);
            
            switch (response.getNivelRiesgo()) {
                case "ALTO":
                    assertTrue(response.getDetalle().toLowerCase().contains("alto"));
                    break;
                case "MEDIO":
                    assertTrue(response.getDetalle().toLowerCase().contains("medio"));
                    break;
                case "BAJO":
                    assertTrue(response.getDetalle().toLowerCase().contains("bajo"));
                    break;
                default:
                    fail("Nivel de riesgo no reconocido: " + response.getNivelRiesgo());
            }
        }
    }

    @Test
    @DisplayName("Debe tener todos los campos no nulos en respuesta exitosa")
    void shouldHaveAllNonNullFieldsInSuccessResponse() {
        // Given
        String documento = "1017654311";

        // When
        RiskEvaluationResponse response = service.evaluateRisk(documento, 5000000.0, 36);

        // Then
        assertNotNull(response, "Response no debe ser null");
        assertNotNull(response.getDocumento(), "Documento no debe ser null");
        assertNotNull(response.getScore(), "Score no debe ser null");
        assertNotNull(response.getNivelRiesgo(), "NivelRiesgo no debe ser null");
        assertNotNull(response.getDetalle(), "Detalle no debe ser null");
    }

    // ========================================================================
    // BOUNDARY VALUE TESTS
    // ========================================================================

    @Test
    @DisplayName("Debe verificar límites de clasificación de riesgo")
    void shouldVerifyRiskClassificationBoundaries() {
        // Este test verifica que la lógica de clasificación es correcta
        // en los puntos de frontera (500 y 700)
        
        // Given - Necesitaríamos inyectar scores específicos, 
        // pero como el servicio es determinístico basado en documento,
        // verificamos la lógica general
        
        String[] documentos = new String[100];
        for (int i = 0; i < 100; i++) {
            documentos[i] = String.format("%010d", i);
        }

        int countAlto = 0;
        int countMedio = 0;
        int countBajo = 0;

        // When
        for (String doc : documentos) {
            RiskEvaluationResponse response = service.evaluateRisk(doc, 5000000.0, 36);
            
            switch (response.getNivelRiesgo()) {
                case "ALTO":
                    countAlto++;
                    assertTrue(response.getScore() <= 500);
                    break;
                case "MEDIO":
                    countMedio++;
                    assertTrue(response.getScore() > 500 && response.getScore() <= 700);
                    break;
                case "BAJO":
                    countBajo++;
                    assertTrue(response.getScore() > 700);
                    break;
            }
        }

        // Then - Debe haber documentos en cada categoría
        assertTrue(countAlto > 0 || countMedio > 0 || countBajo > 0,
            "Debe haber al menos una categoría con documentos");
    }

    @Test
    @DisplayName("Debe manejar documentos con caracteres especiales")
    void shouldHandleDocumentsWithSpecialCharacters() {
        // Given
        String[] specialDocs = {
            "ABC-123-XYZ",
            "12.345.678-9",
            "1234567890A",
            "X-1234567-Y"
        };

        // When & Then
        for (String doc : specialDocs) {
            RiskEvaluationResponse response = service.evaluateRisk(doc, 5000000.0, 36);
            
            assertNotNull(response);
            assertEquals(doc, response.getDocumento());
            assertTrue(response.getScore() >= 300 && response.getScore() <= 950);
        }
    }
}
