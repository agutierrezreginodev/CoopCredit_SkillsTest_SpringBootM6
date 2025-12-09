# Guía de Integración con Grafana para Métricas Dashboard

![Grafana](https://img.shields.io/badge/Grafana-11.0-orange)
![Prometheus](https://img.shields.io/badge/Prometheus-2.50-red)
![Micrometer](https://img.shields.io/badge/Micrometer-Registry-green)

Esta guía te llevará paso a paso para integrar **Grafana** con tu sistema CoopCredit para visualizar métricas, monitorear el rendimiento y crear dashboards personalizados.

---

## 📋 Tabla de Contenidos

- [Descripción General](#-descripción-general)
- [Arquitectura de Monitoreo](#-arquitectura-de-monitoreo)
- [Configuración](#-configuración)
  - [1. Actualizar Docker Compose](#1-actualizar-docker-compose)
  - [2. Configuración de Prometheus](#2-configuración-de-prometheus)
  - [3. Configuración de Grafana](#3-configuración-de-grafana)
- [Iniciar el Stack Completo](#-iniciar-el-stack-completo)
- [Acceder a Grafana](#-acceder-a-grafana)
- [Dashboards Recomendados](#-dashboards-recomendados)
- [Métricas Disponibles](#-métricas-disponibles)
- [Crear Métricas Personalizadas](#-crear-métricas-personalizadas)
- [Alertas en Grafana](#-alertas-en-grafana)
- [Troubleshooting](#-troubleshooting)
- [Mejores Prácticas](#-mejores-prácticas)

---

## 🎯 Descripción General

### ¿Qué lograrás?

Al completar esta guía, tendrás:

- ✅ **Prometheus** recolectando métricas de tus servicios Spring Boot
- ✅ **Grafana** visualizando las métricas en dashboards interactivos
- ✅ Monitoreo en tiempo real de:
  - Rendimiento de la aplicación (latencias, throughput)
  - Uso de recursos (CPU, memoria, JVM)
  - Métricas de negocio (solicitudes de crédito, aprobaciones)
  - Salud de la base de datos
  - Estado de los endpoints HTTP
- ✅ Alertas configurables para problemas críticos

### Stack de Observabilidad

```
┌─────────────────────────────────────────────────────────────┐
│                       GRAFANA (UI)                          │
│                     Puerto: 3000                            │
│              Dashboards + Visualizaciones                   │
└──────────────────────┬──────────────────────────────────────┘
                       │ (queries)
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    PROMETHEUS                               │
│                     Puerto: 9090                            │
│         Time Series Database + Scraper                      │
└──────────────────────┬──────────────────────────────────────┘
                       │ (scrape /actuator/prometheus)
                       ▼
┌──────────────────────────────────────────────────────────────┐
│         SPRING BOOT SERVICES (Micrometer)                    │
│                                                              │
│  ┌────────────────────┐       ┌──────────────────────┐     │
│  │ Credit Application │       │ Risk Central Mock    │     │
│  │   Port: 8080       │       │   Port: 8081         │     │
│  │ /actuator/prometheus│      │ /actuator/prometheus │     │
│  └────────────────────┘       └──────────────────────┘     │
└──────────────────────────────────────────────────────────────┘
```

---

## 🏗️ Arquitectura de Monitoreo

### Flujo de Datos

1. **Aplicación Spring Boot** expone métricas en formato Prometheus en `/actuator/prometheus`
2. **Prometheus** scrape (consulta) periódicamente estos endpoints cada 15 segundos
3. **Prometheus** almacena las métricas en su base de datos de series temporales
4. **Grafana** consulta Prometheus usando PromQL para crear visualizaciones
5. **Usuarios** acceden a dashboards en Grafana para monitorear el sistema

---

## 🔧 Configuración

### 1. Actualizar Docker Compose

Crea o actualiza el archivo `docker-compose.yml` para incluir Prometheus y Grafana:

```yaml
services:
  # Base de datos MySQL
  db:
    image: mysql:8.0
    container_name: coopcredit-db
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: coopcredit_db
      MYSQL_USER: coopcredit
      MYSQL_PASSWORD: coopcredit
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - coopcredit-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-proot"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Servicio Mock de Central de Riesgo
  risk-central-mock-service:
    build:
      context: ./risk-central-mock-service
      dockerfile: Dockerfile
    container_name: risk-central-mock
    ports:
      - "8081:8081"
    networks:
      - coopcredit-network
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8081/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    restart: unless-stopped

  # Servicio Principal de Solicitudes de Crédito
  credit-application-service:
    build:
      context: ./credit-application-service
      dockerfile: Dockerfile
    container_name: credit-application-service
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/coopcredit_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: coopcredit
      SPRING_DATASOURCE_PASSWORD: coopcredit
      RISK_CENTRAL_URL: http://risk-central-mock-service:8081/risk-evaluation
      SPRING_FLYWAY_ENABLED: "true"
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
      risk-central-mock-service:
        condition: service_started
    networks:
      - coopcredit-network
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s
    restart: unless-stopped

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
      - '--storage.tsdb.retention.time=15d'
      - '--web.console.libraries=/usr/share/prometheus/console_libraries'
      - '--web.console.templates=/usr/share/prometheus/consoles'
      - '--web.enable-lifecycle'
    networks:
      - coopcredit-network
    depends_on:
      - credit-application-service
    restart: unless-stopped

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

networks:
  coopcredit-network:
    driver: bridge

volumes:
  mysql_data:
    driver: local
  prometheus_data:
    driver: local
  grafana_data:
    driver: local
```

---

### 2. Configuración de Prometheus

Crea la estructura de directorios:

```bash
mkdir -p monitoring
```

Crea el archivo `monitoring/prometheus.yml`:

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

# Reglas de Alerta (opcional)
# rule_files:
#   - 'alerts/*.yml'
```

---

### 3. Configuración de Grafana

#### 3.1. Crear Estructura de Directorios

```bash
mkdir -p monitoring/grafana/provisioning/datasources
mkdir -p monitoring/grafana/provisioning/dashboards
mkdir -p monitoring/grafana/dashboards
```

#### 3.2. Configurar Datasource de Prometheus

Crea `monitoring/grafana/provisioning/datasources/prometheus.yml`:

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

#### 3.3. Configurar Provisioning de Dashboards

Crea `monitoring/grafana/provisioning/dashboards/dashboards.yml`:

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

#### 3.4. Dashboard Principal de Spring Boot

Crea `monitoring/grafana/dashboards/spring-boot-overview.json`:

```json
{
  "annotations": {
    "list": [
      {
        "builtIn": 1,
        "datasource": "-- Grafana --",
        "enable": true,
        "hide": true,
        "iconColor": "rgba(0, 211, 255, 1)",
        "name": "Annotations & Alerts",
        "type": "dashboard"
      }
    ]
  },
  "editable": true,
  "gnetId": null,
  "graphTooltip": 0,
  "id": null,
  "links": [],
  "panels": [
    {
      "datasource": "Prometheus",
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "thresholds"
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          },
          "unit": "short"
        }
      },
      "gridPos": {
        "h": 8,
        "w": 4,
        "x": 0,
        "y": 0
      },
      "id": 2,
      "options": {
        "colorMode": "value",
        "graphMode": "area",
        "justifyMode": "auto",
        "orientation": "auto",
        "reduceOptions": {
          "calcs": ["lastNotNull"],
          "fields": "",
          "values": false
        },
        "textMode": "auto"
      },
      "pluginVersion": "11.0.0",
      "targets": [
        {
          "expr": "up{application=\"credit-application-service\"}",
          "refId": "A"
        }
      ],
      "title": "Application Status",
      "type": "stat"
    },
    {
      "datasource": "Prometheus",
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 10,
            "gradientMode": "none",
            "hideFrom": {
              "tooltip": false,
              "viz": false,
              "legend": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "auto",
            "spanNulls": false
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          },
          "unit": "reqps"
        }
      },
      "gridPos": {
        "h": 8,
        "w": 10,
        "x": 4,
        "y": 0
      },
      "id": 4,
      "options": {
        "legend": {
          "calcs": [],
          "displayMode": "list",
          "placement": "bottom"
        },
        "tooltip": {
          "mode": "single"
        }
      },
      "targets": [
        {
          "expr": "rate(http_server_requests_seconds_count{application=\"credit-application-service\"}[5m])",
          "legendFormat": "{{uri}}",
          "refId": "A"
        }
      ],
      "title": "HTTP Request Rate",
      "type": "timeseries"
    },
    {
      "datasource": "Prometheus",
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 10,
            "gradientMode": "none",
            "hideFrom": {
              "tooltip": false,
              "viz": false,
              "legend": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "auto",
            "spanNulls": false
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          },
          "unit": "s"
        }
      },
      "gridPos": {
        "h": 8,
        "w": 10,
        "x": 14,
        "y": 0
      },
      "id": 6,
      "options": {
        "legend": {
          "calcs": ["mean", "max"],
          "displayMode": "table",
          "placement": "bottom"
        },
        "tooltip": {
          "mode": "single"
        }
      },
      "targets": [
        {
          "expr": "rate(http_server_requests_seconds_sum{application=\"credit-application-service\"}[5m]) / rate(http_server_requests_seconds_count{application=\"credit-application-service\"}[5m])",
          "legendFormat": "{{uri}} - {{method}}",
          "refId": "A"
        }
      ],
      "title": "HTTP Request Latency (avg)",
      "type": "timeseries"
    },
    {
      "datasource": "Prometheus",
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 30,
            "gradientMode": "none",
            "hideFrom": {
              "tooltip": false,
              "viz": false,
              "legend": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "auto",
            "spanNulls": false,
            "stacking": {
              "group": "A",
              "mode": "none"
            }
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          },
          "unit": "bytes"
        }
      },
      "gridPos": {
        "h": 8,
        "w": 12,
        "x": 0,
        "y": 8
      },
      "id": 8,
      "options": {
        "legend": {
          "calcs": ["lastNotNull"],
          "displayMode": "table",
          "placement": "bottom"
        },
        "tooltip": {
          "mode": "single"
        }
      },
      "targets": [
        {
          "expr": "jvm_memory_used_bytes{application=\"credit-application-service\"}",
          "legendFormat": "{{area}} - {{id}}",
          "refId": "A"
        }
      ],
      "title": "JVM Memory Usage",
      "type": "timeseries"
    },
    {
      "datasource": "Prometheus",
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 10,
            "gradientMode": "none",
            "hideFrom": {
              "tooltip": false,
              "viz": false,
              "legend": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "auto",
            "spanNulls": false
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          }
        }
      },
      "gridPos": {
        "h": 8,
        "w": 12,
        "x": 12,
        "y": 8
      },
      "id": 10,
      "options": {
        "legend": {
          "calcs": [],
          "displayMode": "list",
          "placement": "bottom"
        },
        "tooltip": {
          "mode": "single"
        }
      },
      "targets": [
        {
          "expr": "system_cpu_usage{application=\"credit-application-service\"}",
          "legendFormat": "System CPU",
          "refId": "A"
        },
        {
          "expr": "process_cpu_usage{application=\"credit-application-service\"}",
          "legendFormat": "Process CPU",
          "refId": "B"
        }
      ],
      "title": "CPU Usage",
      "type": "timeseries"
    },
    {
      "datasource": "Prometheus",
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 10,
            "gradientMode": "none",
            "hideFrom": {
              "tooltip": false,
              "viz": false,
              "legend": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "auto",
            "spanNulls": false
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          }
        }
      },
      "gridPos": {
        "h": 8,
        "w": 12,
        "x": 0,
        "y": 16
      },
      "id": 12,
      "options": {
        "legend": {
          "calcs": [],
          "displayMode": "list",
          "placement": "bottom"
        },
        "tooltip": {
          "mode": "single"
        }
      },
      "targets": [
        {
          "expr": "hikaricp_connections_active{application=\"credit-application-service\"}",
          "legendFormat": "Active Connections",
          "refId": "A"
        },
        {
          "expr": "hikaricp_connections_idle{application=\"credit-application-service\"}",
          "legendFormat": "Idle Connections",
          "refId": "B"
        },
        {
          "expr": "hikaricp_connections{application=\"credit-application-service\"}",
          "legendFormat": "Total Connections",
          "refId": "C"
        }
      ],
      "title": "Database Connection Pool",
      "type": "timeseries"
    },
    {
      "datasource": "Prometheus",
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "bars",
            "fillOpacity": 70,
            "gradientMode": "none",
            "hideFrom": {
              "tooltip": false,
              "viz": false,
              "legend": false
            },
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "auto",
            "spanNulls": false
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              }
            ]
          }
        }
      },
      "gridPos": {
        "h": 8,
        "w": 12,
        "x": 12,
        "y": 16
      },
      "id": 14,
      "options": {
        "legend": {
          "calcs": ["sum"],
          "displayMode": "table",
          "placement": "bottom"
        },
        "tooltip": {
          "mode": "single"
        }
      },
      "targets": [
        {
          "expr": "increase(http_server_requests_seconds_count{application=\"credit-application-service\", status=~\"2..\"}[5m])",
          "legendFormat": "2xx Success",
          "refId": "A"
        },
        {
          "expr": "increase(http_server_requests_seconds_count{application=\"credit-application-service\", status=~\"4..\"}[5m])",
          "legendFormat": "4xx Client Error",
          "refId": "B"
        },
        {
          "expr": "increase(http_server_requests_seconds_count{application=\"credit-application-service\", status=~\"5..\"}[5m])",
          "legendFormat": "5xx Server Error",
          "refId": "C"
        }
      ],
      "title": "HTTP Status Codes (5min)",
      "type": "timeseries"
    }
  ],
  "schemaVersion": 38,
  "style": "dark",
  "tags": ["spring-boot", "coopcredit", "overview"],
  "templating": {
    "list": []
  },
  "time": {
    "from": "now-1h",
    "to": "now"
  },
  "timepicker": {},
  "timezone": "",
  "title": "CoopCredit - Spring Boot Overview",
  "uid": "coopcredit-overview",
  "version": 1,
  "weekStart": ""
}
```

---

## 🚀 Iniciar el Stack Completo

### 1. Iniciar con Docker Compose

```bash
# Iniciar todos los servicios incluyendo Grafana y Prometheus
docker compose up -d

