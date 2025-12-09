# 🚀 Inicio Rápido - Monitoreo

## Pasos para activar el monitoreo

### 1️⃣ Iniciar la aplicación
```bash
./mvnw spring-boot:run
```

### 2️⃣ Iniciar Prometheus y Grafana
```bash
docker-compose -f docker-compose-monitoring.yml up -d
```

### 3️⃣ Acceder a las herramientas

| Herramienta | URL | Usuario | Contraseña |
|-------------|-----|---------|------------|
| **Aplicación** | http://localhost:8081 | - | - |
| **Métricas** | http://localhost:8081/actuator/prometheus | - | - |
| **Prometheus** | http://localhost:9090 | - | - |
| **Grafana** | http://localhost:3000 | admin | admin |

### 4️⃣ Ver el Dashboard

1. Abrir Grafana: http://localhost:3000
2. Login con `admin`/`admin`
3. Ir a Dashboards → "Risk Central Mock Service - Spring Boot Metrics"

## 📊 Dashboard incluye:

- ✅ Tasa de solicitudes HTTP
- ✅ Duración de solicitudes
- ✅ Uso de memoria JVM
- ✅ Uso de CPU
- ✅ Threads JVM
- ✅ Garbage Collection

## 🛑 Detener servicios

```bash
docker-compose -f docker-compose-monitoring.yml down
```

---

📖 **Documentación completa**: Ver [MONITORING.md](./MONITORING.md)
