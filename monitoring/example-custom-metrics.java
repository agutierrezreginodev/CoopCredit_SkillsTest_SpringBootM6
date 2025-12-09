package com.coopcredit.creditapplicationservice.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ejemplo de configuración de métricas personalizadas para CoopCredit
 * 
 * Esta clase demuestra cómo crear métricas de negocio personalizadas
 * que serán automáticamente exportadas a Prometheus y visualizadas en Grafana.
 * 
 * Para usar estas métricas:
 * 1. Copia este archivo a: credit-application-service/src/main/java/com/coopcredit/creditapplicationservice/infrastructure/config/
 * 2. Inyecta los beans en tus servicios
 * 3. Llama a los métodos increment(), recordCallable(), etc.
 * 4. Las métricas aparecerán automáticamente en Prometheus
 * 
 * @author CoopCredit Development Team
 */
@Configuration
public class CustomMetricsConfig {

    // ==================== CONTADORES ====================
    // Los contadores solo incrementan, nunca decrementan
    // Útiles para contar eventos: requests, errores, transacciones, etc.

    /**
     * Contador total de solicitudes de crédito creadas
     * Query en Grafana: increase(credit_applications_total[5m])
     */
    @Bean
    public Counter creditApplicationsCounter(MeterRegistry registry) {
        return Counter.builder("credit.applications.total")
                .description("Total de solicitudes de crédito creadas")
                .tag("type", "creation")
                .register(registry);
    }

    /**
     * Contador de solicitudes de crédito aprobadas
     * Query en Grafana: rate(credit_applications_approved[5m])
     */
    @Bean
    public Counter creditApplicationsApprovedCounter(MeterRegistry registry) {
        return Counter.builder("credit.applications.approved")
                .description("Total de solicitudes de crédito aprobadas")
                .tag("status", "approved")
                .register(registry);
    }

    /**
     * Contador de solicitudes de crédito rechazadas
     * Query en Grafana: rate(credit_applications_rejected[5m])
     */
    @Bean
    public Counter creditApplicationsRejectedCounter(MeterRegistry registry) {
        return Counter.builder("credit.applications.rejected")
                .description("Total de solicitudes de crédito rechazadas")
                .tag("status", "rejected")
                .register(registry);
    }

    /**
     * Contador de afiliados registrados
     * Query en Grafana: increase(affiliates_registered_total[1h])
     */
    @Bean
    public Counter affiliatesRegisteredCounter(MeterRegistry registry) {
        return Counter.builder("affiliates.registered.total")
                .description("Total de afiliados registrados en el sistema")
                .tag("type", "registration")
                .register(registry);
    }

    /**
     * Contador de errores en evaluación de riesgo
     * Query en Grafana: increase(risk_evaluation_errors_total[5m])
     */
    @Bean
    public Counter riskEvaluationErrorsCounter(MeterRegistry registry) {
        return Counter.builder("risk.evaluation.errors.total")
                .description("Total de errores al consultar la central de riesgo")
                .tag("type", "error")
                .register(registry);
    }

    // ==================== TIMERS ====================
    // Los timers miden duración y frecuencia de eventos
    // Automáticamente crean métricas de count, sum, max, y histogramas

    /**
     * Timer para medir el tiempo de evaluación de crédito
     * Métricas generadas automáticamente:
     * - credit_evaluation_duration_count (número de evaluaciones)
     * - credit_evaluation_duration_sum (tiempo total)
     * - credit_evaluation_duration_max (tiempo máximo)
     * - credit_evaluation_duration_bucket (histograma para percentiles)
     * 
     * Query P95 en Grafana: 
     * histogram_quantile(0.95, rate(credit_evaluation_duration_bucket[5m]))
     */
    @Bean
    public Timer creditEvaluationTimer(MeterRegistry registry) {
        return Timer.builder("credit.evaluation.duration")
                .description("Tiempo de evaluación de solicitud de crédito")
                .tag("process", "evaluation")
                .register(registry);
    }

