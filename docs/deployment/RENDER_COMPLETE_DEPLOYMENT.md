# 🚀 Despliegue Completo en Render - CoopCredit System

## 📋 Resumen Ejecutivo

Este documento proporciona una guía paso a paso para desplegar el sistema CoopCredit completo en Render, incluyendo:

- ✅ **PostgreSQL 18** - Base de datos relacional
- ✅ **Risk Central Mock Service** - Microservicio de evaluación de riesgo
- ✅ **Credit Application Service** - Microservicio principal de gestión de créditos
- ✅ **Prometheus & Grafana** - Observabilidad y métricas (opcional)

---

## 🎯 Arquitectura de Despliegue

```
┌─────────────────────────────────────────────────────────────┐
│                      RENDER CLOUD                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Credit Application Service (8080)                  │  │
│  │  - Spring Boot 4.0                                  │  │
│  │  - JWT Authentication                              │  │
│  │  - Swagger UI & OpenAPI                            │  │
│  │  - Prometheus Metrics                              │  │
│  └────────────────┬─────────────────────────────────────┘  │
│                   │                                         │
│                   ├──► PostgreSQL 18 (5432)                │
│                   │    - coopcredit_db                     │
│                   │    - Flyway Migrations                 │
│                   │                                         │
│                   └──► Risk Central Mock (8081)            │
│                        - Risk Evaluation API               │
│                        - Health Check                      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 📦 Requisitos Previos

1. **Cuenta en Render** - [render.com](https://render.com)
2. **Repositorio GitHub** - Con el código de CoopCredit
3. **Git configurado** - Para hacer push de cambios
4. **Acceso a Render Dashboard** - Para monitoreo

---

## 🔧 Paso 1: Preparar el Repositorio

### 1.1 Verificar la Estructura del Proyecto

```bash
CoopCredit_SkillsTest_SpringBootM6/
├── credit-application-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── risk-central-mock-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── docker-compose.yml
├── render.yaml          # ← Configuración de Render
└── README.md
```

### 1.2 Verificar render.yaml

El archivo `render.yaml` ya está configurado con:
- PostgreSQL 18 (tipo: pserv)
- Risk Central Mock Service (tipo: web)
- Credit Application Service (tipo: web)
- Todas las variables de entorno necesarias

---

## 🌐 Paso 2: Conectar Repositorio a Render

### 2.1 Crear Cuenta en Render

1. Ve a [render.com](https://render.com)
2. Haz clic en **"Sign Up"**
3. Conecta con GitHub
4. Autoriza Render para acceder a tus repositorios

### 2.2 Conectar Repositorio

1. En Render Dashboard, haz clic en **"New +"**
2. Selecciona **"Blueprint"**
3. Busca tu repositorio: `agutierrezreginodev/CoopCredit_SkillsTest_SpringBootM6`
4. Haz clic en **"Connect"**
5. Render detectará automáticamente `render.yaml`

---

## 🚀 Paso 3: Desplegar con Blueprint

### 3.1 Revisar Configuración

Antes de aplicar el blueprint, verifica:

```yaml
services:
  - name: coopcredit-postgres      # Base de datos
  - name: risk-central-mock-service # Microservicio 1
  - name: credit-application-service # Microservicio 2
```

### 3.2 Aplicar Blueprint

1. En la página de Blueprint, haz clic en **"Apply"**
2. Render comenzará a desplegar los servicios
3. El orden de despliegue será:
   - PostgreSQL (primero)
   - Risk Central Mock Service
   - Credit Application Service (depende de los anteriores)

### 3.3 Esperar el Despliegue

**Tiempo estimado: 10-15 minutos**

Monitorea el progreso en el dashboard:
- 🟡 **Building** - Construyendo imágenes Docker
- 🟡 **Deploying** - Desplegando servicios
- 🟢 **Live** - Servicio activo

---

## 🔐 Paso 4: Configurar Variables de Entorno

### 4.1 Verificar Variables Automáticas

Render genera automáticamente:
- `JWT_SECRET` - Token secreto para JWT
- `POSTGRES_PASSWORD` - Contraseña de la base de datos
- `SPRING_DATASOURCE_URL` - URL de conexión a PostgreSQL

### 4.2 Variables Configuradas

Verifica en cada servicio:

**Credit Application Service:**
```
SPRING_PROFILES_ACTIVE = prod
SPRING_JPA_HIBERNATE_DDL_AUTO = validate
SPRING_FLYWAY_ENABLED = true
RISK_CENTRAL_URL = https://risk-central-mock-service.onrender.com
```

**Risk Central Mock Service:**
```
SERVER_PORT = 8081
JAVA_OPTS = -Xmx512m -Xms256m
```

### 4.3 Agregar Variables Adicionales (si es necesario)

Si necesitas agregar más variables:

1. Ve a **[Servicio] → Environment**
2. Haz clic en **"Add Environment Variable"**
3. Ingresa clave y valor
4. Haz clic en **"Save"**

---

## ✅ Paso 5: Verificar Despliegue

### 5.1 Health Checks

Una vez desplegado, verifica la salud de los servicios:

```bash
# Risk Central Mock
curl https://risk-central-mock-service.onrender.com/actuator/health

