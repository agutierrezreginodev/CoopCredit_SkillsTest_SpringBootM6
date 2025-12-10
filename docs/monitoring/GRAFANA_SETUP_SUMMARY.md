# ✅ Grafana Integration - Setup Summary

## 🎉 ¡Integración Completada!

La integración de Grafana con tu sistema CoopCredit está **lista para usar**. Todos los archivos de configuración han sido creados y el sistema está preparado para monitoreo en tiempo real.

---

## 📦 Archivos Creados

### 1. Documentación Principal
- ✅ **`GRAFANA_INTEGRATION_GUIDE.md`** - Guía completa paso a paso (28+ páginas)
  - Arquitectura de monitoreo
  - Configuración detallada
  - Dashboards recomendados
  - Métricas personalizadas
  - Alertas
  - Troubleshooting completo

### 2. Configuraciones
- ✅ **`monitoring/prometheus.yml`** - Configuración de Prometheus con scraping de servicios
- ✅ **`monitoring/grafana/provisioning/datasources/prometheus.yml`** - Datasource automático
- ✅ **`monitoring/grafana/provisioning/dashboards/dashboards.yml`** - Provisioning de dashboards
- ✅ **`monitoring/grafana/dashboards/spring-boot-overview.json`** - Dashboard principal pre-configurado

### 3. Docker Compose
- ✅ **`docker-compose.yml`** - Actualizado con Prometheus y Grafana
  - Prometheus en puerto 9090
  - Grafana en puerto 3000
  - Volúmenes persistentes
  - Configuración de red

### 4. Guías y Ejemplos
- ✅ **`monitoring/README.md`** - Documentación del directorio de monitoreo
- ✅ **`monitoring/QUICK_REFERENCE.md`** - Referencia rápida para uso diario
- ✅ **`monitoring/example-custom-metrics.java`** - Ejemplos de métricas de negocio

### 5. README Actualizado
- ✅ **`README.md`** - Actualizado con enlaces a Grafana y servicios de monitoreo

---

## 🚀 Iniciar el Sistema (3 pasos)

### Paso 1: Iniciar Docker Compose
```bash
cd CoopCredit-System
docker compose up -d
```

Esto iniciará:
- ✅ MySQL (puerto 3306)
- ✅ Risk Central Mock Service (puerto 8081)
- ✅ Credit Application Service (puerto 8080)
- ✅ **Prometheus** (puerto 9090)
- ✅ **Grafana** (puerto 3000)

### Paso 2: Verificar Servicios
```bash
# Ver estado de todos los contenedores
docker compose ps

# Deberías ver 5 contenedores en estado "Up":
# - coopcredit-db
# - risk-central-mock
# - credit-application-service
# - coopcredit-prometheus
# - coopcredit-grafana
```

### Paso 3: Acceder a Grafana
1. Abre tu navegador en: **http://localhost:3000**
2. Login con:
   - **Usuario:** `admin`
   - **Contraseña:** `admin123`
3. Navega a: **Dashboards** → **Browse** → **CoopCredit**
4. Abre el dashboard: **CoopCredit - Spring Boot Overview**

---

## 📊 Lo Que Verás en el Dashboard

El dashboard pre-configurado incluye:

### Estado General
- 🟢 **Application Status** - UP/DOWN
- ⏱️ **Uptime** - Tiempo activo del servicio
- 📊 **Heap Memory Usage %** - Uso de memoria
- 💻 **System CPU Usage %** - Uso de CPU
- 📈 **Request Rate** - Solicitudes por segundo
- ⚡ **Avg Response Time** - Tiempo de respuesta promedio

### Métricas HTTP
- 📊 **HTTP Request Rate by Endpoint** - Tráfico por endpoint
- ⏱️ **HTTP Request Latency (Avg)** - Latencias por endpoint
- 📉 **HTTP Status Codes** - Distribución de 2xx, 4xx, 5xx

### Recursos del Sistema
- 💾 **JVM Memory Usage** - Memoria heap y non-heap
- 💻 **CPU Usage** - CPU del sistema y proceso
- 🔗 **Database Connection Pool** - Conexiones activas/idle
- 🧵 **JVM Threads** - Threads activos, daemon, peak
- 🗑️ **Garbage Collection Time** - Tiempo de GC

---

## 🎯 URLs de Acceso Rápido

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| **Grafana UI** | http://localhost:3000 | admin / admin123 |
| **Prometheus UI** | http://localhost:9090 | - |
| **API Health** | http://localhost:8080/actuator/health | - |
| **API Metrics** | http://localhost:8080/actuator/prometheus | - |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | - |

