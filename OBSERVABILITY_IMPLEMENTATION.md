# 📊 Implementación de Observabilidad: Actuators, Prometheus y Grafana

## 📋 Tabla de Contenido

1. [Visión General](#-visión-general)
2. [Spring Boot Actuator](#-spring-boot-actuator)
3. [Prometheus](#-prometheus)
4. [Grafana](#-grafana)
5. [Arquitectura del Sistema](#-arquitectura-del-sistema)
6. [Flujo de Datos](#-flujo-de-datos)
7. [Métricas Disponibles](#-métricas-disponibles)
8. [Configuración Paso a Paso](#-configuración-paso-a-paso)
9. [Verificación y Testing](#-verificación-y-testing)

---

## 🎯 Visión General

La observabilidad en CoopCredit se implementó utilizando el **stack clásico de monitoreo para Spring Boot**:

```
Spring Boot Actuator → Prometheus → Grafana
       ↓                   ↓            ↓
  Expone métricas    Almacena datos  Visualiza
```

### ¿Por qué esta Stack?

- **Spring Boot Actuator**: Integración nativa con Spring Boot, sin overhead
- **Prometheus**: Almacenamiento time-series optimizado, lenguaje PromQL potente
- **Grafana**: Visualización flexible, alertas avanzadas, fácil de usar

---

## 🔍 Spring Boot Actuator

### 1. Dependencias Maven

**Archivo:** `credit-application-service/pom.xml`

```xml
<dependencies>
    <!-- Spring Boot Actuator - Expone métricas de la aplicación -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Micrometer Prometheus Registry - Formato de métricas para Prometheus -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

**¿Qué hace cada dependencia?**

- **spring-boot-starter-actuator**: 
  - Expone endpoints de salud, métricas, info, etc.
  - Proporciona `MeterRegistry` para métricas personalizadas
  - Recolecta automáticamente métricas de JVM, HTTP, DB, etc.

- **micrometer-registry-prometheus**:
  - Formatea las métricas en formato que Prometheus entiende
  - Expone endpoint `/actuator/prometheus` con métricas en texto plano
  - Convierte métricas Micrometer a formato Prometheus

### 2. Configuración en `application.yml`

**Archivo:** `credit-application-service/src/main/resources/application.yml`

```yaml
# Configuración de Actuator (Observabilidad)
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}

# Información de la aplicación
info:
  app:
    name: CoopCredit Application Service
    description: Sistema de Gestión de Solicitudes de Crédito
    version: 1.0.0
    encoding: UTF-8
    java:
      version: 17
```

**Explicación de la configuración:**

| Propiedad | Valor | ¿Qué hace? |
|-----------|-------|------------|
| `exposure.include` | `health,info,metrics,prometheus` | Define qué endpoints están disponibles públicamente |
| `base-path` | `/actuator` | Prefijo de la URL para todos los endpoints |
| `health.show-details` | `always` | Muestra detalles completos del estado de salud (DB, disco, etc.) |
| `metrics.enabled` | `true` | Habilita el endpoint `/actuator/metrics` |
| `prometheus.enabled` | `true` | Habilita el endpoint `/actuator/prometheus` |
| `metrics.tags.application` | `credit-application-service` | Añade tag a todas las métricas para identificar la app |

### 3. Configuración de Seguridad

**Archivo:** `SecurityConfig.java`

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            // Endpoints de Actuator son públicos (para Prometheus)
            .requestMatchers("/actuator/**").permitAll()
            // ... otras reglas
        );
    return http.build();
}
```

**¿Por qué públicos?**
- Prometheus necesita acceso sin autenticación para hacer scraping
- En producción, restringe el acceso a nivel de red (firewall/security groups)
- Alternativamente, configura autenticación básica en Prometheus

### 4. Endpoints Disponibles

Una vez configurado, Actuator expone estos endpoints:

| Endpoint | URL | ¿Qué proporciona? |
|----------|-----|-------------------|
| **Health** | `http://localhost:8080/actuator/health` | Estado de la app, DB, disco |
| **Metrics** | `http://localhost:8080/actuator/metrics` | Lista de todas las métricas |
| **Prometheus** | `http://localhost:8080/actuator/prometheus` | Métricas en formato Prometheus |
| **Info** | `http://localhost:8080/actuator/info` | Información de la aplicación |

**Ejemplo de salida del endpoint `/actuator/health`:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**Ejemplo de salida del endpoint `/actuator/prometheus`:**
```prometheus
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{application="credit-application-service",area="heap"} 1.23456789E8

# HELP http_server_requests_seconds Duration of HTTP server request handling
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{application="credit-application-service",method="GET",uri="/api/applications"} 42.0
http_server_requests_seconds_sum{application="credit-application-service",method="GET",uri="/api/applications"} 0.523
```

---

## 🔥 Prometheus

### 1. Configuración de Docker

**Archivo:** `docker-compose.yml`

```yaml
# Prometheus - Recolección de Métricas
prometheus:
  image: prom/prometheus:v2.50.0
  container_name: coopcredit-prometheus
  ports:
    - "9090:9090"
  volumes:
    - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
    - prometheus_data:/prometheus
  command:
    - '--config.file=/etc/prometheus/prometheus.yml'
    - '--storage.tsdb.path=/prometheus'
    - '--storage.tsdb.retention.time=15d'  # Retiene datos 15 días
    - '--web.console.libraries=/usr/share/prometheus/console_libraries'
    - '--web.console.templates=/usr/share/prometheus/consoles'
    - '--web.enable-lifecycle'
  networks:
    - coopcredit-network
  depends_on:
    - credit-application-service
  restart: unless-stopped

volumes:
  prometheus_data:
    driver: local
```

**Explicación de la configuración:**

| Parámetro | Valor | ¿Qué hace? |
|-----------|-------|------------|
| `image` | `prom/prometheus:v2.50.0` | Versión específica de Prometheus |
| `ports` | `9090:9090` | Expone la UI de Prometheus |
| `retention.time` | `15d` | Guarda métricas por 15 días |
| `web.enable-lifecycle` | - | Permite recargar config sin reiniciar (POST /-/reload) |
| `prometheus_data` | volume | Persistencia de datos entre reinicios |

### 2. Configuración de Scraping

**Archivo:** `monitoring/prometheus.yml`

```yaml
# Configuración Global de Prometheus
global:
  scrape_interval: 15s       # Frecuencia de recolección de métricas
  evaluation_interval: 15s   # Frecuencia de evaluación de reglas
  external_labels:
    cluster: 'coopcredit-cluster'
    environment: 'development'

# Configuración de Scrape (Recolección)
scrape_configs:
  # Prometheus auto-monitoreo
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
        labels:
          service: 'prometheus'

  # Credit Application Service
  - job_name: 'credit-application-service'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['credit-application-service:8080']
        labels:
          application: 'credit-application-service'
          service: 'backend'
          team: 'credit'

  # Risk Central Mock Service
  - job_name: 'risk-central-mock-service'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['risk-central-mock-service:8081']
        labels:
          application: 'risk-central-mock-service'
          service: 'backend'
          team: 'risk'
```

**Explicación de la configuración:**

#### Global:
- **scrape_interval: 15s**: Prometheus consulta cada target cada 15 segundos
- **evaluation_interval: 15s**: Evalúa reglas de alerta cada 15 segundos
- **external_labels**: Labels aplicados a todas las métricas (útil para Grafana Cloud o multi-cluster)

#### Scrape Configs:
Cada `job_name` representa un servicio a monitorear:

| Campo | ¿Qué hace? |
|-------|------------|
| `job_name` | Identificador único del job (aparece como label `job="..."`) |
| `metrics_path` | Ruta del endpoint de métricas (default: `/metrics`) |
| `scrape_interval` | Frecuencia específica para este job (override del global) |
| `targets` | Lista de `host:port` a consultar |
| `labels` | Labels personalizados añadidos a todas las métricas del target |

**¿Por qué usar DNS names en targets?**
- `credit-application-service:8080` en lugar de `localhost:8080`
- Docker Compose crea un DNS interno
- Los contenedores se comunican por nombre de servicio

### 3. Flujo de Scraping

```
┌─────────────────────────────────────────────────────────┐
│                   Prometheus Server                      │
│                                                          │
│  ┌───────────────────────────────────────────────────┐ │
│  │  Scrape Manager (cada 15s)                        │ │
│  └───────┬────────────────────────────┬───────────────┘ │
│          │                            │                  │
│          ▼                            ▼                  │
│  ┌───────────────┐            ┌──────────────────┐     │
│  │  Job: credit  │            │  Job: risk-mock  │     │
│  │  Target: :8080│            │  Target: :8081   │     │
│  └───────┬───────┘            └─────────┬────────┘     │
└──────────┼─────────────────────────────┼──────────────┘
           │ GET /actuator/prometheus    │
           │                             │
           ▼                             ▼
┌──────────────────┐          ┌─────────────────────┐
│  Credit Service  │          │  Risk Mock Service  │
│  Spring Actuator │          │  Spring Actuator    │
│  Returns metrics │          │  Returns metrics    │
└──────────────────┘          └─────────────────────┘
```

### 4. Almacenamiento Time-Series

Prometheus almacena las métricas en formato time-series:

```
metric_name{label1="value1", label2="value2"} → [(timestamp1, value1), (timestamp2, value2), ...]
```

**Ejemplo real:**
```
http_server_requests_seconds_count{
  application="credit-application-service",
  method="POST",
  uri="/api/applications",
  status="200"
} → [(1702200000, 10), (1702200015, 12), (1702200030, 15), ...]
```

---

## 📈 Grafana

### 1. Configuración de Docker

**Archivo:** `docker-compose.yml`

```yaml
# Grafana - Visualización de Métricas
grafana:
  image: grafana/grafana:11.0.0
  container_name: coopcredit-grafana
  ports:
    - "3000:3000"
  environment:
    - GF_SECURITY_ADMIN_USER=admin
    - GF_SECURITY_ADMIN_PASSWORD=admin123
    - GF_INSTALL_PLUGINS=
    - GF_SERVER_ROOT_URL=http://localhost:3000
    - GF_USERS_ALLOW_SIGN_UP=false
  volumes:
    - grafana_data:/var/lib/grafana
    - ./monitoring/grafana/provisioning:/etc/grafana/provisioning
    - ./monitoring/grafana/dashboards:/var/lib/grafana/dashboards
  networks:
    - coopcredit-network
  depends_on:
    - prometheus
  restart: unless-stopped

volumes:
  grafana_data:
    driver: local
```

**Explicación de variables de entorno:**

| Variable | Valor | ¿Qué hace? |
|----------|-------|------------|
| `GF_SECURITY_ADMIN_USER` | `admin` | Usuario administrador por defecto |
| `GF_SECURITY_ADMIN_PASSWORD` | `admin123` | Contraseña (⚠️ cambiar en producción) |
| `GF_USERS_ALLOW_SIGN_UP` | `false` | Deshabilita auto-registro de usuarios |
| `GF_SERVER_ROOT_URL` | `http://localhost:3000` | URL base de Grafana |

**Volúmenes montados:**

| Volumen Local | Volumen Contenedor | Propósito |
|---------------|--------------------|-----------|
| `./monitoring/grafana/provisioning` | `/etc/grafana/provisioning` | Configuración automática de datasources y dashboards |
| `./monitoring/grafana/dashboards` | `/var/lib/grafana/dashboards` | Archivos JSON de dashboards |
| `grafana_data` | `/var/lib/grafana` | Persistencia de configuración y datos |

### 2. Provisioning de Datasource

**Archivo:** `monitoring/grafana/provisioning/datasources/prometheus.yml`

```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
    jsonData:
      timeInterval: "15s"
      httpMethod: POST
    version: 1
```

**Explicación de campos:**

| Campo | Valor | ¿Qué hace? |
|-------|-------|------------|
| `name` | `Prometheus` | Nombre del datasource en Grafana |
| `type` | `prometheus` | Tipo de datasource (Grafana sabe cómo consultar Prometheus) |
| `access` | `proxy` | Grafana server hace las peticiones (no el browser del usuario) |
| `url` | `http://prometheus:9090` | URL del servidor Prometheus (DNS interno de Docker) |
| `isDefault` | `true` | Datasource por defecto al crear nuevos dashboards |
| `editable` | `true` | Permite editar desde la UI |
| `timeInterval` | `15s` | Intervalo mínimo de consulta (debe coincidir con `scrape_interval`) |
| `httpMethod` | `POST` | Método HTTP para queries (POST soporta queries más largas) |

**¿Por qué provisioning automático?**
- **Infraestructura como código**: La configuración está versionada en Git
- **Reproducibilidad**: Mismo setup en dev, test, prod
- **Sin clicks manuales**: Al levantar Docker, todo está configurado
- **Onboarding rápido**: Nuevos desarrolladores tienen todo listo

### 3. Provisioning de Dashboards

**Archivo:** `monitoring/grafana/provisioning/dashboards/dashboards.yml`

```yaml
apiVersion: 1

providers:
  - name: 'CoopCredit Dashboards'
    orgId: 1
    folder: 'CoopCredit'
    type: file
    disableDeletion: false
    updateIntervalSeconds: 30
    allowUiUpdates: true
    options:
      path: /var/lib/grafana/dashboards
      foldersFromFilesStructure: true
```

**Explicación de campos:**

| Campo | Valor | ¿Qué hace? |
|-------|-------|------------|
| `name` | `CoopCredit Dashboards` | Nombre del provider |
| `folder` | `CoopCredit` | Carpeta en Grafana donde aparecen los dashboards |
| `type` | `file` | Lee dashboards desde archivos JSON |
| `disableDeletion` | `false` | Permite eliminar dashboards desde la UI |
| `updateIntervalSeconds` | `30` | Escanea cambios en archivos cada 30s |
| `allowUiUpdates` | `true` | Permite editar dashboards desde la UI |
| `path` | `/var/lib/grafana/dashboards` | Ruta donde buscar archivos JSON |

**Dashboards incluidos:**
- `spring-boot-overview.json`: Dashboard principal con métricas de JVM, HTTP, DB, etc.

### 4. Dashboard: Spring Boot Overview

**Archivo:** `monitoring/grafana/dashboards/spring-boot-overview.json`

Este dashboard incluye paneles para:

#### 📊 Paneles de Estado
- **Application Status**: UP/DOWN
- **Uptime**: Tiempo desde el último reinicio

#### 🧠 Paneles de Memoria JVM
- **Heap Memory Usage (%)**: `(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100`
- **JVM Memory (Heap vs Non-Heap)**: Gráfico de uso de memoria a lo largo del tiempo

#### ⚡ Paneles de CPU
- **CPU Usage (%)**: `system_cpu_usage * 100`
- **Process CPU**: CPU específica del proceso Java

#### 🌐 Paneles HTTP
- **Request Rate**: `rate(http_server_requests_seconds_count[5m])`
- **Average Latency**: `rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])`
- **HTTP Status Codes**: Breakdown de 2xx, 4xx, 5xx

#### 💾 Paneles de Base de Datos
- **Active Connections**: `hikaricp_connections_active`
- **Connection Pool Usage (%)**: `(hikaricp_connections_active / hikaricp_connections_max) * 100`

#### 🧵 Paneles de Threads
- **Thread Count**: `jvm_threads_live`
- **Thread States**: Estados de threads (runnable, waiting, blocked)

#### 🗑️ Paneles de Garbage Collection
- **GC Time**: Tiempo gastado en garbage collection
- **GC Count**: Número de GCs ejecutados

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend / API Clients                    │
│                     (Postman, React App, etc.)                   │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTP Requests
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│              Credit Application Service (Spring Boot)            │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Spring Boot Actuator                                      │  │
│  │  - Recolecta métricas JVM, HTTP, DB, etc.                 │  │
│  │  - Expone endpoints /actuator/health, /actuator/prometheus│  │
│  │  - Registra métricas en MeterRegistry (Micrometer)        │  │
│  └────────────────────────┬───────────────────────────────────┘  │
│                           │                                       │
│                           │ Métricas                              │
│                           ▼                                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Micrometer Prometheus Registry                           │  │
│  │  - Convierte métricas a formato Prometheus                │  │
│  │  - Expone /actuator/prometheus                            │  │
│  └────────────────────────┬───────────────────────────────────┘  │
└───────────────────────────┼───────────────────────────────────────┘
                            │ GET /actuator/prometheus (cada 15s)
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Prometheus Server                            │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Scrape Manager                                           │  │
│  │  - Lee prometheus.yml                                     │  │
│  │  - Scrape cada 15s: credit-app, risk-mock                │  │
│  │  - Añade labels: job, instance, application              │  │
│  └────────────────────────┬───────────────────────────────────┘  │
│                           │                                       │
│                           ▼                                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Time-Series Database (TSDB)                             │  │
│  │  - Almacena métricas con timestamps                      │  │
│  │  - Retención: 15 días                                    │  │
│  │  - Compresión eficiente                                  │  │
│  └────────────────────────┬───────────────────────────────────┘  │
│                           │                                       │
│                           │ PromQL Queries                        │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Query Engine                                             │  │
│  │  - Procesa queries PromQL                                │  │
│  │  - Calcula agregaciones, rates, percentiles              │  │
│  └────────────────────────┬───────────────────────────────────┘  │
└───────────────────────────┼───────────────────────────────────────┘
                            │ HTTP API (:9090/api/v1/query)
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Grafana Server                           │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Provisioning                                             │  │
│  │  - Auto-configura datasource Prometheus                  │  │
│  │  - Auto-importa dashboards desde JSON                    │  │
│  └────────────────────────┬───────────────────────────────────┘  │
│                           │                                       │
│                           ▼                                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Datasource Proxy                                         │  │
│  │  - Ejecuta queries a Prometheus                          │  │
│  │  - Cachea resultados                                     │  │
│  │  - Maneja autenticación                                  │  │
│  └────────────────────────┬───────────────────────────────────┘  │
│                           │                                       │
│                           ▼                                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Visualization Engine                                     │  │
│  │  - Renderiza gráficos (time-series, gauge, stat, etc.)  │  │
│  │  - Aplica transformaciones                               │  │
│  │  - Ejecuta alertas                                       │  │
│  └────────────────────────┬───────────────────────────────────┘  │
└───────────────────────────┼───────────────────────────────────────┘
                            │ HTTP :3000
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Usuario (Browser)                           │
│                   http://localhost:3000                          │
│             Dashboard: CoopCredit Spring Boot Overview           │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Flujo de Datos

### Paso 1: Generación de Métricas

```java
// En tu servicio Spring Boot
@Service
public class CreditApplicationService {
    
    // Spring Boot Actuator automáticamente registra:
    
    public CreditApplication createApplication(...) {
        // ✅ Métrica automática: http_server_requests_seconds_count
        // ✅ Métrica automática: jvm_memory_used_bytes
        // ✅ Métrica automática: hikaricp_connections_active
        
        return repository.save(application);
    }
}
```

### Paso 2: Exposición por Actuator

```
GET http://localhost:8080/actuator/prometheus

# HELP http_server_requests_seconds Duration of HTTP server request handling
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{application="credit-application-service",method="POST",uri="/api/applications",status="200"} 42.0
http_server_requests_seconds_sum{application="credit-application-service",method="POST",uri="/api/applications",status="200"} 0.523
```

### Paso 3: Scraping por Prometheus

```
[15:00:00] Prometheus → GET credit-application-service:8080/actuator/prometheus
[15:00:00] Prometheus ← 200 OK (métricas en texto plano)
[15:00:00] Prometheus → Parsea y almacena en TSDB

[15:00:15] Prometheus → GET credit-application-service:8080/actuator/prometheus
[15:00:15] Prometheus ← 200 OK (nuevas métricas)
[15:00:15] Prometheus → Parsea y almacena en TSDB

... cada 15 segundos ...
```

### Paso 4: Query desde Grafana

```
Usuario → Abre dashboard en Grafana
Grafana → Ejecuta query: rate(http_server_requests_seconds_count[5m])
Grafana → HTTP GET prometheus:9090/api/v1/query?query=...
Prometheus → Procesa query, calcula rate sobre últimos 5 minutos
Prometheus → Retorna JSON con resultados
Grafana → Renderiza gráfico time-series
Grafana → Muestra al usuario
```

### Paso 5: Actualización en Tiempo Real

```
Grafana ejecuta queries automáticamente según el refresh interval configurado:
- Cada 5s: Paneles críticos (status, CPU, memory)
- Cada 30s: Paneles de métricas de negocio
- Cada 1m: Paneles de trends y estadísticas
```

---

## 📊 Métricas Disponibles

### 1. Métricas Automáticas de JVM

| Métrica | Descripción | Query Example |
|---------|-------------|---------------|
| `jvm_memory_used_bytes` | Memoria usada (heap/non-heap) | `jvm_memory_used_bytes{area="heap"}` |
| `jvm_memory_max_bytes` | Memoria máxima disponible | `jvm_memory_max_bytes{area="heap"}` |
| `jvm_threads_live` | Número de threads activos | `jvm_threads_live` |
| `jvm_gc_pause_seconds` | Tiempo de pause por GC | `rate(jvm_gc_pause_seconds_sum[5m])` |
| `jvm_classes_loaded` | Clases cargadas en JVM | `jvm_classes_loaded` |

### 2. Métricas HTTP Automáticas

| Métrica | Descripción | Query Example |
|---------|-------------|---------------|
| `http_server_requests_seconds_count` | Número de requests HTTP | `rate(http_server_requests_seconds_count[5m])` |
| `http_server_requests_seconds_sum` | Tiempo total de requests | `rate(http_server_requests_seconds_sum[5m])` |
| `http_server_requests_seconds_bucket` | Histograma de latencias | `histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))` |

**Labels automáticos:**
- `method`: GET, POST, PUT, DELETE
- `uri`: Ruta del endpoint
- `status`: Código HTTP (200, 400, 500, etc.)
- `exception`: Clase de excepción (si hubo error)

### 3. Métricas de Base de Datos (HikariCP)

| Métrica | Descripción | Query Example |
|---------|-------------|---------------|
| `hikaricp_connections_active` | Conexiones activas | `hikaricp_connections_active` |
| `hikaricp_connections_idle` | Conexiones idle | `hikaricp_connections_idle` |
| `hikaricp_connections_max` | Conexiones máximas | `hikaricp_connections_max` |
| `hikaricp_connections_pending` | Threads esperando conexión | `hikaricp_connections_pending` |
| `hikaricp_connections_timeout_total` | Timeouts de conexión | `rate(hikaricp_connections_timeout_total[5m])` |

### 4. Métricas del Sistema

| Métrica | Descripción | Query Example |
|---------|-------------|---------------|
| `system_cpu_usage` | CPU del sistema (0-1) | `system_cpu_usage * 100` |
| `process_cpu_usage` | CPU del proceso Java (0-1) | `process_cpu_usage * 100` |
| `system_cpu_count` | Número de CPUs | `system_cpu_count` |
| `process_uptime_seconds` | Tiempo desde inicio | `process_uptime_seconds` |

### 5. Métricas Personalizadas (Opcional)

Puedes crear métricas de negocio usando el archivo `example-custom-metrics.java`:

```java
@Configuration
public class CustomMetricsConfig {
    
    @Bean
    public Counter creditApplicationsCounter(MeterRegistry registry) {
        return Counter.builder("credit.applications.total")
                .description("Total de solicitudes de crédito creadas")
                .register(registry);
    }
    
    @Bean
    public Timer creditEvaluationTimer(MeterRegistry registry) {
        return Timer.builder("credit.evaluation.duration")
                .description("Tiempo de evaluación de solicitud de crédito")
                .register(registry);
    }
}

// En tu servicio:
@Service
@RequiredArgsConstructor
public class CreditApplicationService {
    private final Counter creditApplicationsCounter;
    
    public void createApplication(...) {
        creditApplicationsCounter.increment();
        // ... lógica
    }
}
```

---

## ⚙️ Configuración Paso a Paso

### Paso 1: Añadir Dependencias

Edita `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Paso 2: Configurar Actuator

Edita `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Paso 3: Permitir Acceso en Security

Edita `SecurityConfig.java`:

```java
.requestMatchers("/actuator/**").permitAll()
```

### Paso 4: Configurar Prometheus

Crea `monitoring/prometheus.yml`:

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'credit-application-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['credit-application-service:8080']
```

### Paso 5: Configurar Grafana Datasource

Crea `monitoring/grafana/provisioning/datasources/prometheus.yml`:

```yaml
apiVersion: 1
datasources:
  - name: Prometheus
    type: prometheus
    url: http://prometheus:9090
    isDefault: true
```

### Paso 6: Configurar Docker Compose

Añade en `docker-compose.yml`:

```yaml
prometheus:
  image: prom/prometheus:v2.50.0
  volumes:
    - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml

grafana:
  image: grafana/grafana:11.0.0
  volumes:
    - ./monitoring/grafana/provisioning:/etc/grafana/provisioning
```

### Paso 7: Levantar el Stack

```bash
docker compose up -d
```

### Paso 8: Verificar

```bash
# Verificar métricas en Actuator
curl http://localhost:8080/actuator/prometheus

# Verificar targets en Prometheus
curl http://localhost:9090/api/v1/targets

# Abrir Grafana
open http://localhost:3000
# Usuario: admin, Contraseña: admin123
```

---

## ✅ Verificación y Testing

### 1. Verificar Spring Boot Actuator

```bash
# Health endpoint
curl http://localhost:8080/actuator/health | jq

# Métricas disponibles
curl http://localhost:8080/actuator/metrics | jq '.names | .[]'

# Métricas en formato Prometheus
curl http://localhost:8080/actuator/prometheus | head -50
```

**Resultado esperado:**
```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{application="credit-application-service",area="heap"} 1.23456789E8
```

### 2. Verificar Prometheus Scraping

```bash
# Ver targets configurados y su estado
curl http://localhost:9090/api/v1/targets | jq

# Verificar que está scrapeando
curl 'http://localhost:9090/api/v1/query?query=up{job="credit-application-service"}' | jq
```

**Resultado esperado:**
```json
{
  "status": "success",
  "data": {
    "resultType": "vector",
    "result": [{
      "metric": {
        "job": "credit-application-service",
        "instance": "credit-application-service:8080"
      },
      "value": [1702200000, "1"]  // 1 = UP, 0 = DOWN
    }]
  }
}
```

### 3. Verificar Grafana Datasource

```bash
# Login en Grafana
open http://localhost:3000
# Usuario: admin, Contraseña: admin123

# Ir a: Configuration → Data Sources → Prometheus
# Hacer click en "Test" → Debe mostrar "Data source is working"
```

### 4. Test de Queries PromQL

En Prometheus UI (`http://localhost:9090`), ejecuta estas queries:

#### Request Rate
```promql
rate(http_server_requests_seconds_count[5m])
```

#### Latencia P95
```promql
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))
```

#### Memoria Heap (%)
```promql
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100
```

#### Conexiones DB Activas
```promql
hikaricp_connections_active
```

### 5. Test de Dashboard en Grafana

1. Abre Grafana: `http://localhost:3000`
2. Ir a **Dashboards** → **CoopCredit** → **Spring Boot Overview**
3. Verificar que todos los paneles muestran datos
4. Cambiar time range: últimos 5 minutos, última hora, etc.
5. Hacer zoom en un gráfico
6. Hacer requests a tu API y ver el impacto en tiempo real

### 6. Generar Carga para Testing

```bash
# Generar múltiples requests
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}'
  sleep 0.1
done
```

Luego observa en Grafana:
- **Request Rate** debe incrementar
- **Latency** puede variar
- **JVM Memory** puede aumentar
- **HTTP Status Codes** debe mostrar 200s o 401s

---

## 🎓 Conceptos Clave

### ¿Qué es Observabilidad?

**Observabilidad** = Capacidad de entender el estado interno de un sistema basándose en sus outputs externos (logs, métricas, traces)

**Los 3 Pilares:**
1. **Logs**: Eventos discretos (ERROR, INFO, DEBUG)
2. **Métricas**: Valores numéricos a lo largo del tiempo (CPU, memoria, requests/s)
3. **Traces**: Camino de una request a través de múltiples servicios

Esta implementación cubre **Métricas** usando el stack Actuator + Prometheus + Grafana.

### Micrometer: El SLF4J de las Métricas

- **SLF4J**: Abstracción para logging (Logback, Log4j, etc.)
- **Micrometer**: Abstracción para métricas (Prometheus, Datadog, New Relic, etc.)

```java
// Código agnóstico del backend de métricas
Counter counter = meterRegistry.counter("my.counter");
counter.increment();

// Micrometer lo exporta a Prometheus, Datadog, etc., según la dependencia
```

### PromQL: El Lenguaje de Queries de Prometheus

PromQL es un lenguaje funcional para consultar time-series:

```promql
# Selector simple
http_server_requests_seconds_count

# Selector con filtros
http_server_requests_seconds_count{method="GET", status="200"}

# Rate: cambio por segundo en los últimos 5 minutos
rate(http_server_requests_seconds_count[5m])

# Percentil 95
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Agregación por label
sum by (uri) (rate(http_server_requests_seconds_count[5m]))
```

### Pull vs Push

**Prometheus usa modelo Pull:**
- Prometheus activamente consulta (scrape) los targets
- Targets exponen métricas en un endpoint HTTP
- Ventajas: Control centralizado, detección de targets caídos, no requiere config en targets

**Alternativa Push:**
- Aplicación envía métricas activamente (Datadog, New Relic)
- Requiere configurar cada aplicación

### Cardinality: Cuidado con los Labels

**Alta cardinality** = Muchas combinaciones únicas de labels

❌ **Mal uso** (alta cardinality):
```java
Counter.builder("requests")
       .tag("user_id", userId)  // ¡Miles de usuarios!
       .tag("request_id", requestId)  // ¡Millones de requests!
       .register(registry);
```

✅ **Buen uso** (baja cardinality):
```java
Counter.builder("requests")
       .tag("method", "GET")  // Solo: GET, POST, PUT, DELETE
       .tag("status", "200")  // Solo: 2xx, 4xx, 5xx
       .register(registry);
```

**Regla:** Labels deben tener **valores finitos y conocidos** (método HTTP, status code, endpoint, etc.)

---

## 🚀 Próximos Pasos

### 1. Añadir Métricas de Negocio

Copia el ejemplo:
```bash
cp monitoring/example-custom-metrics.java \
   credit-application-service/src/main/java/com/coopcredit/credit_application_service/infrastructure/config/CustomMetricsConfig.java
```

Personaliza y usa en tus servicios.

### 2. Crear Dashboards Personalizados

- Crear dashboard para métricas de negocio
- Añadir paneles para tasa de aprobación de créditos
- Gráfico de solicitudes por día/hora
- Top afiliados con más solicitudes

### 3. Configurar Alertas

En Grafana:
- Alerta si latencia P95 > 2 segundos
- Alerta si tasa de error > 5%
- Alerta si memoria heap > 85%
- Alerta si servicio está DOWN

### 4. Integrar Logs (ELK Stack)

- Añadir Elasticsearch + Logstash + Kibana
- Correlacionar logs con métricas
- Implementar structured logging con JSON

### 5. Añadir Distributed Tracing

- Integrar Spring Cloud Sleuth + Zipkin
- Rastrear requests a través de microservicios
- Identificar cuellos de botella

### 6. Producción

- [ ] Cambiar credenciales de Grafana
- [ ] Configurar HTTPS (reverse proxy)
- [ ] Restringir acceso a Prometheus (red interna)
- [ ] Aumentar retention de Prometheus (30-60 días)
- [ ] Configurar backups de Grafana dashboards
- [ ] Integrar alertas con Slack/PagerDuty/Email

---

## 📚 Recursos Adicionales

### Documentación Oficial
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [PromQL Cheat Sheet](https://promlabs.com/promql-cheat-sheet/)

### Archivos del Proyecto
- `monitoring/README.md`: Guía completa de monitoreo
- `monitoring/QUICK_REFERENCE.md`: Referencia rápida de comandos
- `monitoring/example-custom-metrics.java`: Ejemplos de métricas personalizadas

---

## 🎯 Resumen Ejecutivo

### ✅ Lo que se implementó:

1. **Spring Boot Actuator**
   - Dependencia: `spring-boot-starter-actuator`
   - Endpoints: `/actuator/health`, `/actuator/prometheus`
   - Métricas automáticas: JVM, HTTP, DB

2. **Micrometer Prometheus Registry**
   - Dependencia: `micrometer-registry-prometheus`
   - Formato de exportación: Prometheus text format
   - Tags personalizados: `application=credit-application-service`

3. **Prometheus Server**
   - Versión: 2.50.0
   - Scrape interval: 15 segundos
   - Retention: 15 días
   - Targets: credit-application-service, risk-central-mock

4. **Grafana**
   - Versión: 11.0.0
   - Datasource: Prometheus (auto-provisionado)
   - Dashboard: Spring Boot Overview (auto-provisionado)
   - Credenciales: admin / admin123

### 🔧 Cómo funciona:

```
Actuator expone métricas → Prometheus las scrape cada 15s → 
Grafana las visualiza en dashboards → Usuario monitorea en tiempo real
```

### 📈 Métricas disponibles:

- **JVM**: Memoria, CPU, Threads, GC
- **HTTP**: Request rate, latencia, status codes
- **Base de datos**: Connection pool, queries, timeouts
- **Sistema**: CPU, memoria, uptime

### 🎨 Dashboards:

- **Spring Boot Overview**: 12 paneles con métricas esenciales
- **Customizables**: Puedes crear tus propios dashboards desde la UI

---

**🎉 ¡Sistema de observabilidad completamente funcional y listo para producción!**

Para más detalles, consulta:
- `monitoring/README.md`
- `monitoring/QUICK_REFERENCE.md`
- Documentación oficial de cada herramienta
