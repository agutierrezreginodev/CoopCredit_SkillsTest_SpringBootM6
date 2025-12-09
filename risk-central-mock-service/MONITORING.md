# Monitoreo con Prometheus y Grafana

Este documento explica cómo configurar y usar Prometheus y Grafana para monitorear las métricas de la aplicación Risk Central Mock Service.

## 📋 Requisitos Previos

- Docker y Docker Compose instalados
- La aplicación Spring Boot ejecutándose en el puerto 8081

## 🚀 Inicio Rápido

### 1. Iniciar la aplicación Spring Boot

```bash
./mvnw spring-boot:run
```

La aplicación expondrá métricas en: `http://localhost:8081/actuator/prometheus`

### 2. Iniciar Prometheus y Grafana

```bash
docker-compose -f docker-compose-monitoring.yml up -d
```

Esto iniciará:
- **Prometheus** en `http://localhost:9090`
- **Grafana** en `http://localhost:3000`

### 3. Acceder a Grafana

1. Abrir navegador en: `http://localhost:3000`
2. Credenciales por defecto:
   - **Usuario**: `admin`
   - **Contraseña**: `admin`
3. El dashboard "Risk Central Mock Service - Spring Boot Metrics" estará disponible automáticamente

## 📊 Dashboard de Métricas

El dashboard incluye los siguientes paneles:

### HTTP Metrics
- **HTTP Request Rate**: Tasa de solicitudes HTTP por minuto
- **HTTP Request Duration**: Duración promedio de las solicitudes HTTP

### JVM Metrics
- **JVM Heap Memory Available**: Porcentaje de memoria heap disponible
- **JVM Memory Usage**: Uso de memoria por área (heap, non-heap)
- **JVM Threads**: Número de threads activos y daemon

### System Metrics
- **CPU Usage**: Uso de CPU del proceso
- **GC Rate**: Tasa de recolección de basura

## 🔍 Métricas Disponibles

La aplicación expone las siguientes métricas principales:

### HTTP Server
```
http_server_requests_seconds_count
http_server_requests_seconds_sum
http_server_requests_seconds_max
```

### JVM Memory
```
jvm_memory_used_bytes
jvm_memory_max_bytes
jvm_memory_committed_bytes
```

### JVM Threads
```
jvm_threads_live_threads
jvm_threads_daemon_threads
jvm_threads_peak_threads
```

### JVM Garbage Collection
```
jvm_gc_pause_seconds_count
jvm_gc_pause_seconds_sum
jvm_gc_memory_allocated_bytes_total
```

### System
```
process_cpu_usage
system_cpu_usage
system_load_average_1m
```

## 🛠️ Configuración

### Prometheus

La configuración de Prometheus está en `monitoring/prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'risk-central-mock-service'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
    static_configs:
      - targets: ['host.docker.internal:8081']
```

### Spring Boot Actuator

La configuración de Actuator está en `src/main/resources/application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

## 📈 Consultas Prometheus Útiles

### Tasa de solicitudes HTTP
```promql
rate(http_server_requests_seconds_count{application="risk-central-mock-service"}[1m])
```

### Duración promedio de solicitudes
```promql
rate(http_server_requests_seconds_sum[1m]) / rate(http_server_requests_seconds_count[1m])
```

### Uso de memoria heap
```promql
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100
```

### Tasa de GC
```promql
rate(jvm_gc_pause_seconds_count[1m])
```

## 🔧 Personalización del Dashboard

### Crear un nuevo panel en Grafana:

1. Ir al dashboard existente
2. Click en "Add Panel"
3. Seleccionar "Add a new panel"
4. Ingresar una consulta Prometheus
5. Configurar visualización y opciones
6. Guardar el panel

### Exportar dashboard:

```bash
# Desde Grafana UI: Dashboard Settings > JSON Model > Copy to clipboard
```

## 🐳 Comandos Docker Útiles

### Ver logs de Prometheus
```bash
docker logs prometheus
```

### Ver logs de Grafana
```bash
docker logs grafana
```

### Reiniciar servicios
```bash
docker-compose -f docker-compose-monitoring.yml restart
```

### Detener servicios
```bash
docker-compose -f docker-compose-monitoring.yml down
```

### Detener y eliminar volúmenes
```bash
docker-compose -f docker-compose-monitoring.yml down -v
```

## 🔐 Seguridad

### Cambiar contraseña de Grafana:

Editar `docker-compose-monitoring.yml`:

```yaml
grafana:
  environment:
    - GF_SECURITY_ADMIN_PASSWORD=tu_nueva_contraseña
```

### Habilitar autenticación en Prometheus:

Para producción, se recomienda agregar autenticación básica o configurar un reverse proxy.

## 🚨 Alertas (Opcional)

Para configurar alertas en Prometheus, crear `monitoring/prometheus-rules.yml`:

```yaml
groups:
  - name: risk_central_alerts
    rules:
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage detected"
          description: "Heap memory usage is above 90%"
      
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[1m]) > 0.1
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is above 10%"
```

Y actualizar la configuración de Prometheus para incluir las reglas.

## 📚 Referencias

- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## 🤝 Soporte

Para problemas o preguntas sobre el monitoreo:
1. Verificar que la aplicación esté ejecutándose correctamente
2. Verificar que el endpoint `/actuator/prometheus` esté accesible
3. Revisar los logs de Prometheus y Grafana
4. Verificar la configuración de red de Docker

## 📝 Notas Adicionales

- Las métricas se recolectan cada 5 segundos por defecto
- Los datos se mantienen en volúmenes de Docker persistentes
- El dashboard se actualiza automáticamente cada 5 segundos
- Para entornos de producción, considerar usar Prometheus y Grafana en modo alta disponibilidad