---

## 📈 Próximos Pasos

### 1. Explorar el Dashboard
- Familiarízate con los paneles
- Observa las métricas en tiempo real
- Cambia el rango de tiempo (1h, 6h, 24h)

### 2. Generar Carga en la API
```bash
# Registrar un usuario
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123",
    "documento": "12345678",
    "role": "ROLE_ADMIN"
  }'

# Login
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "test123"}' | jq -r '.token')

# Hacer requests y observar métricas en Grafana
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/affiliates
```

### 3. Importar Dashboards Comunitarios
1. En Grafana: **Dashboards** → **Import**
2. Ingresa el ID: **10280** (Spring Boot Statistics)
3. Selecciona datasource: **Prometheus**
4. Click **Import**

Otros dashboards útiles:
- **4701** - JVM Micrometer
- **11159** - Spring Boot Observability

### 4. Crear Métricas Personalizadas (Opcional)
```bash
# Copiar el ejemplo al proyecto
cp monitoring/example-custom-metrics.java \
   credit-application-service/src/main/java/com/coopcredit/creditapplicationservice/infrastructure/config/CustomMetricsConfig.java

# Reconstruir el servicio
cd credit-application-service
./mvnw clean package
docker compose up -d --build credit-application-service
```

Esto agregará métricas de negocio como:
- `credit_applications_total` - Solicitudes creadas
- `credit_applications_approved` - Solicitudes aprobadas
- `credit_applications_rejected` - Solicitudes rechazadas
- `credit_evaluation_duration` - Tiempo de evaluación

### 5. Configurar Alertas
1. Edita un panel en Grafana
2. Tab **Alert** → **Create alert rule from this panel**
3. Configura condición (ej: latencia > 2s, memoria > 85%)
4. Añade notification channel
5. **Save**

---

## 📚 Documentación Completa

Para más detalles, consulta:

1. **[GRAFANA_INTEGRATION_GUIDE.md](./GRAFANA_INTEGRATION_GUIDE.md)** ⭐
   - Guía completa paso a paso
   - 28+ páginas de documentación
   - Arquitectura detallada
   - Ejemplos de código
   - Queries PromQL avanzadas
   - Troubleshooting extenso

2. **[monitoring/QUICK_REFERENCE.md](./monitoring/QUICK_REFERENCE.md)**
   - Referencia rápida para uso diario
   - Comandos más comunes
   - Queries PromQL esenciales
   - Troubleshooting rápido

3. **[monitoring/README.md](./monitoring/README.md)**
   - Documentación del directorio de monitoreo
   - Estructura de archivos
   - Personalización
   - FAQ

---

## 🎨 Estructura Visual del Stack

```
┌─────────────────────────────────────────────────────────────┐
│                    👤 USUARIO                                │
└──────────────────────┬──────────────────────────────────────┘
                       │
         ┌─────────────┴─────────────┐
         │                           │
         ▼                           ▼
┌──────────────────┐        ┌──────────────────┐
│   GRAFANA :3000  │        │  SWAGGER :8080   │
│   Dashboards     │        │  API Testing     │
└────────┬─────────┘        └──────────────────┘
         │
         │ (queries)
         ▼
┌──────────────────────────────────────────────┐
│           PROMETHEUS :9090                    │
│   Time Series Database                       │
└────────┬─────────────────────────────────────┘
         │
         │ (scrape /actuator/prometheus every 15s)
         ▼
┌──────────────────────────────────────────────────────────────┐
│              SPRING BOOT APPLICATIONS                         │
│                                                               │
│  ┌─────────────────────┐      ┌────────────────────┐        │
│  │ Credit Application  │      │ Risk Central Mock  │        │
│  │ Service :8080       │◄────►│ Service :8081      │        │
│  │ /actuator/*         │      │ /actuator/*        │        │
│  └──────────┬──────────┘      └────────────────────┘        │
│             │                                                 │
│             ▼                                                 │
│     ┌───────────────┐                                        │
│     │  MySQL :3306  │                                        │
│     │  Database     │                                        │
│     └───────────────┘                                        │
└──────────────────────────────────────────────────────────────┘
```

---

## 🔍 Verificación Rápida

Ejecuta estos comandos para verificar que todo funcione:

