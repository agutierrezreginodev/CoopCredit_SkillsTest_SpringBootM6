# Despliegue Completo en Render - CoopCredit System

## 📋 Resumen

Este proyecto contiene dos microservicios configurados para despliegue en Render:

1. **Risk Central Mock Service** - Servicio de evaluación de riesgo crediticio (puerto 8081)
2. **Credit Application Service** - Servicio principal de gestión de solicitudes de crédito (puerto 8080)

## 🚀 Despliegue Rápido

### Paso 1: Conectar Repositorio
1. Ve a [Render Dashboard](https://dashboard.render.com)
2. Click en **"New +"** → **"Blueprint"**
3. Conecta tu repositorio de GitHub
4. Selecciona: `agutierrezreginodev/CoopCredit_SkillsTest_SpringBootM6`
5. Render detectará el archivo `render.yaml`
6. Click en **"Apply"**

### Paso 2: Configurar Variables Adicionales

#### Para Credit Application Service
Después del primer despliegue, ve al servicio y agrega:

**Variables de Base de Datos:**
- `DB_USERNAME`: Usuario de la base de datos
- `DB_PASSWORD`: Contraseña de la base de datos

**Para H2 en memoria (pruebas):**
- Cambiar `SPRING_PROFILES_ACTIVE` a `dev`

### Paso 3: Esperar el Despliegue
Los dos servicios se desplegarán automáticamente. Esto puede tomar 5-10 minutos.

## 📦 Servicios Desplegados

### 1. Risk Central Mock Service

**URL:** `https://risk-central-mock-service.onrender.com`

**Endpoints:**
- `GET /actuator/health` - Health check
- `POST /api/risk/evaluate` - Evaluar riesgo crediticio
- `GET /actuator/info` - Información del servicio

**Configuración:**
- Puerto: 8081
- Memoria: 512MB max
- Plan: Free
- Health check: `/actuator/health`

### 2. Credit Application Service

**URL:** `https://credit-application-service.onrender.com`

**Endpoints:**
- `GET /actuator/health` - Health check
- `GET /swagger-ui.html` - Documentación API
- `POST /api/auth/register` - Registro de usuarios
- `POST /api/auth/login` - Login con JWT
- `POST /api/applications` - Crear solicitud de crédito
- `GET /api/applications` - Listar solicitudes
- `GET /api/applications/{id}` - Obtener solicitud

**Configuración:**
- Puerto: 8080
- Memoria: 512MB max
- Plan: Free
- Health check: `/actuator/health`
- Integración: Se conecta a Risk Central Mock Service

## 🔧 Arquitectura de Despliegue

```
┌─────────────────────────────────────────────────────┐
│                    Render Cloud                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌──────────────────────────────────────────────┐  │
│  │  Credit Application Service                  │  │
│  │  (https://credit-application-service.       │  │
│  │   onrender.com)                             │  │
│  │                                              │  │
│  │  • Puerto: 8080                             │  │
│  │  • Spring Boot + JWT + JPA                  │  │
│  │  • Base de datos (PostgreSQL/H2)            │  │
│  └──────────────┬───────────────────────────────┘  │
│                 │                                   │
│                 │ HTTP Request                      │
│                 ▼                                   │
│  ┌──────────────────────────────────────────────┐  │
│  │  Risk Central Mock Service                   │  │
│  │  (https://risk-central-mock-service.        │  │
│  │   onrender.com)                             │  │
│  │                                              │  │
│  │  • Puerto: 8081                             │  │
│  │  • API de evaluación de riesgo              │  │
│  └──────────────────────────────────────────────┘  │
│                                                     │
└─────────────────────────────────────────────────────┘
```

## 📝 Archivo render.yaml

El archivo `render.yaml` en la raíz del proyecto configura ambos servicios:

```yaml
services:
  - type: web
    name: risk-central-mock-service
    runtime: docker
    dockerfilePath: ./risk-central-mock-service/Dockerfile
    dockerContext: ./risk-central-mock-service
    
  - type: web
    name: credit-application-service
    runtime: docker
    dockerfilePath: ./credit-application-service/Dockerfile
    dockerContext: ./credit-application-service
```

**Nota importante:** Cada servicio tiene su propio `dockerContext` para evitar el error `/src: not found`.

## 🗄️ Configuración de Base de Datos

### Opción A: PostgreSQL en Render (Recomendado para producción)

1. Crear base de datos PostgreSQL:
   ```
   Dashboard → New + → PostgreSQL
   Name: credit-application-db
   Plan: Free
   ```

2. Copiar la **Internal Connection String**

3. Actualizar variables en Credit Application Service:
   ```
   DB_HOST=<host-from-connection-string>
   DB_PORT=5432
   DB_NAME=<db-name>
   DB_USERNAME=<username>
   DB_PASSWORD=<password>
   ```

### Opción B: H2 en Memoria (Solo para desarrollo)

Cambiar variable de entorno:
```
SPRING_PROFILES_ACTIVE=dev
```

## 🔐 Seguridad

### JWT Configuration
- `JWT_SECRET`: Se genera automáticamente en el primer despliegue
- `JWT_EXPIRATION`: 24 horas (86400000 ms)

### Buenas Prácticas
- ✅ Secrets no están en el código
- ✅ Usuario no-root en containers
- ✅ Variables sensibles en variables de entorno
- ✅ HTTPS automático por Render

## 📊 Monitoreo

### Health Checks
Render verifica automáticamente la salud de los servicios:
- **Risk Central**: `GET /actuator/health`
- **Credit Application**: `GET /actuator/health`

### Métricas Prometheus
Ambos servicios exponen métricas:
- `GET /actuator/prometheus`

### Logs
Accede a los logs en tiempo real:
```
Dashboard → [Nombre del Servicio] → Logs
```

## ⚠️ Limitaciones del Plan Free

### Ambos Servicios
- 🔄 Se duermen después de 15 minutos de inactividad
- ⏱️ Primera petición después del sleep: 30-50 segundos
- ⏰ 750 horas gratis al mes (suficiente para 1 servicio 24/7)
- 💾 512 MB RAM por servicio

### Base de Datos PostgreSQL Free
- ⏰ 90 días de vida
- 💾 1 GB de almacenamiento
- 🔄 Se elimina automáticamente después de 90 días

### Solución para "Sleep"
Si necesitas que los servicios estén siempre activos:
1. Actualizar a plan **Starter** ($7/mes por servicio)
2. Usar un servicio de "keep-alive" (ping periódico)

## 🧪 Pruebas de Despliegue

### 1. Verificar Health
```bash
# Risk Central
curl https://risk-central-mock-service.onrender.com/actuator/health

# Credit Application
curl https://credit-application-service.onrender.com/actuator/health
```

### 2. Probar Integración
```bash
# Registro de usuario
curl -X POST https://credit-application-service.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'

# Login
curl -X POST https://credit-application-service.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### 3. Ver Documentación API
Abre en el navegador:
```
https://credit-application-service.onrender.com/swagger-ui.html
```

## 🔄 Actualización de Servicios

### Automática
Los servicios se actualizan automáticamente cuando:
1. Haces `git push` a la rama main
2. Render detecta los cambios
3. Reconstruye las imágenes Docker
4. Despliega la nueva versión con zero-downtime

### Manual
Forzar re-deploy:
```
Dashboard → [Servicio] → Manual Deploy → Deploy latest commit
```

## 🛠️ Solución de Problemas Comunes

### Error: "/src": not found
✅ **Solucionado** - El `dockerContext` está configurado correctamente en `render.yaml`

### Service is unavailable
- ⏰ Espera 30-50 segundos (servicio despertando)
- 🔍 Revisa logs: Dashboard → Logs
- 🔄 Intenta hacer re-deploy manual

### Database connection failed
- ✅ Verifica variables de entorno
- ✅ Comprueba que la DB está activa
- ✅ Para pruebas: usa H2 (`SPRING_PROFILES_ACTIVE=dev`)

### JWT Token errors
- ✅ Asegúrate que `JWT_SECRET` tiene 32+ caracteres
- ✅ Verifica que se generó automáticamente
- ✅ Revisa logs para errores específicos

### Risk Central integration fails
- ✅ Verifica que Risk Central está desplegado
- ✅ Comprueba la URL en `RISK_CENTRAL_URL`
- ✅ Espera a que ambos servicios estén activos

## 📚 Documentación Detallada

Para más información sobre cada servicio:

- **Risk Central Mock Service**: Ver `risk-central-mock-service/DEPLOY_RENDER.md`
- **Credit Application Service**: Ver `credit-application-service/DEPLOY_RENDER.md`

## 🔗 Enlaces Útiles

- [Render Dashboard](https://dashboard.render.com)
- [Render Docs - Docker](https://render.com/docs/docker)
- [Render Docs - Blueprint](https://render.com/docs/blueprint-spec)
- [Render Docs - PostgreSQL](https://render.com/docs/databases)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## 💰 Costos Estimados

### Plan Free (Actual)
- **Servicios Web**: $0 (2 servicios × Free)
- **Base de Datos**: $0 (PostgreSQL Free por 90 días)
- **Total**: **$0/mes**

### Plan Starter (Sin "sleep")
- **Servicios Web**: $14 (2 servicios × $7)
- **Base de Datos**: $7 (PostgreSQL Starter)
- **Total**: **$21/mes**

## ✅ Checklist de Despliegue

- [ ] Repositorio conectado a Render
- [ ] Blueprint aplicado desde `render.yaml`
- [ ] Variables de entorno configuradas
- [ ] Base de datos creada y conectada
- [ ] Health checks funcionando
- [ ] Integración Risk Central → Credit Application funcionando
- [ ] JWT funcionando correctamente
- [ ] Swagger UI accesible
- [ ] Métricas Prometheus disponibles
- [ ] Logs verificados

## 🎉 ¡Listo!

Ambos microservicios están ahora desplegados en Render y listos para usar. La primera petición después del despliegue puede tardar un poco mientras los servicios se "calientan".

---

**Última actualización**: 2025-12-09
**Versión**: 1.0
