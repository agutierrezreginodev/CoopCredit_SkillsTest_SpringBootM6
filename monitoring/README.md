# Monitoring - Configuración de Observabilidad

Este directorio contiene toda la configuración necesaria para el stack de observabilidad de CoopCredit con **Prometheus** y **Grafana**.

## 📁 Estructura

```
monitoring/
├── README.md                           # Este archivo
├── QUICK_REFERENCE.md                  # Guía de referencia rápida
├── prometheus.yml                      # Configuración de Prometheus
├── example-custom-metrics.java         # Ejemplo de métricas personalizadas
├── grafana/
│   ├── provisioning/
│   │   ├── datasources/
│   │   │   └── prometheus.yml         # Datasource de Prometheus
│   │   └── dashboards/
│   │       └── dashboards.yml         # Provisioning de dashboards
│   └── dashboards/
│       └── spring-boot-overview.json  # Dashboard principal de Spring Boot
```

## 🚀 Inicio Rápido

```bash
# 1. Asegúrate de estar en el directorio raíz del proyecto
cd CoopCredit-System

# 2. Iniciar todo el stack (incluye Prometheus y Grafana)
docker compose up -d

# 3. Verificar que los servicios estén corriendo
docker compose ps

# 4. Acceder a Grafana
open http://localhost:3000
# Usuario: admin
# Contraseña: admin123
```

## 📊 Dashboards Incluidos

### 1. CoopCredit - Spring Boot Overview
Dashboard pre-configurado con:
- ✅ Estado de la aplicación (UP/DOWN)
- ✅ Uptime
- ✅ Uso de memoria heap (%)
- ✅ Uso de CPU (%)
- ✅ Request rate por endpoint
- ✅ Latencia HTTP promedio
- ✅ Uso de memoria JVM
- ✅ CPU del sistema y proceso
- ✅ Pool de conexiones de base de datos
- ✅ Códigos de estado HTTP (2xx, 4xx, 5xx)
- ✅ Threads JVM
- ✅ Tiempo de Garbage Collection

## 🔧 Configuración

### Prometheus (`prometheus.yml`)

Configuración de scraping para:
- **Credit Application Service** - Puerto 8080, path `/actuator/prometheus`
- **Risk Central Mock Service** - Puerto 8081, path `/actuator/prometheus`
- **Prometheus** - Auto-monitoreo en puerto 9090

**Configuración clave:**
- `scrape_interval: 15s` - Recolecta métricas cada 15 segundos
- `retention: 15d` - Retiene datos por 15 días
- Labels automáticos: `application`, `service`, `team`

### Grafana Datasource (`grafana/provisioning/datasources/prometheus.yml`)

Provisioning automático de Prometheus como datasource:
- URL: `http://prometheus:9090`
- Datasource por defecto
- Método HTTP: POST
- Intervalo de tiempo: 15s

### Dashboards (`grafana/provisioning/dashboards/dashboards.yml`)

Provisioning automático de dashboards desde `/var/lib/grafana/dashboards`:
- Carpeta: **CoopCredit**
- Actualización cada 30 segundos
- Editable desde la UI

## 📈 Métricas Personalizadas

### Implementar Métricas de Negocio

El archivo `example-custom-metrics.java` contiene ejemplos de:

1. **Contadores** (Counter): Para eventos que solo incrementan
   - Solicitudes de crédito creadas
   - Solicitudes aprobadas/rechazadas
   - Afiliados registrados
   - Errores en evaluación de riesgo

2. **Timers**: Para medir duración de operaciones
   - Tiempo de evaluación de crédito
   - Tiempo de respuesta de central de riesgo

**Para usar:**
```bash
# Copiar el ejemplo al proyecto
cp monitoring/example-custom-metrics.java \
   credit-application-service/src/main/java/com/coopcredit/creditapplicationservice/infrastructure/config/CustomMetricsConfig.java
```

Luego inyecta los beans en tus servicios:
```java
@Service
@RequiredArgsConstructor
public class CreditApplicationService {
    private final Counter creditApplicationsCounter;
    private final Timer creditEvaluationTimer;
    
    public void createApplication(...) {
        creditApplicationsCounter.increment();
        // ...
    }
}
```

## 📚 Documentación

### Guías Completas

1. **[GRAFANA_INTEGRATION_GUIDE.md](../GRAFANA_INTEGRATION_GUIDE.md)**
   - Guía completa paso a paso
   - Configuración detallada
   - Ejemplos de queries PromQL
   - Configuración de alertas
   - Troubleshooting extenso

2. **[QUICK_REFERENCE.md](./QUICK_REFERENCE.md)**
   - Referencia rápida
   - Comandos más usados
   - Queries PromQL comunes
   - Troubleshooting rápido

## 🎯 Queries PromQL Esenciales