    /**
     * Timer para medir el tiempo de respuesta de la central de riesgo
     * Query tiempo promedio: 
     * rate(risk_central_request_duration_sum[5m]) / rate(risk_central_request_duration_count[5m])
     */
    @Bean
    public Timer riskCentralRequestTimer(MeterRegistry registry) {
        return Timer.builder("risk.central.request.duration")
                .description("Tiempo de respuesta de la central de riesgo")
                .tag("service", "risk-central")
                .register(registry);
    }

    // ==================== EJEMPLO DE USO EN SERVICIOS ====================
    
    /*
     * EJEMPLO 1: Usar contador en un servicio
     * 
     * @Service
     * @RequiredArgsConstructor
     * public class CreditApplicationService {
     *     
     *     private final Counter creditApplicationsCounter;
     *     private final Counter creditApplicationsApprovedCounter;
     *     
     *     public CreditApplication createApplication(CreateApplicationRequest request) {
     *         // Incrementar contador
     *         creditApplicationsCounter.increment();
     *         
     *         // ... lógica de creación
     *         
     *         return application;
     *     }
     *     
     *     public EvaluationResult evaluateApplication(Long id) {
     *         // ... lógica de evaluación
     *         
     *         if (result.isApproved()) {
     *             creditApplicationsApprovedCounter.increment();
     *         }
     *         
     *         return result;
     *     }
     * }
     */
    
    /*
     * EJEMPLO 2: Usar timer para medir duración
     * 
     * @Service
     * @RequiredArgsConstructor
     * public class CreditApplicationService {
     *     
     *     private final Timer creditEvaluationTimer;
     *     
     *     public EvaluationResult evaluateApplication(Long id) {
     *         // Opción 1: Usar recordCallable
     *         return creditEvaluationTimer.recordCallable(() -> {
     *             // ... lógica de evaluación
     *             return performEvaluation(id);
     *         });
     *         
     *         // Opción 2: Usar record con Supplier
     *         return creditEvaluationTimer.record(() -> performEvaluation(id));
     *         
     *         // Opción 3: Medir manualmente con Sample
     *         Timer.Sample sample = Timer.start(registry);
     *         try {
     *             EvaluationResult result = performEvaluation(id);
     *             sample.stop(creditEvaluationTimer);
     *             return result;
     *         } catch (Exception e) {
     *             sample.stop(creditEvaluationTimer);
     *             throw e;
     *         }
     *     }
     * }
     */
    
    /*
     * EJEMPLO 3: Contador con tags dinámicos
     * 
     * @Service
     * @RequiredArgsConstructor
     * public class CreditApplicationService {
     *     
     *     private final MeterRegistry registry;
     *     
     *     public void rejectApplication(Long id, String reason) {
     *         // Crear contador con tag dinámico
     *         Counter.builder("credit.applications.rejected")
     *                 .tag("reason", reason)  // Tag dinámico: reason=insufficient_income, etc.
     *                 .register(registry)
     *                 .increment();
     *         
     *         // ... lógica de rechazo
     *     }
     * }
     */

    // ==================== QUERIES ÚTILES PARA GRAFANA ====================
    
    /*
     * TASA DE APROBACIÓN DE CRÉDITOS (%)
     * rate(credit_applications_approved[5m]) / rate(credit_applications_total[5m]) * 100
     * 
     * SOLICITUDES DE CRÉDITO POR HORA
     * increase(credit_applications_total[1h])
     * 
     * TIEMPO PROMEDIO DE EVALUACIÓN
     * rate(credit_evaluation_duration_sum[5m]) / rate(credit_evaluation_duration_count[5m])
     * 
     * PERCENTIL 95 DE TIEMPO DE EVALUACIÓN
     * histogram_quantile(0.95, rate(credit_evaluation_duration_bucket[5m]))
     * 
     * PERCENTIL 99 DE TIEMPO DE EVALUACIÓN
     * histogram_quantile(0.99, rate(credit_evaluation_duration_bucket[5m]))
     * 
     * TASA DE ERROR EN CENTRAL DE RIESGO
     * rate(risk_evaluation_errors_total[5m]) / rate(risk_central_request_duration_count[5m]) * 100
     * 
     * TOP 5 RAZONES DE RECHAZO
     * topk(5, increase(credit_applications_rejected[1h]))
     */
}
