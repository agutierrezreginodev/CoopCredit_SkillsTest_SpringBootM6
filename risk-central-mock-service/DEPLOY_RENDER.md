# Guía de Despliegue en Render

## Requisitos Previos
- Cuenta en [Render](https://render.com) (gratis)
- Repositorio Git en GitHub/GitLab/Bitbucket
- Código subido al repositorio

## Método 1: Despliegue usando render.yaml (Recomendado)

### Paso 1: Subir el código a Git
```bash
git add .
git commit -m "Preparar para despliegue en Render"
git push origin main
```

### Paso 2: Conectar con Render
1. Ve a [Render Dashboard](https://dashboard.render.com)
2. Click en **"New +"** → **"Blueprint"**
3. Conecta tu repositorio de GitHub/GitLab
4. Selecciona el repositorio `CoopCredit-System`
5. Render detectará automáticamente el archivo `render.yaml`
6. Click en **"Apply"**

### Paso 3: Configurar Variables de Entorno (Opcional)
Si necesitas variables adicionales:
- Ve a tu servicio en el Dashboard
- Click en **"Environment"**
- Agrega las variables necesarias

## Método 2: Despliegue Manual con Docker

### Paso 1: Crear Web Service
1. En Render Dashboard, click **"New +"** → **"Web Service"**
2. Conecta tu repositorio
3. Selecciona el repositorio y rama

### Paso 2: Configurar el Servicio
- **Name**: `risk-central-mock-service`
- **Runtime**: Docker
- **Dockerfile Path**: `./Dockerfile`
- **Plan**: Free

### Paso 3: Variables de Entorno
Agrega estas variables:
```
JAVA_OPTS=-Xmx512m -Xms256m
SERVER_PORT=8081
```

### Paso 4: Advanced Settings
- **Health Check Path**: `/actuator/health`
- **Auto-Deploy**: Yes

### Paso 5: Deploy
Click en **"Create Web Service"**

## Verificación del Despliegue

Una vez desplegado, tu servicio estará disponible en:
```
https://risk-central-mock-service.onrender.com
```

### Endpoints de verificación:
- Health check: `https://tu-app.onrender.com/actuator/health`
- Info: `https://tu-app.onrender.com/actuator/info`

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

### Optimizaciones
- El Dockerfile usa multi-stage build para reducir tamaño
- Capa de caché de dependencias Maven para builds más rápidos
- Usuario no-root para seguridad

## Solución de Problemas

### Build Fails
```bash
# Verificar que el build funciona localmente
docker build -t risk-service .
docker run -p 8081:8081 risk-service
```

### Health Check Fails
Verifica que el endpoint `/actuator/health` responde:
```bash
curl http://localhost:8081/actuator/health
```

### Port Issues
Asegúrate que la variable `SERVER_PORT` está configurada correctamente.

## Actualizar el Servicio

El servicio se actualiza automáticamente cuando:
1. Haces push a la rama configurada
2. Render detecta cambios
3. Reconstruye la imagen Docker
4. Despliega la nueva versión

Para forzar un re-deploy:
- Dashboard → Manual Deploy → Deploy latest commit

## Recursos Adicionales

- [Render Docs - Docker](https://render.com/docs/docker)
- [Render Docs - Spring Boot](https://render.com/docs/deploy-spring-boot)
- [Render Docs - Blueprint](https://render.com/docs/blueprint-spec)
