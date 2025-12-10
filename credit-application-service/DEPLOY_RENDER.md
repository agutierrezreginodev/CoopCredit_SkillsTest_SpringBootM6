# Guía de Despliegue en Render - Credit Application Service

## Requisitos Previos
- Cuenta en [Render](https://render.com) (gratis)
- Repositorio Git en GitHub/GitLab/Bitbucket
- Código subido al repositorio
- **Base de datos MySQL** (Render ofrece PostgreSQL gratis, o usar H2 en memoria para pruebas)

## Método 1: Despliegue usando render.yaml (Recomendado)

### Paso 1: Subir el código a Git
```bash
git add .
git commit -m "Preparar credit-application-service para despliegue"
git push origin main
```

### Paso 2: Conectar con Render
1. Ve a [Render Dashboard](https://dashboard.render.com)
2. Click en **"New +"** → **"Blueprint"**
3. Conecta tu repositorio de GitHub/GitLab
4. Selecciona el repositorio `CoopCredit-System`
5. Render detectará automáticamente el archivo `render.yaml`
6. Click en **"Apply"**

### Paso 3: Configurar Variables de Entorno
El servicio requiere las siguientes variables (ya configuradas en render.yaml):

#### Variables Básicas
- `JAVA_OPTS`: `-Xmx512m -Xms256m`
- `SERVER_PORT`: `8080`
- `SPRING_PROFILES_ACTIVE`: `prod`

#### Variables de Base de Datos
- `DB_HOST`: Host de la base de datos
- `DB_PORT`: Puerto de la base de datos (3306 para MySQL)
- `DB_NAME`: Nombre de la base de datos
- `DB_USERNAME`: Usuario de la base de datos (configurar manualmente)
- `DB_PASSWORD`: Contraseña de la base de datos (configurar manualmente)

#### Variables de Seguridad
- `JWT_SECRET`: Se genera automáticamente en el primer despliegue
- `JWT_EXPIRATION`: `86400000` (24 horas en ms)

#### Variables de Integración
- `RISK_CENTRAL_URL`: URL del servicio Risk Central (ej: `https://risk-central-mock-service.onrender.com`)

### Paso 4: Configurar Base de Datos

#### Opción A: Base de Datos H2 en Memoria (Solo para pruebas)
Agregar variable de entorno:
```
SPRING_PROFILES_ACTIVE=dev
```
La aplicación usará H2 automáticamente.

#### Opción B: Base de Datos PostgreSQL en Render (Gratis)
1. En Render Dashboard, click **"New +"** → **"PostgreSQL"**
2. Configura el nombre: `credit-application-db`
3. Selecciona plan **Free**
4. Una vez creada, copia la **Internal Connection String**
5. En el servicio web, agrega las variables:
   - `DB_HOST`: (desde connection string)
   - `DB_USERNAME`: (desde connection string)
   - `DB_PASSWORD`: (desde connection string)

#### Opción C: Base de Datos MySQL Externa
Agrega las variables de conexión a tu MySQL existente.

## Método 2: Despliegue Manual con Docker

### Paso 1: Crear Web Service
1. En Render Dashboard, click **"New +"** → **"Web Service"**
2. Conecta tu repositorio
3. Selecciona el repositorio y rama

### Paso 2: Configurar el Servicio
- **Name**: `credit-application-service`
- **Runtime**: Docker
- **Dockerfile Path**: `./credit-application-service/Dockerfile`
- **Docker Context**: `./credit-application-service`
- **Plan**: Free

### Paso 3: Variables de Entorno
Configura todas las variables mencionadas en el Paso 3 del Método 1.

### Paso 4: Advanced Settings
- **Health Check Path**: `/actuator/health`
- **Auto-Deploy**: Yes

### Paso 5: Deploy
Click en **"Create Web Service"**

## Verificación del Despliegue

Una vez desplegado, tu servicio estará disponible en:
```
https://credit-application-service.onrender.com
```

### Endpoints de verificación:
- **Health check**: `https://tu-app.onrender.com/actuator/health`
- **API Docs (Swagger)**: `https://tu-app.onrender.com/swagger-ui.html`
- **API Info**: `https://tu-app.onrender.com/actuator/info`
- **Métricas Prometheus**: `https://tu-app.onrender.com/actuator/prometheus`

### Endpoints de la aplicación:
- **POST** `/api/auth/register` - Registro de usuarios
- **POST** `/api/auth/login` - Login
- **POST** `/api/applications` - Crear solicitud de crédito
- **GET** `/api/applications` - Listar solicitudes
- **GET** `/api/applications/{id}` - Obtener solicitud

## Monitoreo

Render proporciona:
- **Logs en tiempo real**: Dashboard → Logs
- **Métricas**: Dashboard → Metrics
- **Health checks automáticos**
- **Auto-restart** si el servicio falla

## Notas Importantes

### Plan Gratuito
- ⚠️ El servicio se duerme después de 15 minutos de inactividad
- ⚠️ Primera petición después del sleep puede tardar 30-50 segundos
- ⚠️ 750 horas gratis al mes
- ⚠️ Base de datos PostgreSQL gratis: 90 días, luego se elimina

### Optimizaciones
- El Dockerfile usa multi-stage build para reducir tamaño
- Capa de caché de dependencias Maven para builds más rápidos
- Usuario no-root para seguridad
- Lombok + MapStruct configurados para procesamiento de anotaciones

### Seguridad
- JWT para autenticación
- Spring Security configurado
- Variables sensibles no están en el código
- Base de datos con credenciales seguras

## Solución de Problemas

### Error: "/src": not found
Si obtienes el error `failed to compute cache key: "/src": not found`, verifica que tu `render.yaml` tenga configurado el `dockerContext` correctamente:
```yaml
dockerfilePath: ./credit-application-service/Dockerfile
dockerContext: ./credit-application-service
```

### Build Fails
```bash
# Verificar que el build funciona localmente
cd credit-application-service
docker build -t credit-service .
docker run -p 8080:8080 credit-service
```

### Health Check Fails
Verifica que el endpoint `/actuator/health` responde:
```bash
curl http://localhost:8080/actuator/health
```

### Database Connection Issues
1. Verifica las variables de entorno de conexión
2. Comprueba que la base de datos está activa
3. Revisa los logs: `Dashboard → Logs`
4. Para H2 en memoria: `SPRING_PROFILES_ACTIVE=dev`

### JWT Token Issues
1. Asegúrate que `JWT_SECRET` tiene al menos 32 caracteres
2. Verifica que `JWT_EXPIRATION` está configurado
3. Comprueba los logs para errores de autenticación

### Risk Central Integration Issues
1. Verifica que `RISK_CENTRAL_URL` está configurado correctamente
2. Asegúrate que el servicio Risk Central está desplegado y funcionando
3. Revisa los logs para errores de conexión HTTP

## Actualizar el Servicio

El servicio se actualiza automáticamente cuando:
1. Haces push a la rama configurada
2. Render detecta cambios
3. Reconstruye la imagen Docker
4. Despliega la nueva versión

Para forzar un re-deploy:
- Dashboard → Manual Deploy → Deploy latest commit

## Migración de Base de Datos

Las migraciones de Flyway se ejecutan automáticamente al iniciar la aplicación.

### Archivos de migración
```
src/main/resources/db/migration/
├── V1__create_users_table.sql
├── V2__create_applications_table.sql
└── V3__add_indexes.sql
```

### Verificar migraciones
```sql
SELECT * FROM flyway_schema_history;
```

## Recursos Adicionales

- [Render Docs - Docker](https://render.com/docs/docker)
- [Render Docs - Spring Boot](https://render.com/docs/deploy-spring-boot)
- [Render Docs - PostgreSQL](https://render.com/docs/databases)
- [Render Docs - Environment Variables](https://render.com/docs/environment-variables)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