```bash
# 1. ✅ Ver servicios activos
docker compose ps

# 2. ✅ Health check de la API
curl http://localhost:8080/actuator/health

# 3. ✅ Verificar que Prometheus esté scrapeando
curl http://localhost:9090/api/v1/targets | jq

# 4. ✅ Ver métricas disponibles
curl http://localhost:8080/actuator/prometheus | head -20

# 5. ✅ Test query en Prometheus
curl 'http://localhost:9090/api/v1/query?query=up' | jq
```

Si todos los comandos responden correctamente, ¡**el sistema está funcionando perfectamente**! 🎉

---

## 🛠️ Troubleshooting Rápido

### Problema: Grafana no muestra datos

```bash
# 1. Verificar logs
docker compose logs grafana prometheus

# 2. Verificar que Prometheus esté scrapeando
curl http://localhost:9090/api/v1/targets

# 3. Reiniciar servicios de monitoreo
docker compose restart prometheus grafana
```

### Problema: Puerto en uso

```bash
# Ver qué está usando el puerto 3000 (Grafana)
sudo lsof -i :3000

# O puerto 9090 (Prometheus)
sudo lsof -i :9090

# Detener proceso
kill <PID>
```

### Problema: Dashboard no carga

1. Verifica que el archivo exista:
   ```bash
   ls -la monitoring/grafana/dashboards/
   ```

2. Verifica el provisioning:
   ```bash
   docker exec -it coopcredit-grafana ls -la /etc/grafana/provisioning/dashboards/
   docker exec -it coopcredit-grafana ls -la /var/lib/grafana/dashboards/
   ```

3. Reinicia Grafana:
   ```bash
   docker compose restart grafana
   ```

Para más ayuda, consulta la sección **Troubleshooting** en [GRAFANA_INTEGRATION_GUIDE.md](./GRAFANA_INTEGRATION_GUIDE.md#-troubleshooting)

---

## 🎓 Aprender Más

### PromQL (Prometheus Query Language)
- [PromQL Cheat Sheet](https://promlabs.com/promql-cheat-sheet/)
- [Prometheus Query Basics](https://prometheus.io/docs/prometheus/latest/querying/basics/)

### Grafana
- [Grafana Tutorials](https://grafana.com/tutorials/)
- [Dashboard Best Practices](https://grafana.com/docs/grafana/latest/best-practices/)

### Spring Boot Actuator
- [Actuator Endpoints](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Metrics](https://micrometer.io/docs)

---

## 💡 Tips y Mejores Prácticas

### Dashboard
- ✅ Usa refresh de 10-30 segundos para dashboards en tiempo real
- ✅ Organiza paneles por función (HTTP, JVM, Database)
- ✅ Añade descripciones a los paneles
- ✅ Exporta dashboards importantes como backup

### Queries
- ✅ Usa `rate()` para contadores, no valores directos
- ✅ Usa ventanas de 5m en rate: `rate(metric[5m])`
- ✅ Para percentiles: `histogram_quantile(0.95, ...)`
- ✅ Agrega `by (label)` para agrupar métricas

### Alertas
- ✅ Alerta sobre síntomas (latencia alta) no causas (CPU alta)
- ✅ Evita alertas ruidosas
- ✅ Configura períodos de evaluación apropiados (5m)
- ✅ Documenta cada alerta (qué hacer cuando se activa)

### Seguridad
- ⚠️ Cambia las contraseñas por defecto en producción
- ⚠️ No expongas Prometheus/Grafana sin autenticación
- ✅ Usa HTTPS en producción
- ✅ Configura roles y permisos en Grafana

---

## 📞 Soporte

Si tienes problemas:

1. Consulta el **[Troubleshooting](./GRAFANA_INTEGRATION_GUIDE.md#-troubleshooting)** en la guía completa
2. Revisa el **[Quick Reference](./monitoring/QUICK_REFERENCE.md)** para comandos comunes
3. Verifica los logs: `docker compose logs grafana prometheus`
4. Revisa la documentación oficial:
   - [Prometheus Docs](https://prometheus.io/docs/)
   - [Grafana Docs](https://grafana.com/docs/)

---

## ✨ ¡Disfruta del Monitoreo!

Ahora tienes:
- ✅ Stack completo de observabilidad
- ✅ Dashboard pre-configurado
- ✅ Métricas en tiempo real
- ✅ Visualizaciones profesionales
- ✅ Base para alertas y monitoreo proactivo

**Próximo paso:** Abre http://localhost:3000 y explora tu nuevo dashboard de Grafana 🎉

---

**Desarrollado con ❤️ para CoopCredit System**