# Ver logs
docker compose logs -f

# Ver solo logs de Grafana
docker compose logs -f grafana

# Ver solo logs de Prometheus
docker compose logs -f prometheus
```

### 2. Verificar que todos los servicios estén corriendo

```bash
docker compose ps
```

Deberías ver todos los servicios con estado **Up**:
- `coopcredit-db`
- `risk-central-mock`
- `credit-application-service`
- `coopcredit-prometheus`
- `coopcredit-grafana`

### 3. Verificar Endpoints

```bash
# MySQL
docker exec -it coopcredit-db mysqladmin ping

# Risk Central Mock
curl http://localhost:8081/health

# Credit Application Service
curl http://localhost:8080/actuator/health

# Prometheus (debe retornar métricas)
curl http://localhost:8080/actuator/prometheus | head -20

# Prometheus UI
curl -I http://localhost:9090

# Grafana
curl -I http://localhost:3000
```

---

## 🎨 Acceder a Grafana

### 1. Acceder a la Interfaz Web

Abre tu navegador y ve a:

```
http://localhost:3000
```

### 2. Credenciales de Login

- **Usuario:** `admin`
- **Contraseña:** `admin123`

> **Nota:** En producción, cambia estas credenciales inmediatamente.

### 3. Verificar Datasource

1. Ve a **Configuration** → **Data Sources**
2. Deberías ver **Prometheus** configurado y funcionando
3. Click en **Test** para verificar la conexión

### 4. Abrir Dashboard

1. Ve a **Dashboards** → **Browse**
2. Navega a la carpeta **CoopCredit**
3. Abre el dashboard **CoopCredit - Spring Boot Overview**

---

## 📊 Dashboards Recomendados

### 1. Dashboard Principal (Ya incluido)

**CoopCredit - Spring Boot Overview** incluye:
- Estado de la aplicación (Up/Down)
- Tasa de requests HTTP
- Latencia promedio por endpoint
- Uso de memoria JVM
- Uso de CPU
- Pool de conexiones de base de datos
- Distribución de códigos de estado HTTP

### 2. Importar Dashboards Comunitarios

Grafana tiene dashboards pre-construidos que puedes importar:

#### Spring Boot 2.1 Statistics
1. En Grafana, ve a **Dashboards** → **Import**
2. Ingresa el ID: **10280**
3. Click **Load**
4. Selecciona datasource: **Prometheus**
5. Click **Import**

#### JVM (Micrometer)
1. Dashboard ID: **4701**
2. Importa siguiendo los mismos pasos

#### MySQL Overview
1. Dashboard ID: **7362**
2. Requiere configurar MySQL Exporter (opcional)

---

## 📈 Métricas Disponibles

### Métricas HTTP

```promql
# Total de requests
http_server_requests_seconds_count

