# 🔐 Render Environment Variables Reference

## 📋 Variables por Servicio

### 1. PostgreSQL (coopcredit-postgres)

**Tipo:** `pserv` (PostgreSQL Service)

| Variable | Valor | Descripción |
|----------|-------|-------------|
| `POSTGRES_DB` | `coopcredit_db` | Nombre de la base de datos |
| `POSTGRES_USER` | `coopcredit` | Usuario de PostgreSQL |
| `POSTGRES_PASSWORD` | `[AUTO]` | Contraseña (generada automáticamente) |

**Notas:**
- Render genera automáticamente la contraseña
- La URL de conexión se proporciona automáticamente a los servicios que la necesitan
- Plan Free: 90 días de vida, 1 GB almacenamiento

---

### 2. Risk Central Mock Service

**Tipo:** `web` (Docker)

| Variable | Valor | Descripción |
|----------|-------|-------------|
| `JAVA_OPTS` | `-Xmx512m -Xms256m` | Opciones JVM (memoria) |
| `SERVER_PORT` | `8081` | Puerto del servicio |

**Configuración Adicional:**
```yaml
healthCheckPath: /actuator/health
plan: free
region: oregon
autoDeploy: true
```

**Notas:**
- No tiene dependencias de base de datos
- Health check automático cada 30 segundos
- Auto-deploy habilitado (se redespliega con cada push)

---

### 3. Credit Application Service

**Tipo:** `web` (Docker)

#### Variables Automáticas (Generadas por Render)

| Variable | Origen | Descripción |
|----------|--------|-------------|
| `SPRING_DATASOURCE_URL` | PostgreSQL | URL de conexión a la BD |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL | Contraseña de la BD |
| `JWT_SECRET` | `generateValue: true` | Token secreto para JWT |

#### Variables Configuradas

| Variable | Valor | Descripción |
|----------|-------|-------------|
| `JAVA_OPTS` | `-Xmx512m -Xms256m` | Opciones JVM |
| `SERVER_PORT` | `8080` | Puerto del servicio |
| `SPRING_PROFILES_ACTIVE` | `prod` | Perfil de Spring (producción) |
| `SPRING_DATASOURCE_USERNAME` | `coopcredit` | Usuario de BD |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `validate` | Validar esquema (no crear) |
| `SPRING_FLYWAY_ENABLED` | `true` | Habilitar migraciones Flyway |
| `RISK_CENTRAL_URL` | `https://risk-central-mock-service.onrender.com` | URL del servicio de riesgo |

**Configuración Adicional:**
```yaml
depends_on:
  - coopcredit-postgres
  - risk-central-mock-service
healthCheckPath: /actuator/health
plan: free
region: oregon
autoDeploy: true
```

**Notas:**
- Depende de PostgreSQL y Risk Central Mock
- Ejecuta migraciones Flyway automáticamente
- Valida el esquema de BD (no lo modifica)

---

## 🔄 Flujo de Variables

```
┌─────────────────────────────────────────────────────────┐
│ Render Blueprint (render.yaml)                          │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ PostgreSQL                                              │
│ ├─ POSTGRES_DB = coopcredit_db                         │
│ ├─ POSTGRES_USER = coopcredit                          │
│ └─ POSTGRES_PASSWORD = [AUTO]                          │
│                                                         │
│ Risk Central Mock Service                               │
│ ├─ JAVA_OPTS = -Xmx512m -Xms256m                       │
│ └─ SERVER_PORT = 8081                                  │
│                                                         │
│ Credit Application Service                              │
│ ├─ SPRING_DATASOURCE_URL = [FROM PostgreSQL]           │
│ ├─ SPRING_DATASOURCE_USERNAME = coopcredit             │
│ ├─ SPRING_DATASOURCE_PASSWORD = [FROM PostgreSQL]      │
│ ├─ JWT_SECRET = [AUTO]                                 │
│ ├─ RISK_CENTRAL_URL = https://risk-central-...         │
│ └─ ... (otras variables)                               │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 🔐 Variables Sensibles

### JWT_SECRET

**¿Qué es?**
- Clave secreta para firmar tokens JWT
- Debe ser única y segura
- Render la genera automáticamente

**¿Dónde se usa?**
- En `credit-application-service`
- Para firmar y validar tokens de autenticación

**¿Cómo verificar?**
```bash
# En los logs de Credit Application Service
# Busca: "JWT Secret configured"
```

### POSTGRES_PASSWORD

**¿Qué es?**
- Contraseña de acceso a PostgreSQL
- Render la genera automáticamente
- Se proporciona automáticamente a los servicios que la necesitan

**¿Dónde se usa?**
- En `credit-application-service`
- Para conectarse a la base de datos

**¿Cómo verificar?**
```bash
# En Environment de PostgreSQL
# Verifica que POSTGRES_PASSWORD tiene un valor
```

---

## 📝 Agregar Variables Adicionales

Si necesitas agregar más variables:

### Opción 1: Modificar render.yaml

```yaml
envVars:
  - key: MI_VARIABLE
    value: "mi_valor"