# Credit Application Service
curl https://credit-application-service.onrender.com/actuator/health
```

**Respuesta esperada:**
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "livenessState": {"status": "UP"},
    "readinessState": {"status": "UP"}
  }
}
```

### 5.2 Acceder a la API

**Swagger UI:**
```
https://credit-application-service.onrender.com/swagger-ui.html
```

**API Docs:**
```
https://credit-application-service.onrender.com/v3/api-docs
```

### 5.3 Probar Endpoints

#### Registrar Usuario

```bash
curl -X POST https://credit-application-service.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123",
    "documento": "12345678",
    "role": "ROLE_ADMIN"
  }'
```

#### Login

```bash
curl -X POST https://credit-application-service.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123"
  }'
```

Guarda el `token` de la respuesta para usar en otros endpoints.

#### Probar Risk Central Integration

```bash
curl -X POST https://credit-application-service.onrender.com/api/applications \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "afiliadoId": 1,
    "montoSolicitado": 5000000,
    "plazoMeses": 36,
    "tasaPropuesta": 12.5
  }'
```

---

## 📊 Paso 6: Monitoreo y Observabilidad

### 6.1 Logs en Tiempo Real

1. Ve a **[Servicio] → Logs**
2. Verás los logs en tiempo real
3. Busca errores o advertencias

### 6.2 Métricas Prometheus

Accede a las métricas:

```
https://credit-application-service.onrender.com/actuator/prometheus
```

### 6.3 Alertas (Opcional)

Configura alertas en Render:

1. Ve a **[Servicio] → Alerts**
2. Haz clic en **"Create Alert"**
3. Configura condiciones (CPU, memoria, errores)

---

## 🔄 Paso 7: Actualizaciones Automáticas

### 7.1 Auto-Deploy Habilitado

El blueprint tiene `autoDeploy: true`, lo que significa:

- Cada `git push` a `main` dispara un nuevo despliegue
- Las imágenes Docker se reconstruyen automáticamente
- El despliegue es **zero-downtime**

### 7.2 Despliegue Manual

Si necesitas desplegar manualmente:

1. Ve a **[Servicio] → Manual Deploy**
2. Haz clic en **"Deploy latest commit"**

### 7.3 Revertir a Versión Anterior

1. Ve a **[Servicio] → Deployments**
2. Selecciona un despliegue anterior
3. Haz clic en **"Redeploy"**

---

## 🛠️ Solución de Problemas

### Problema: "Service is unavailable"

**Causa:** El servicio está durmiendo (plan free)

**Solución:**
- Espera 30-50 segundos
- Intenta nuevamente
- Verifica logs: **[Servicio] → Logs**

### Problema: "Database connection failed"

**Causa:** PostgreSQL no está listo

**Solución:**
1. Verifica que PostgreSQL está en estado "Live"
2. Revisa logs de PostgreSQL
3. Intenta un re-deploy manual

### Problema: "JWT Token errors"

**Causa:** `JWT_SECRET` no está configurado correctamente

**Solución:**
1. Ve a **Credit Application Service → Environment**
2. Verifica que `JWT_SECRET` tiene un valor (debe generarse automáticamente)
3. Si está vacío, elimínalo y deja que Render lo genere

### Problema: "Risk Central integration fails"

**Causa:** URL de Risk Central incorrecta

**Solución:**
1. Verifica que Risk Central está desplegado y en estado "Live"
2. En Credit Application Service, verifica:
   ```
   RISK_CENTRAL_URL = https://risk-central-mock-service.onrender.com
   ```
3. Intenta acceder a: `https://risk-central-mock-service.onrender.com/actuator/health`