# Latencia promedio
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# Requests por segundo
rate(http_server_requests_seconds_count[1m])

# P95 latency
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Errors 5xx
http_server_requests_seconds_count{status=~"5.."}
```

### Métricas JVM

```promql
# Memoria heap usada
jvm_memory_used_bytes{area="heap"}

# Memoria heap máxima
jvm_memory_max_bytes{area="heap"}

# GC pause time
rate(jvm_gc_pause_seconds_sum[5m])

# Threads activos
jvm_threads_live_threads
```

### Métricas de Base de Datos

```promql
# Conexiones activas
hikaricp_connections_active

# Tiempo de espera por conexión
hikaricp_connections_acquire_seconds

# Pool usage
hikaricp_connections_active / hikaricp_connections_max
```

### Métricas de Sistema

```promql
# CPU del sistema
system_cpu_usage

# CPU del proceso
process_cpu_usage

# Uptime
process_uptime_seconds
```

---

## 🔧 Crear Métricas Personalizadas

### 1. Crear un Bean de Métricas

Crea la clase `config/MetricsConfig.java`:

```java
package com.coopcredit.creditapplicationservice.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter creditApplicationsCounter(MeterRegistry registry) {
        return Counter.builder("credit.applications.total")
                .description("Total de solicitudes de crédito creadas")
                .tag("type", "creation")
                .register(registry);
    }

    @Bean
    public Counter creditApplicationsApprovedCounter(MeterRegistry registry) {
        return Counter.builder("credit.applications.approved")
                .description("Total de solicitudes de crédito aprobadas")
                .tag("status", "approved")
                .register(registry);
    }

    @Bean
    public Counter creditApplicationsRejectedCounter(MeterRegistry registry) {
        return Counter.builder("credit.applications.rejected")
                .description("Total de solicitudes de crédito rechazadas")
                .tag("status", "rejected")
                .register(registry);
    }

    @Bean
    public Timer creditEvaluationTimer(MeterRegistry registry) {
        return Timer.builder("credit.evaluation.duration")
                .description("Tiempo de evaluación de crédito")
                .tag("process", "evaluation")
                .register(registry);
    }
}
```

### 2. Usar las Métricas en tu Servicio

Actualiza tu servicio de aplicación:

```java
package com.coopcredit.creditapplicationservice.application.services;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditApplicationService {

    private final Counter creditApplicationsCounter;
    private final Counter creditApplicationsApprovedCounter;
    private final Counter creditApplicationsRejectedCounter;
    private final Timer creditEvaluationTimer;

    // ... otros dependencies

    public CreditApplication createApplication(CreateApplicationRequest request) {
        // Incrementar contador
        creditApplicationsCounter.increment();
        
        // ... lógica existente
        
        return application;
    }

    public EvaluationResult evaluateApplication(Long id) {
        // Medir tiempo de evaluación
        return creditEvaluationTimer.recordCallable(() -> {
            // ... lógica de evaluación
            
            EvaluationResult result = performEvaluation(id);
            
            // Incrementar contadores según resultado
            if (result.isApproved()) {
                creditApplicationsApprovedCounter.increment();
            } else {
                creditApplicationsRejectedCounter.increment();
            }
            
            return result;
        });
    }
}
```

### 3. Visualizar Métricas Personalizadas en Grafana

Crea un nuevo panel en Grafana:

**Query para solicitudes totales:**
```promql
increase(credit_applications_total[5m])
```

**Query para tasa de aprobación:**
```promql
rate(credit_applications_approved[5m]) / rate(credit_applications_total[5m]) * 100
```

**Query para tiempo de evaluación (P95):**
```promql
histogram_quantile(0.95, rate(credit_evaluation_duration_bucket[5m]))
```

---

## 🚨 Alertas en Grafana

### 1. Configurar Alerta por Alta Latencia

1. En Grafana, abre el panel de **HTTP Request Latency**
2. Click en el título del panel → **Edit**
3. Ve a la pestaña **Alert**
4. Click **Create alert rule from this panel**
5. Configura:
   - **Name:** Alta latencia en API
   - **Condition:** `WHEN avg() OF query(A, 5m, now) IS ABOVE 2` (2 segundos)
   - **Evaluate:** Every 1m for 5m
6. Añade **Notification channel** (email, Slack, etc.)
7. **Save**

### 2. Alerta por Errores 5xx

Query:
```promql
increase(http_server_requests_seconds_count{status=~"5..", application="credit-application-service"}[5m]) > 10
```

Condición: Más de 10 errores en 5 minutos

### 3. Alerta por Memoria Alta

Query:
```promql
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100 > 85
```

Condición: Uso de heap > 85%

### 4. Alerta por Aplicación Caída

Query:
```promql
up{application="credit-application-service"} == 0
```

Condición: Servicio down

---

## 🔍 Troubleshooting

### Problema: Grafana no muestra datos

**Solución:**

1. Verificar que Prometheus esté scrapeando correctamente:
   ```bash
   curl http://localhost:9090/api/v1/targets
   ```
   
2. Verificar que la aplicación esté exponiendo métricas:
   ```bash
   curl http://localhost:8080/actuator/prometheus
   ```

3. Verificar logs de Prometheus:
   ```bash
   docker compose logs prometheus
   ```

4. Verificar conectividad desde Grafana a Prometheus:
   ```bash
   docker exec -it coopcredit-grafana wget -O- http://prometheus:9090/api/v1/query?query=up
   ```

### Problema: Prometheus no puede acceder a servicios

**Solución:**

1. Verificar que todos los servicios estén en la misma red Docker:
   ```bash
   docker network inspect coopcredit_coopcredit-network
   ```

2. Asegurarse de usar nombres de servicio (no localhost) en `prometheus.yml`:
   ```yaml
   - targets: ['credit-application-service:8080']  # ✅ Correcto
   - targets: ['localhost:8080']                   # ❌ Incorrecto en Docker
   ```

3. Reiniciar Prometheus:
   ```bash
   docker compose restart prometheus
   ```

### Problema: Dashboard muestra "No Data"

**Solución:**

1. Verificar rango de tiempo (esquina superior derecha en Grafana)
2. Ejecutar query manualmente en Prometheus: http://localhost:9090
3. Verificar que el filtro `application` coincida:
   ```promql
   # Listar todas las aplicaciones disponibles
   group by (application) (up)
   ```

### Problema: Grafana no guarda cambios en dashboards

**Solución:**

1. Si usas provisioning, los dashboards son read-only por defecto
2. Para hacer editable, cambiar en `dashboards.yml`:
   ```yaml
   allowUiUpdates: true
   ```
3. O crear un nuevo dashboard desde cero (no provisionado)

---

## ✅ Mejores Prácticas

### 1. Métricas

- ✅ Usa nombres descriptivos: `credit.applications.approved` en vez de `counter1`
- ✅ Añade tags relevantes: `{status="approved", team="credit"}`
- ✅ No crees demasiadas métricas (límite: cardinalidad baja)
- ✅ Usa los tipos correctos:
  - **Counter:** Valores que solo incrementan (requests, errors)
  - **Gauge:** Valores que suben/bajan (conexiones activas, memoria)
  - **Histogram:** Distribución de valores (latencias, tamaños)
  - **Timer:** Similar a histogram pero para medir tiempo

### 2. Dashboards

- ✅ Agrupa paneles por función (HTTP, JVM, Database)
- ✅ Usa colores consistentes (verde=ok, rojo=error)
- ✅ Incluye descripciones en paneles
- ✅ Configura rangos de tiempo adecuados (1h, 6h, 24h)
- ✅ Usa variables de template para filtrar por instancia/ambiente

### 3. Alertas

- ✅ Alerta sobre síntomas (latencia alta) no causas (CPU alta)
- ✅ Evita alertas ruidosas (demasiadas falsas alarmas)
- ✅ Configura períodos de evaluación apropiados (5m, no 30s)
- ✅ Documenta cada alerta (por qué es importante, qué hacer)

### 4. Retention y Performance

- ✅ Configura retention apropiado en Prometheus (15 días por defecto)
- ✅ Para históricos largos, considera exportar a sistemas de largo plazo
- ✅ Monitorea el uso de recursos de Prometheus mismo
- ✅ Usa recording rules para queries complejas frecuentes

### 5. Seguridad

- ❌ No expongas Grafana/Prometheus directamente a internet sin autenticación
- ✅ Cambia contraseñas por defecto
- ✅ Usa HTTPS en producción
- ✅ Configura roles y permisos en Grafana
- ✅ Considera usar OAuth/LDAP para autenticación

---

## 📚 Queries PromQL Útiles

### Performance

```promql
# Top 5 endpoints más lentos
topk(5, rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m]))