```

Luego hacer `git push` para que se redepliegue.

### Opción 2: Agregar en Dashboard

1. Ve a **[Servicio] → Environment**
2. Haz clic en **"Add Environment Variable"**
3. Ingresa clave y valor
4. Haz clic en **"Save"**

---

## 🔍 Verificar Variables en Render

### Desde Dashboard

1. Ve a **[Servicio] → Environment**
2. Verás todas las variables configuradas
3. Las variables generadas (como JWT_SECRET) tendrán un ícono especial

### Desde Logs

```bash
# Las variables se muestran en los logs de inicio
# Busca líneas como:
# "Spring Boot Application started"
# "Datasource URL: jdbc:postgresql://..."
```

---

## ⚙️ Variables Especiales de Render

### fromDatabase

Permite obtener valores de un servicio de base de datos:

```yaml
- key: SPRING_DATASOURCE_URL
  fromDatabase:
    name: coopcredit-postgres
    property: connectionString
```

**Propiedades disponibles:**
- `connectionString` - URL de conexión completa
- `host` - Nombre del host
- `port` - Puerto
- `database` - Nombre de la BD
- `username` - Usuario
- `password` - Contraseña

### generateValue

Permite que Render genere automáticamente un valor seguro:

```yaml
- key: JWT_SECRET
  generateValue: true
```

**Valores generados:**
- Strings aleatorios seguros
- Longitud suficiente para seguridad criptográfica
- Únicos por servicio

---

## 🔄 Actualizar Variables

### Cambiar una Variable

1. Ve a **[Servicio] → Environment**
2. Haz clic en el ícono de edición (lápiz)
3. Modifica el valor
4. Haz clic en **"Save"**
5. El servicio se reiniciará automáticamente

### Eliminar una Variable

1. Ve a **[Servicio] → Environment**
2. Haz clic en el ícono de eliminar (X)
3. Confirma la eliminación
4. El servicio se reiniciará automáticamente

---

## 📊 Variables de Monitoreo

### Prometheus Metrics

```
SPRING_ACTUATOR_METRICS_EXPORT_PROMETHEUS_ENABLED=true
```

**Acceso:**
```
https://credit-application-service.onrender.com/actuator/prometheus
```

### Health Check

```
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
```

**Acceso:**
```
https://credit-application-service.onrender.com/actuator/health
```

---

## 🚨 Troubleshooting de Variables

### Problema: Variable no se aplica

**Causa:** El servicio no se reinició

**Solución:**
1. Verifica que guardaste los cambios
2. Espera a que el servicio se reinicie
3. Si no se reinicia, haz un re-deploy manual

### Problema: Variable con valor incorrecto

**Causa:** Typo o valor incorrecto

**Solución:**
1. Ve a Environment
2. Verifica el valor exacto
3. Corrígelo y guarda
4. Verifica en logs que se aplicó correctamente

### Problema: Variable sensible expuesta

**Causa:** Variable visible en logs o código

**Solución:**
1. Cambia el valor en Environment
2. Revisa que no esté en el código
3. Haz un nuevo despliegue

---

## 📋 Checklist de Variables

- [ ] `POSTGRES_DB` = `coopcredit_db`
- [ ] `POSTGRES_USER` = `coopcredit`
- [ ] `POSTGRES_PASSWORD` tiene un valor (auto-generado)
- [ ] `SPRING_DATASOURCE_URL` apunta a PostgreSQL
- [ ] `SPRING_DATASOURCE_USERNAME` = `coopcredit`
- [ ] `JWT_SECRET` tiene un valor (auto-generado)
- [ ] `RISK_CENTRAL_URL` = `https://risk-central-mock-service.onrender.com`
- [ ] `SPRING_PROFILES_ACTIVE` = `prod`
- [ ] `SPRING_FLYWAY_ENABLED` = `true`
- [ ] Todas las variables sensibles están en Environment (no en código)

---

## 🔗 Referencias

- [Render Docs - Environment Variables](https://render.com/docs/environment-variables)
- [Spring Boot Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
- [PostgreSQL Connection Strings](https://www.postgresql.org/docs/current/libpq-connect.html)

---

**Última actualización**: 2025-12-10
**Versión**: 1.0

