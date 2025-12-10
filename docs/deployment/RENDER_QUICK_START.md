# 🚀 Render Deployment - Quick Start Guide

## ⚡ 5 Minutos para Desplegar

### 1️⃣ Conectar Repositorio (2 min)

```
1. Ve a https://dashboard.render.com
2. Haz clic en "New +" → "Blueprint"
3. Conecta tu repositorio GitHub
4. Selecciona: agutierrezreginodev/CoopCredit_SkillsTest_SpringBootM6
5. Haz clic en "Connect"
```

### 2️⃣ Aplicar Blueprint (1 min)

```
1. Render detectará render.yaml automáticamente
2. Revisa la configuración:
   - coopcredit-postgres (PostgreSQL 18)
   - risk-central-mock-service (Puerto 8081)
   - credit-application-service (Puerto 8080)
3. Haz clic en "Apply"
```

### 3️⃣ Esperar Despliegue (10-15 min)

```
Monitorea en Dashboard:
🟡 Building → 🟡 Deploying → 🟢 Live

Orden de despliegue:
1. PostgreSQL (primero)
2. Risk Central Mock Service
3. Credit Application Service
```

### 4️⃣ Verificar Despliegue (2 min)

```bash
# Health Check - Risk Central
curl https://risk-central-mock-service.onrender.com/actuator/health

# Health Check - Credit Application
curl https://credit-application-service.onrender.com/actuator/health

# Swagger UI
https://credit-application-service.onrender.com/swagger-ui.html
```

---

## 📊 URLs Después del Despliegue

| Servicio | URL |
|----------|-----|
| **Risk Central Mock** | https://risk-central-mock-service.onrender.com |
| **Credit Application API** | https://credit-application-service.onrender.com |
| **Swagger UI** | https://credit-application-service.onrender.com/swagger-ui.html |
| **API Docs** | https://credit-application-service.onrender.com/v3/api-docs |
| **Metrics** | https://credit-application-service.onrender.com/actuator/prometheus |

---

## 🧪 Prueba Rápida

### 1. Registrar Usuario

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

### 2. Login

```bash
curl -X POST https://credit-application-service.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123"
  }'
```

**Copia el `token` de la respuesta**

### 3. Crear Solicitud de Crédito

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

## ⚠️ Problemas Comunes

| Problema | Solución |
|----------|----------|
| **"Service unavailable"** | Espera 30-50 seg (plan free duerme) |
| **"Database connection failed"** | Verifica que PostgreSQL está "Live" |
| **"Build fails"** | Revisa logs en Dashboard → [Servicio] → Logs |
| **"JWT Token error"** | Verifica JWT_SECRET en Environment |
| **"Risk Central fails"** | Verifica RISK_CENTRAL_URL en Environment |

---

## 📋 Checklist Rápido

- [ ] Repositorio conectado a Render
- [ ] Blueprint aplicado
- [ ] Todos los servicios en "Live"
- [ ] Health checks funcionando
- [ ] Swagger UI accesible
- [ ] Login funcionando
- [ ] Solicitud de crédito creada exitosamente

---

## 💡 Tips

✅ **Auto-deploy habilitado** - Cada `git push` a `main` despliega automáticamente
✅ **Zero-downtime** - Las actualizaciones no interrumpen el servicio
✅ **Logs en tiempo real** - Ve los logs en Dashboard → [Servicio] → Logs
✅ **Métricas disponibles** - Accede a `/actuator/prometheus` para Prometheus

---

## 📚 Documentación Completa

Para más detalles, ver: **RENDER_COMPLETE_DEPLOYMENT.md**

---

**¡Tu sistema está listo para producción!** 🎉