# Percentil 99 de latencia
histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m]))

# Throughput total
sum(rate(http_server_requests_seconds_count[1m]))
```

### Errores

```promql
# Tasa de error
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m])) * 100

# Errores por endpoint
sum by (uri) (increase(http_server_requests_seconds_count{status=~"5.."}[1h]))
```

### Recursos

```promql
# % de memoria heap usada
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# Threads por estado
sum by (state) (jvm_threads_states_threads)

# Database connection pool usage
(hikaricp_connections_active / hikaricp_connections_max) * 100
```

### Business Metrics

```promql
# Solicitudes de crédito por hora
increase(credit_applications_total[1h])

# Tasa de aprobación
rate(credit_applications_approved[1h]) / rate(credit_applications_total[1h]) * 100

# Tiempo medio de evaluación
rate(credit_evaluation_duration_sum[5m]) / rate(credit_evaluation_duration_count[5m])
```

---

## 🎯 Próximos Pasos

### Nivel Intermedio

1. **Añadir MySQL Exporter** para métricas de base de datos
2. **Configurar Alertmanager** para gestión avanzada de alertas
3. **Crear dashboards de negocio** específicos de CoopCredit
4. **Implementar Distributed Tracing** con Jaeger/Zipkin

### Nivel Avanzado

1. **High Availability:** Configurar Prometheus en modo federado
2. **Long-term storage:** Integrar con Thanos o Cortex
3. **Log aggregation:** Añadir Loki para logs + traces + metrics
4. **SLO/SLI:** Definir y monitorear Service Level Objectives

---

## 📖 Referencias

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [PromQL Cheat Sheet](https://promlabs.com/promql-cheat-sheet/)

---

## 📧 Soporte

Si tienes problemas con la integración de Grafana:

1. Revisa la sección de [Troubleshooting](#-troubleshooting)
2. Verifica logs: `docker compose logs grafana prometheus`
3. Consulta la documentación oficial de Grafana
4. Abre un issue en el repositorio del proyecto

---

**¡Felicitaciones! 🎉** Ahora tienes un stack completo de observabilidad con Grafana, Prometheus y métricas de Spring Boot.

**Desarrollado con ❤️ para CoopCredit System**
