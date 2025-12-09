# CoopCredit - Sistema de Gestión de Créditos

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Docker](https://img.shields.io/badge/Docker-Compose-blue)

Sistema profesional de gestión de solicitudes de crédito para cooperativas, construido con **arquitectura hexagonal**, seguridad **JWT**, y **microservicios**.

---

## 🚀 Inicio Rápido

### Un Comando para Todo

```bash
./manage.sh start
```

Esto iniciará automáticamente:
- ✅ MySQL (Docker)
- ✅ Risk Central Mock Service
- ✅ Credit Application Service
- ✅ Todas las verificaciones de salud

### Detener el Sistema

```bash
./manage.sh stop
```

### Ver Estado

```bash
./manage.sh status
```

### Otros Comandos

```bash
./manage.sh help        # Ver todos los comandos disponibles
./manage.sh logs        # Ver logs de los servicios
./manage.sh restart     # Reiniciar el sistema
./manage.sh clean       # Limpiar todo
./manage.sh docker      # Usar Docker Compose
```

---

## 📋 Tabla de Contenidos

- [Descripción]()
- [Arquitectura]()
- [Diagramas Visuales]()
- [Tecnologías]()
- [Características]()
- [Requisitos]()
- [Instalación]()
- [Servicios Disponibles]()
- [API Endpoints]()
- [Roles y Permisos]()
- [Pruebas]()
- [Solución de Problemas]()
- [Docker]()
- [Observabilidad]()
- [**📊 Integración con Grafana**](./GRAFANA_INTEGRATION_GUIDE.md) ⭐

---

## 📖 Descripción

CoopCredit es un sistema integral que permite a cooperativas de ahorro y crédito gestionar solicitudes de crédito de manera eficiente, segura y escalable.

### Problema Resuelto

- ❌ Inconsistencias en historiales de crédito
- ❌ Errores en aprobación de solicitudes
- ❌ Falta de trazabilidad en evaluación de riesgo
- ❌ Largas demoras en estudios de crédito
- ❌ Ausencia de autenticación segura

### Solución Implementada

- ✅ Arquitectura hexagonal desacoplada
- ✅ Autenticación JWT con roles (AFILIADO, ANALISTA, ADMIN)
- ✅ Evaluación automatizada de riesgo
- ✅ Validaciones robustas con Bean Validation
- ✅ Trazabilidad completa
- ✅ Microservicios independientes
- ✅ Métricas y observabilidad

---

## 🏗️ Arquitectura

### Arquitectura Hexagonal (Puertos y Adaptadores)

```
credit-application-service/
├── domain/                    # Capa de dominio (lógica de negocio pura)
│   ├── model/                # Entidades del dominio
│   ├── ports/                # Interfaces (contratos)
│   │   ├── in/              # Casos de uso (input ports)
│   │   └── out/             # Dependencias externas (output ports)
│   ├── enums/               # Enumeraciones
│   └── exceptions/          # Excepciones del dominio
│
├── application/              # Capa de aplicación (orquestación)
│   └── services/            # Implementación de casos de uso
│
└── infrastructure/          # Capa de infraestructura (adaptadores)
    ├── adapters/           
    │   ├── jpa/            # Adaptador de persistencia (PostgreSQL)
    │   └── rest/           # Adaptador HTTP externo (Risk Central)
    ├── config/             # Configuraciones (Spring, Security, etc.)
    ├── controllers/        # Adaptadores REST (API)
    ├── security/           # Seguridad JWT
    ├── web/                # DTOs y Mappers
    └── exceptions/         # Manejo global de errores
```

### Microservicios

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| **credit-application-service** | 8080 | Servicio principal de gestión de créditos |
| **risk-central-mock-service** | 8081 | Servicio simulado de evaluación de riesgo |
| **MySQL** | 3306 | Base de datos relacional |
| **Prometheus** | 9090 | Recolección de métricas |
| **Grafana** | 3000 | Visualización de métricas y dashboards |

---

## 📊 Diagramas Visuales

El proyecto incluye **8 diagramas completos en formato Mermaid** que documentan toda la arquitectura:

📄 **Ver todos los diagramas:** [diagrams.md](./diagrams.md)

### Diagramas Disponibles

1. **🏗️ Arquitectura Hexagonal** - Muestra las 3 capas (domain, application, infrastructure) con puertos y adaptadores
2. **🎯 Casos de Uso** - Todos los casos de uso por rol (AFILIADO, ANALISTA, ADMIN)
3. **🔄 Secuencia de Evaluación** - Flujo completo del proceso de evaluación de crédito
4. **🌐 Arquitectura de Microservicios** - Comunicación entre servicios y componentes Docker
5. **🔐 Autenticación y Autorización** - Flujo de registro, login y requests autenticados
6. **📊 Modelo de Datos (ER)** - Relaciones entre entidades de la base de datos
7. **🔄 Flujo de Evaluación** - Decisiones y validaciones en el proceso de aprobación
8. **🔧 Diagrama de Deployment** - Configuración de contenedores y recursos

> 💡 **Tip:** Los diagramas Mermaid se visualizan automáticamente en GitHub, GitLab y VS Code con la extensión Mermaid Preview.

---

## 🛠️ Tecnologías

### Backend
- **Java 17**: Lenguaje de programación
- **Spring Boot 4.0.0**: Framework principal
- **Spring Data JPA**: Persistencia de datos
- **Spring Security**: Seguridad y autenticación
- **JWT (jjwt 0.11.5)**: Tokens de autenticación
- **MapStruct 1.5.5**: Mapeo de objetos DTO ↔ Entity
- **Flyway**: Migraciones versionadas de base de datos

### Base de Datos
- **MySQL 8.0**: Base de datos principal

### Observabilidad
- **Spring Actuator**: Health checks y métricas
- **Micrometer + Prometheus**: Métricas exportadas

### Documentación
- **Springdoc OpenAPI 3**: Documentación Swagger automática

### Testing
- **JUnit 5**: Pruebas unitarias
- **Mockito**: Mocks
- **Testcontainers**: Pruebas de integración con contenedores

### DevOps
- **Docker**: Contenedorización
- **Docker Compose**: Orquestación local
- **Maven**: Gestión de dependencias

---

## ✨ Características

### Funcionales

#### 1. Gestión de Afiliados
- ✅ Registro de afiliados con validación completa
- ✅ Actualización de información
- ✅ Cambio de estado (ACTIVO/INACTIVO)
- ✅ Consulta por documento o ID
- ✅ Validación de salario > 0

#### 2. Gestión de Solicitudes de Crédito
- ✅ Creación de solicitudes
- ✅ Evaluación automatizada con políticas de negocio:
  - Afiliado debe estar ACTIVO
  - Antigüedad mínima de 6 meses
  - Monto máximo = 3 veces el salario
  - Score crediticio mínimo de 500
  - Nivel de riesgo no ALTO
  - Ratio cuota/ingreso máximo 40%
- ✅ Integración con central de riesgo externa
- ✅ Estados: PENDIENTE, APROBADO, RECHAZADO
- ✅ Trazabilidad completa de decisiones

#### 3. Autenticación y Autorización
- ✅ Registro de usuarios
- ✅ Login con JWT
- ✅ 3 Roles: AFILIADO, ANALISTA, ADMIN
- ✅ Control de acceso por endpoint
- ✅ Tokens con expiración (24h)

#### 4. Evaluación de Riesgo
- ✅ Servicio mock de central de riesgo
- ✅ Score consistente por documento (300-950)
- ✅ Clasificación: BAJO, MEDIO, ALTO
- ✅ Comunicación REST entre microservicios

### No Funcionales

- ✅ Arquitectura hexagonal desacoplada
- ✅ Principios SOLID aplicados
- ✅ Validaciones con Bean Validation
- ✅ Manejo global de errores (RFC 7807)
- ✅ Logging estructurado
- ✅ Transacciones ACID
- ✅ Prevención de N+1 con @EntityGraph
- ✅ Migraciones versionadas con Flyway
- ✅ Dockerización multi-stage
- ✅ Métricas y observabilidad
- ✅ Documentación OpenAPI/Swagger

---

## 📋 Requisitos Previos

- **Java 17** o superior
- **Maven 3.8+**
- **Docker** (recomendado)
- **Git**

---

## 🔧 Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/tu-usuario/CoopCredit-System.git
cd CoopCredit-System
```

### 2. Dar Permisos al Script de Gestión

```bash
chmod +x manage.sh
```

### 3. Iniciar el Sistema

```bash
./manage.sh start
```

El script automáticamente:
1. Inicia MySQL con Docker
2. Espera a que MySQL esté listo
3. Inicia Risk Central Mock Service
4. Inicia Credit Application Service
5. Verifica que todo esté funcionando

---

## 📊 Servicios Disponibles

Una vez iniciado el sistema:

| Servicio | URL | Descripción |
|----------|-----|-------------|
| **MySQL** | `localhost:3306` | Base de datos |
| | `DB: coopcredit_db` | |
| | `User: coopcredit / Pass: coopcredit` | |
| **Risk Central Mock** | http://localhost:8081 | Evaluación de riesgo |
| | http://localhost:8081/health | Health check |
| **API Principal** | http://localhost:8080 | API REST principal |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Documentación interactiva ⭐ |
| **API Docs** | http://localhost:8080/v3/api-docs | OpenAPI JSON |
| **Health Check** | http://localhost:8080/actuator/health | Estado del servicio |
| **Actuator Prometheus** | http://localhost:8080/actuator/prometheus | Métricas exportadas |
| **Prometheus UI** | http://localhost:9090 | Interfaz de Prometheus |
| **Grafana** | http://localhost:3000 | Dashboards de métricas ⭐ |

---

## 📡 Endpoints API

### Autenticación

#### Registrar Usuario
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "juan.perez",
  "password": "password123",
  "role": "ROLE_AFILIADO",
  "documento": "1017654311"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "juan.perez",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "juan.perez",
  "role": "ROLE_AFILIADO",
  "mensaje": "Login exitoso"
}
```

### Afiliados

#### Crear Afiliado
```http
POST /api/affiliates
Authorization: Bearer {token}
Content-Type: application/json

{
  "documento": "1017654311",
  "nombre": "Juan Pérez García",
  "salario": 3000000.00,
  "fechaAfiliacion": "2022-01-15",
  "estado": "ACTIVO"
}
```

#### Listar Afiliados
```http
GET /api/affiliates
Authorization: Bearer {token}
```

#### Obtener Afiliado por ID
```http
GET /api/affiliates/{id}
Authorization: Bearer {token}
```

#### Cambiar Estado
```http
PATCH /api/affiliates/{id}/status?newStatus=INACTIVO
Authorization: Bearer {token}
```

### Solicitudes de Crédito

#### Crear Solicitud
```http
POST /api/applications
Authorization: Bearer {token}
Content-Type: application/json

{
  "afiliadoId": 1,
  "montoSolicitado": 5000000.00,
  "plazoMeses": 36,
  "tasaPropuesta": 12.5
}
```

#### Evaluar Solicitud
```http
POST /api/applications/{id}/evaluate
Authorization: Bearer {token}
```

#### Listar Solicitudes Pendientes
```http
GET /api/applications/pending
Authorization: Bearer {token}
```

#### Listar Solicitudes por Afiliado
```http
GET /api/applications/affiliate/{afiliadoId}
Authorization: Bearer {token}
```

### Documentación Completa

📚 **Swagger UI**: http://localhost:8080/swagger-ui.html (Recomendado)
📄 **OpenAPI JSON**: http://localhost:8080/v3/api-docs

---

## 👥 Roles y Permisos

| Endpoint | AFILIADO | ANALISTA | ADMIN |
|----------|----------|----------|-------|
| POST /api/affiliates | ❌ | ✅ | ✅ |
| GET /api/affiliates | ✅ | ✅ | ✅ |
| POST /api/applications | ✅ | ❌ | ✅ |
| POST /api/applications/*/evaluate | ❌ | ✅ | ✅ |
| GET /api/applications/pending | ❌ | ✅ | ✅ |

### Usuarios Precargados

| Username | Password | Rol | Documento |
|----------|----------|-----|-----------|
| admin | admin123 | ROLE_ADMIN | - |
| analista1 | analista123 | ROLE_ANALISTA | - |
| juan.perez | afiliado123 | ROLE_AFILIADO | 1017654311 |
| maria.gonzalez | afiliado123 | ROLE_AFILIADO | 1023456789 |

---

## 🔄 Flujo de Evaluación

```
1. AFILIADO crea solicitud → Estado: PENDIENTE

2. ANALISTA ejecuta evaluación:
   ├── Verifica afiliado ACTIVO
   ├── Verifica antigüedad ≥ 6 meses
   ├── Verifica monto ≤ 3 × salario
   ├── Consulta central de riesgo (HTTP)
   ├── Verifica score ≥ 500
   ├── Verifica nivel ≠ ALTO
   └── Verifica ratio cuota/ingreso ≤ 40%

3. Sistema decide:
   ├── APROBADO (todas las validaciones OK)
   └── RECHAZADO (alguna validación falla)

4. Respuesta con evaluación completa
```

---

## 🧪 Pruebas

### Pruebas Rápidas

#### 1. Verificar MySQL
```bash
docker exec -it coopcredit-mysql mysql -u coopcredit -pcoopcredit coopcredit_db

# Ver tablas creadas
SHOW TABLES;

# Ver migraciones aplicadas
SELECT * FROM flyway_schema_history;

# Salir
exit
```

#### 2. Probar Risk Central Mock
```bash
curl -X POST http://localhost:8081/risk-evaluation \
  -H "Content-Type: application/json" \
  -d '{
    "documento": "12345678",
    "montoSolicitado": 5000000
  }'
```

**Respuesta esperada:**
```json
{
  "documento": "12345678",
  "score": 750,
  "nivelRiesgo": "MEDIO",
  "detalle": "Evaluación completada exitosamente"
}
```

#### 3. Probar API Principal

**Registrar usuario:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123",
    "documento": "12345678",
    "role": "ROLE_ADMIN"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123"
  }'
```

Guarda el `token` de la respuesta para usar en otros endpoints.

### Pruebas Unitarias

```bash
cd credit-application-service
./mvnw test
```

### Pruebas de Integración

```bash
./mvnw verify
```

### Coverage

```bash
./mvnw clean verify jacoco:report
# Reporte en: target/site/jacoco/index.html
```

---

## ❗ Solución de Problemas

### Docker sin permisos

```bash
# Agregar usuario al grupo docker
sudo usermod -aG docker $USER

# Aplicar cambios (o reiniciar sesión)
newgrp docker

# Verificar
docker ps
```

### Puerto 3306 ya está en uso

```bash
# Ver qué está usando el puerto
sudo lsof -i :3306

# Opción 1: Detener MySQL local
sudo systemctl stop mysql

# Opción 2: Usar otro puerto en Docker
# Modifica el script manage.sh para usar -p 3307:3306
```

### Puerto 8080 ya está en uso

```bash
# Ver qué está usando el puerto
sudo lsof -i :8080

# Detener el servicio
kill <PID>

# O cambiar puerto en application.yml
server:
  port: 8081
```

### Spring Boot no conecta a MySQL

**Verificar que MySQL esté corriendo:**
```bash
docker ps | grep mysql
docker exec coopcredit-mysql mysqladmin ping -h localhost -u root -proot
```

**Verificar credenciales en `application.yml`:**
```yaml
datasource:
  url: jdbc:mysql://localhost:3306/coopcredit_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
  username: coopcredit
  password: coopcredit
```

### Ver logs del sistema

```bash
./manage.sh logs
```

O individualmente:
```bash
# MySQL
docker logs -f coopcredit-mysql

# Risk Central Mock
tail -f /tmp/risk-central.log

# Credit Application API
tail -f /tmp/credit-api.log
```

### Limpiar todo y empezar de cero

```bash
./manage.sh clean
./manage.sh start
```

---

## 🐳 Docker

### Opción 1: Usar el Script de Gestión (Recomendado)

```bash
./manage.sh docker
```

### Opción 2: Docker Compose Manual

```bash
# Iniciar todo el sistema
docker compose up --build -d

# Ver logs
docker compose logs -f

# Ver estado
docker compose ps

# Detener
docker compose down

# Detener y eliminar volúmenes (incluyendo datos de MySQL)
docker compose down -v
```

### Construir Imágenes Manualmente

```bash
# Servicio principal
docker build -t coopcredit/credit-application-service:latest ./credit-application-service

# Risk central mock
docker build -t coopcredit/risk-central-mock:latest ./risk-central-mock-service
```

---

## 📊 Observabilidad

El sistema incluye un stack completo de observabilidad con **Prometheus** y **Grafana**.

### 🎯 Guía Completa de Grafana

📘 **[Ver Guía de Integración con Grafana](./GRAFANA_INTEGRATION_GUIDE.md)**

Incluye:
- Configuración paso a paso de Prometheus y Grafana
- Dashboards pre-configurados para Spring Boot
- Métricas de negocio personalizadas
- Alertas configurables
- Queries PromQL útiles
- Troubleshooting completo

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Métricas
```bash
# Listado de métricas disponibles
curl http://localhost:8080/actuator/metrics

# Métricas específicas
curl http://localhost:8080/actuator/metrics/http.server.requests
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

### Prometheus
```bash
# Métricas en formato Prometheus
curl http://localhost:8080/actuator/prometheus

# Interfaz web de Prometheus
open http://localhost:9090
```

### Grafana
```bash
# Acceder a Grafana
open http://localhost:3000

# Usuario: admin
# Contraseña: admin123
```

### Logs Estructurados
Los logs incluyen:
- Timestamp
- Nivel (INFO, DEBUG, ERROR)
- Clase/Método
- TraceID (en errores)
- Mensaje contextual

---

## 📝 Comandos Útiles

```bash
# Gestión del sistema
./manage.sh start          # Iniciar todo
./manage.sh stop           # Detener todo
./manage.sh restart        # Reiniciar
./manage.sh status         # Ver estado
./manage.sh logs           # Ver logs
./manage.sh clean          # Limpiar todo

# Solo MySQL
./manage.sh mysql

# Solo Risk Central Mock
./manage.sh mock

# Usar Docker Compose
./manage.sh docker

# Ver puertos en uso
sudo netstat -tlnp | grep -E '3306|8080|8081'

# Ver procesos Java
ps aux | grep spring-boot
```

---

## 🎯 Flujo Recomendado de Desarrollo

1. **Iniciar servicios:**
   ```bash
   ./manage.sh start
   ```

2. **Abrir Swagger UI:**
   http://localhost:8080/swagger-ui.html

3. **Desarrollar y probar:**
   - Usa Postman, curl o Swagger UI
   - Verifica logs si hay errores: `./manage.sh logs`

4. **Hacer cambios en código:**
   - Los cambios se recargan automáticamente con DevTools
   - O reinicia manualmente: `./manage.sh restart`

5. **Al terminar:**
   ```bash
   ./manage.sh stop
   ```

---

## 📚 Estructura del Proyecto

```
CoopCredit-System/
├── credit-application-service/     # Servicio principal
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/.../
│   │   │   │   ├── domain/        # Lógica de negocio
│   │   │   │   ├── application/   # Casos de uso
│   │   │   │   └── infrastructure/# Adaptadores
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/  # Scripts Flyway
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
│
├── risk-central-mock-service/      # Servicio mock
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── manage.sh                       # Script de gestión ⭐
├── docker-compose.yml              # Orquestación Docker
├── README.md                       # Esta documentación
└── postman_collection.json         # Colección de Postman
```

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT.

---

## 📧 Contacto

**CoopCredit Development Team**
- Email: dev@coopcredit.com
- GitHub: [CoopCredit-System](https://github.com/tu-usuario/CoopCredit-System)

---

**Desarrollado con ❤️ usando Arquitectura Hexagonal, Spring Boot 4 y Microservicios**