### Performance
```promql
# Request rate
rate(http_server_requests_seconds_count[5m])

# Latencia P95
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))
```

### Recursos
```promql
# Memoria heap (%)
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# CPU sistema (%)
system_cpu_usage * 100
```

### Base de Datos
```promql
# Conexiones activas
hikaricp_connections_active

# Pool usage (%)
(hikaricp_connections_active / hikaricp_connections_max) * 100
```

Ver más queries en [QUICK_REFERENCE.md](./QUICK_REFERENCE.md)

## 🔍 Verificar Configuración

```bash
# 1. Verificar que Prometheus esté scrapeando
curl http://localhost:9090/api/v1/targets

# 2. Verificar métricas de la aplicación
curl http://localhost:8080/actuator/prometheus | head -20

# 3. Test query en Prometheus
curl 'http://localhost:9090/api/v1/query?query=up'

# 4. Verificar logs
docker compose logs prometheus grafana
```

## 🛠️ Personalización

### Añadir Nuevos Targets a Prometheus

Edita `prometheus.yml`:
```yaml
scrape_configs:
  - job_name: 'nuevo-servicio'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['nuevo-servicio:8082']
        labels:
          application: 'nuevo-servicio'
```

Recarga la configuración:
```bash
curl -X POST http://localhost:9090/-/reload
```

### Añadir Nuevos Dashboards

1. Crea tu dashboard en Grafana UI
2. Exporta como JSON: **Dashboard settings** → **JSON Model**
3. Guarda en `grafana/dashboards/mi-dashboard.json`
4. Reinicia Grafana o espera auto-reload (30s)

### Modificar Retention de Prometheus

En `docker-compose.yml`:
```yaml
prometheus:
  command:
    - '--storage.tsdb.retention.time=30d'  # Cambiar de 15d a 30d
```

## 🚨 Alertas

### Configurar Alertas en Grafana

1. Edita un panel
2. Tab **Alert** → **Create alert rule**
3. Configura condición (ej: latencia > 2s)
4. Añade notification channel
5. **Save**

### Ejemplos de Alertas Útiles

**Alta Latencia:**
```promql
avg(rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])) > 2
```

**Memoria Alta:**
```promql
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100 > 85
```

**Servicio Caído:**
```promql
up{application="credit-application-service"} == 0
```

**Tasa de Error Alta:**
```promql
(sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m]))) * 100 > 5
```

## 🔐 Seguridad

### Cambiar Credenciales de Grafana

En `docker-compose.yml`:
```yaml
grafana:
  environment:
    - GF_SECURITY_ADMIN_USER=admin
    - GF_SECURITY_ADMIN_PASSWORD=TU_NUEVA_CONTRASEÑA
```

### Autenticación en Producción

Para producción, considera:
- ✅ Cambiar contraseñas por defecto
- ✅ Usar HTTPS (reverse proxy con Nginx/Traefik)
- ✅ Configurar OAuth/LDAP
- ✅ Restringir acceso a Prometheus (no exponerlo públicamente)
- ✅ Usar autenticación básica en Prometheus

## 🗑️ Limpiar Datos

```bash
# Detener servicios
docker compose down

# Eliminar solo datos de monitoreo (mantiene MySQL)
docker volume rm coopcredit-system_prometheus_data
docker volume rm coopcredit-system_grafana_data

# Reiniciar
docker compose up -d
```

## 📖 Recursos Externos

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [PromQL Cheat Sheet](https://promlabs.com/promql-cheat-sheet/)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## ❓ FAQ

**P: ¿Por qué no veo métricas en Grafana?**
R: Verifica que Prometheus esté scrapeando correctamente con `curl http://localhost:9090/api/v1/targets`

**P: ¿Cómo agrego más métricas de negocio?**
R: Copia `example-custom-metrics.java` al proyecto y personalízalo. Ver [GRAFANA_INTEGRATION_GUIDE.md](../GRAFANA_INTEGRATION_GUIDE.md#-crear-métricas-personalizadas)

**P: ¿Cuánto espacio ocupan las métricas?**
R: Aproximadamente 1-2 GB por 15 días de retención con 2-3 servicios.

**P: ¿Puedo usar Grafana Cloud?**
R: Sí, cambia el datasource URL a tu instancia de Grafana Cloud y usa Remote Write en Prometheus.

**P: ¿Cómo exporto mis dashboards?**
R: Dashboard settings → JSON Model → Copy to clipboard

---

**🎉 ¡Stack de observabilidad listo!**

Para más ayuda, consulta:
- 📘 [Guía Completa de Grafana](../GRAFANA_INTEGRATION_GUIDE.md)
- 📋 [Quick Reference](./QUICK_REFERENCE.md)
- 📖 [README Principal](../README.md)
