# Grafana & Prometheus - Quick Reference Card

## 🚀 Quick Start

```bash
# Iniciar todo (incluye Grafana y Prometheus)
docker compose up -d

# Verificar que todo esté corriendo
docker compose ps

# Ver logs
docker compose logs -f grafana prometheus
```

## 🌐 URLs

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| **Grafana** | http://localhost:3000 | admin / admin123 |
| **Prometheus** | http://localhost:9090 | - |
| **API Metrics** | http://localhost:8080/actuator/prometheus | - |
| **Health Check** | http://localhost:8080/actuator/health | - |

## 📊 Dashboards

### Dashboard Pre-configurado
- **Nombre:** CoopCredit - Spring Boot Overview
- **Ubicación:** Dashboards → Browse → CoopCredit
- **Paneles:** Status, Request Rate, Latency, Memory, CPU, DB Pool, HTTP Status

### Importar Dashboards Comunitarios
1. Ir a **Dashboards** → **Import**
2. Ingresar ID del dashboard:
   - **10280** - Spring Boot Statistics
   - **4701** - JVM Micrometer
   - **11159** - Spring Boot Observability
3. Seleccionar datasource: **Prometheus**
4. Click **Import**

## 🔍 Queries PromQL Más Útiles

### Performance
```promql
# Request rate (req/s)
rate(http_server_requests_seconds_count[5m])

# Latencia promedio
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# Latencia P95
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Latencia P99
histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m]))
```

### Errores
```promql
# Tasa de errores 5xx
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# % de errores
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m])) * 100

# Errores en última hora por endpoint
sum by (uri) (increase(http_server_requests_seconds_count{status=~"5.."}[1h]))
```

### Recursos JVM
```promql
# Memoria heap usada (%)
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# CPU del sistema (%)
system_cpu_usage * 100

# CPU del proceso (%)
process_cpu_usage * 100

# Threads activos
jvm_threads_live_threads

# GC time
rate(jvm_gc_pause_seconds_sum[5m])
```

### Base de Datos
```promql
# Conexiones activas
hikaricp_connections_active

# Conexiones idle
hikaricp_connections_idle

# Pool usage (%)
(hikaricp_connections_active / hikaricp_connections_max) * 100

# Tiempo de espera por conexión
hikaricp_connections_acquire_seconds
```

### Business Metrics (si implementaste custom metrics)
```promql
# Solicitudes de crédito en última hora
increase(credit_applications_total[1h])

# Tasa de aprobación
rate(credit_applications_approved[5m]) / rate(credit_applications_total[5m]) * 100

# Tiempo promedio de evaluación
rate(credit_evaluation_duration_sum[5m]) / rate(credit_evaluation_duration_count[5m])
```

## 🚨 Crear una Alerta Simple

1. Abrir un panel en Grafana
2. Click en título del panel → **Edit**
3. Tab **Alert** → **Create alert rule from this panel**
4. Configurar:
   ```
   Name: Alta latencia en API
   Condition: WHEN avg() OF query(A, 5m, now) IS ABOVE 2
   Evaluate: Every 1m for 5m
   ```
5. **Save** y aplicar

## 🛠️ Troubleshooting Rápido

### Grafana no muestra datos
```bash
# 1. Verificar que Prometheus esté scrapeando
curl http://localhost:9090/api/v1/targets | jq

# 2. Verificar métricas en la app
curl http://localhost:8080/actuator/prometheus | head -20

# 3. Test query directo en Prometheus
curl 'http://localhost:9090/api/v1/query?query=up'

# 4. Ver logs
docker compose logs grafana prometheus
```

### Reiniciar servicios de monitoreo
```bash
# Solo Prometheus
docker compose restart prometheus

# Solo Grafana
docker compose restart grafana

# Ambos
docker compose restart prometheus grafana
```

### Recargar configuración de Prometheus (sin reiniciar)
```bash
curl -X POST http://localhost:9090/-/reload
```

### Limpiar datos y empezar de cero
```bash
# Detener servicios
docker compose down

# Eliminar volúmenes (CUIDADO: borra todos los datos)
docker volume rm coopcredit-system_prometheus_data
docker volume rm coopcredit-system_grafana_data

# Reiniciar
docker compose up -d
```

## 📈 Paneles Útiles para Crear

### Panel de Request Rate
- **Tipo:** Time series
- **Query:** `rate(http_server_requests_seconds_count{application="credit-application-service"}[5m])`
- **Legend:** `{{method}} {{uri}}`

### Panel de Latencia P95
- **Tipo:** Time series
- **Query:** `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))`
- **Unit:** seconds (s)

### Panel de Memory Usage
- **Tipo:** Time series
- **Query:** `jvm_memory_used_bytes{area="heap"}`
- **Unit:** bytes

### Panel de Error Rate
- **Tipo:** Stat
- **Query:** `sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m])) * 100`
- **Unit:** percent (0-100)
- **Thresholds:** Green < 1, Yellow < 5, Red >= 5

### Panel de Uptime
- **Tipo:** Stat
- **Query:** `process_uptime_seconds{application="credit-application-service"}`
- **Unit:** seconds (s)

## 🎨 Variables de Dashboard

Para hacer dashboards dinámicos:

1. **Dashboard settings** → **Variables** → **Add variable**
2. Configurar:
   ```
   Name: application
   Type: Query
   Data source: Prometheus
   Query: label_values(up, application)
   ```
3. Usar en queries: `{application="$application"}`

## 📦 Exportar/Importar Dashboard

### Exportar
1. Abrir dashboard
2. **Dashboard settings** (⚙️) → **JSON Model**
3. Click **Copy to clipboard**
4. Guardar en archivo `.json`

### Importar
1. **Dashboards** → **Import**
2. Pegar JSON o subir archivo
3. Click **Load** → **Import**

## 🔐 Cambiar Contraseña de Grafana

```bash
# Opción 1: Desde la UI
# Settings → Change password

# Opción 2: Desde CLI
docker exec -it coopcredit-grafana grafana-cli admin reset-admin-password newpassword

# Opción 3: Cambiar en docker-compose.yml
# GF_SECURITY_ADMIN_PASSWORD=nueva_contraseña
```

## 📚 Recursos Adicionales

- [Guía Completa de Grafana](./GRAFANA_INTEGRATION_GUIDE.md)
- [PromQL Cheat Sheet](https://promlabs.com/promql-cheat-sheet/)
- [Grafana Docs](https://grafana.com/docs/)
- [Prometheus Docs](https://prometheus.io/docs/)

## 💡 Tips

- ✅ Usa **rate()** para contadores, no valores directos
- ✅ Usa **5m** como ventana de tiempo estándar en rate()
- ✅ Para percentiles, usa **histogram_quantile()**
- ✅ Agrega **by (label)** para agrupar métricas
- ✅ Usa **sum()**, **avg()**, **max()**, **min()** para agregar
- ✅ Refresh dashboard cada 10-30 segundos
- ✅ Guarda tus dashboards importantes

---

**¿Necesitas ayuda?** Consulta la [Guía Completa](./GRAFANA_INTEGRATION_GUIDE.md) o la sección de [Troubleshooting](./GRAFANA_INTEGRATION_GUIDE.md#-troubleshooting)