### Problema: "Build fails with Docker error"

**Causa:** Dockerfile o contexto incorrecto

**Solución:**
1. Verifica que `dockerContext` apunta a la carpeta correcta
2. Verifica que el `Dockerfile` existe en esa carpeta
3. Revisa los logs de build en Render

---

## 📈 Limitaciones del Plan Free

### Ambos Servicios Web
- 🔄 Se duermen después de 15 minutos de inactividad
- ⏱️ Primera petición después del sleep: 30-50 segundos
- ⏰ 750 horas gratis al mes (suficiente para 1 servicio 24/7)
- 💾 512 MB RAM por servicio

### Base de Datos PostgreSQL Free
- ⏰ 90 días de vida
- 💾 1 GB de almacenamiento
- 🔄 Se elimina automáticamente después de 90 días

### Soluciones

**Para evitar "sleep":**
1. Actualizar a plan **Starter** ($7/mes por servicio)
2. Usar un servicio de "keep-alive" (ping periódico)

**Para base de datos permanente:**
1. Actualizar a plan **Starter** ($7/mes)
2. Usar PostgreSQL externo (AWS RDS, etc.)

---

## 💰 Costos Estimados

### Plan Free (Actual)
```
Servicios Web:        $0  (2 servicios × Free)
Base de Datos:        $0  (PostgreSQL Free por 90 días)
─────────────────────────
Total:                $0/mes
```

### Plan Starter (Sin "sleep")
```
Servicios Web:        $14 (2 servicios × $7)
Base de Datos:        $7  (PostgreSQL Starter)
─────────────────────────
Total:                $21/mes
```

### Plan Pro (Producción)
```
Servicios Web:        $49 (2 servicios × $25)
Base de Datos:        $29 (PostgreSQL Pro)
─────────────────────────
Total:                $78/mes
```

---

## 📋 Checklist de Despliegue

### Antes de Desplegar
- [ ] Repositorio conectado a Render
- [ ] `render.yaml` presente en la raíz
- [ ] Dockerfiles presentes en ambos servicios
- [ ] `pom.xml` configurado correctamente
- [ ] Migraciones Flyway en lugar correcto

### Durante el Despliegue
- [ ] PostgreSQL desplegado correctamente
- [ ] Risk Central Mock Service desplegado
- [ ] Credit Application Service desplegado
- [ ] Todos los servicios en estado "Live"

### Después del Despliegue
- [ ] Health checks funcionando
- [ ] Swagger UI accesible
- [ ] Endpoints de autenticación funcionando
- [ ] Integración Risk Central → Credit Application OK
- [ ] JWT tokens generados correctamente
- [ ] Logs sin errores críticos
- [ ] Métricas Prometheus disponibles

### Pruebas Finales
- [ ] Registrar usuario exitosamente
- [ ] Login exitoso
- [ ] Crear solicitud de crédito
- [ ] Evaluar solicitud con Risk Central
- [ ] Ver resultados en Swagger UI

---

## 🔗 URLs Importantes

### Servicios Desplegados
```
Risk Central Mock:
  Health: https://risk-central-mock-service.onrender.com/actuator/health
  API:    https://risk-central-mock-service.onrender.com

Credit Application Service:
  Health:    https://credit-application-service.onrender.com/actuator/health
  Swagger:   https://credit-application-service.onrender.com/swagger-ui.html
  API Docs:  https://credit-application-service.onrender.com/v3/api-docs
  Metrics:   https://credit-application-service.onrender.com/actuator/prometheus
```

### Render Dashboard
```
Dashboard:  https://dashboard.render.com
Docs:       https://render.com/docs
Blueprint:  https://render.com/docs/blueprint-spec
```

---

## 📚 Documentación Adicional

- **Render Docs**: https://render.com/docs
- **Spring Boot Actuator**: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
- **PostgreSQL**: https://www.postgresql.org/docs/
- **JWT**: https://jwt.io/

---

## 🎉 ¡Listo!

Tu sistema CoopCredit está ahora desplegado en Render. 

### Próximos Pasos

1. **Monitorea los logs** regularmente
2. **Configura alertas** para errores críticos
3. **Realiza pruebas** de carga si es necesario
4. **Actualiza a plan Starter** si necesitas evitar "sleep"
5. **Configura backups** de la base de datos

---

**Última actualización**: 2025-12-10
**Versión**: 2.0 - PostgreSQL 18
**Autor**: CoopCredit Development Team

