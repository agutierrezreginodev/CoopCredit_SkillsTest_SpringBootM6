# 📸 Render Deployment - Step by Step Visual Guide

## Step 1: Create Render Account

```
1. Open: https://render.com
2. Click "Sign Up"
3. Choose "Sign up with GitHub"
4. Authorize Render to access your GitHub account
5. Complete profile setup
```

**Expected Result:** ✅ Render account created and logged in

---

## Step 2: Connect GitHub Repository

```
Dashboard Home
    │
    └─► Click "New +" button (top right)
        │
        └─► Select "Blueprint"
            │
            └─► Click "Connect Repository"
                │
                └─► Search for your repo:
                    agutierrezreginodev/CoopCredit_SkillsTest_SpringBootM6
                    │
                    └─► Click "Connect"
```

**Expected Result:** ✅ Repository connected to Render

---

## Step 3: Review Blueprint Configuration

```
Render will auto-detect render.yaml and show:

┌─────────────────────────────────────────────────────────┐
│ Blueprint Configuration                                 │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ Services to Deploy:                                     │
│                                                         │
│ ☑ coopcredit-postgres                                  │
│   └─ PostgreSQL 18                                      │
│   └─ Plan: Free                                         │
│   └─ Region: Oregon                                     │
│                                                         │
│ ☑ risk-central-mock-service                            │
│   └─ Docker Web Service                                │
│   └─ Port: 8081                                         │
│   └─ Plan: Free                                         │
│   └─ Region: Oregon                                     │
│                                                         │
│ ☑ credit-application-service                           │
│   └─ Docker Web Service                                │
│   └─ Port: 8080                                         │
│   └─ Plan: Free                                         │
│   └─ Region: Oregon                                     │
│   └─ Depends on: coopcredit-postgres                    │
│   └─ Depends on: risk-central-mock-service             │
│                                                         │
└─────────────────────────────────────────────────────────┘

Review the configuration and verify:
✓ All 3 services are listed
✓ PostgreSQL is first (no dependencies)
✓ Risk Central is second
✓ Credit App is third (depends on both)
✓ All regions are "Oregon"
✓ All plans are "Free"
```

**Expected Result:** ✅ Configuration reviewed and correct

---

## Step 4: Apply Blueprint

```
Click "Apply" button

┌─────────────────────────────────────────────────────────┐
│ Deployment Started                                      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ Render will now:                                        │
│ 1. Create PostgreSQL service                           │
│ 2. Create Risk Central Mock service                    │
│ 3. Create Credit Application service                   │
│ 4. Build Docker images                                 │
│ 5. Start containers                                    │
│ 6. Run health checks                                   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Expected Result:** ✅ Deployment started

---

## Step 5: Monitor Deployment Progress

```
Go to: https://dashboard.render.com

You'll see 3 services with status indicators:

┌─────────────────────────────────────────────────────────┐
│ Services                                                │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ 🟡 coopcredit-postgres                                 │
│    Status: Building                                     │
│    Progress: Creating PostgreSQL instance...           │
│    Time: ~2-3 minutes                                   │
│                                                         │
│ ⏳ risk-central-mock-service                            │
│    Status: Waiting                                      │
│    Progress: Waiting for PostgreSQL...                 │
│    Time: ~5-8 minutes                                   │
│                                                         │
│ ⏳ credit-application-service                           │
│    Status: Waiting                                      │
│    Progress: Waiting for Risk Central...               │
│    Time: ~10-15 minutes                                │
│                                                         │
└─────────────────────────────────────────────────────────┘

Timeline:
T+0min   ├─ Blueprint applied
T+2min   ├─ PostgreSQL: 🟡 Building
T+3min   ├─ PostgreSQL: 🟢 Live ✓
T+4min   ├─ Risk Central: 🟡 Building
T+8min   ├─ Risk Central: 🟢 Live ✓
T+9min   ├─ Credit App: 🟡 Building
T+15min  └─ Credit App: 🟢 Live ✓
```

**Expected Result:** ✅ All services reach "Live" status

---

## Step 6: Verify Services are Live

```
When all services show 🟢 Live:

┌─────────────────────────────────────────────────────────┐
│ Services Status                                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ 🟢 coopcredit-postgres                                 │
│    Status: Live                                         │
│    Type: PostgreSQL                                     │
│    Region: Oregon                                       │
│                                                         │
│ 🟢 risk-central-mock-service                            │
│    Status: Live                                         │
│    URL: https://risk-central-mock-service.onrender.com │
│    Port: 8081                                           │
│                                                         │
│ 🟢 credit-application-service                           │
│    Status: Live                                         │
│    URL: https://credit-application-service.onrender.com│
│    Port: 8080                                           │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Expected Result:** ✅ All services are Live

---

## Step 7: Check Health Endpoints

```
Open your terminal and run:

# Check Risk Central Health
curl https://risk-central-mock-service.onrender.com/actuator/health

Expected Response:
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "livenessState": {"status": "UP"},
    "readinessState": {"status": "UP"}
  }
}

# Check Credit Application Health
curl https://credit-application-service.onrender.com/actuator/health

Expected Response:
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

**Expected Result:** ✅ Both services return "UP" status

---

## Step 8: Access Swagger UI

```
Open in your browser:
https://credit-application-service.onrender.com/swagger-ui.html

You should see:

┌─────────────────────────────────────────────────────────┐
│ Swagger UI - CoopCredit API                             │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ Servers:                                                │
│ └─ https://credit-application-service.onrender.com     │
│                                                         │
│ Available Endpoints:                                    │
│ ├─ Authentication Controller                           │
│ │  ├─ POST /api/auth/register                          │
│ │  └─ POST /api/auth/login                             │
│ │                                                       │
│ ├─ Affiliate Controller                                │
│ │  ├─ POST /api/affiliates                             │
│ │  ├─ GET /api/affiliates                              │
│ │  ├─ GET /api/affiliates/{id}                         │
│ │  └─ PATCH /api/affiliates/{id}/status                │
│ │                                                       │
│ └─ Credit Application Controller                       │
│    ├─ POST /api/applications                           │
│    ├─ GET /api/applications                            │
│    ├─ GET /api/applications/{id}                       │
│    ├─ POST /api/applications/{id}/evaluate             │
│    └─ GET /api/applications/pending                    │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Expected Result:** ✅ Swagger UI loads and shows all endpoints

---

## Step 9: Test Authentication

```
In Swagger UI or Terminal:

1. Register a new user:

POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "test123",
  "documento": "12345678",
  "role": "ROLE_ADMIN"
}

Expected Response (201 Created):
{
  "id": 1,
  "username": "testuser",
  "documento": "12345678",
  "role": "ROLE_ADMIN",
  "mensaje": "Usuario registrado exitosamente"
}

2. Login:

POST /api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "test123"
}

Expected Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "testuser",
  "role": "ROLE_ADMIN",
  "mensaje": "Login exitoso"
}

SAVE THIS TOKEN FOR NEXT STEPS!
```

**Expected Result:** ✅ User registered and logged in successfully

---

## Step 10: Test API Endpoints

```
Using the token from Step 9:

1. Create a Credit Application:

POST /api/applications
Authorization: Bearer {token}
Content-Type: application/json

{
  "afiliadoId": 1,
  "montoSolicitado": 5000000,
  "plazoMeses": 36,
  "tasaPropuesta": 12.5
}

Expected Response (201 Created):
{
  "id": 1,
  "afiliadoId": 1,
  "montoSolicitado": 5000000,
  "plazoMeses": 36,
  "tasaPropuesta": 12.5,
  "estado": "PENDIENTE",
  "fechaCreacion": "2025-12-10T..."
}

2. Evaluate the Application:

POST /api/applications/1/evaluate
Authorization: Bearer {token}

Expected Response (200 OK):
{
  "id": 1,
  "estado": "APROBADO",
  "evaluacion": {
    "scoreRiesgo": 750,
    "nivelRiesgo": "MEDIO",
    "razonDecision": "Solicitud aprobada. Score crediticio: 750"
  }
}
```

**Expected Result:** ✅ Credit application created and evaluated

---

## Step 11: Monitor Logs

```
In Render Dashboard:

1. Go to: https://dashboard.render.com
2. Click on: credit-application-service
3. Click on: Logs tab

You'll see real-time logs:

┌─────────────────────────────────────────────────────────┐
│ Logs                                                    │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ 2025-12-10T08:50:00.000Z INFO  Starting Spring Boot... │
│ 2025-12-10T08:50:01.000Z INFO  Database initialized    │
│ 2025-12-10T08:50:02.000Z INFO  Flyway migrations OK    │
│ 2025-12-10T08:50:03.000Z INFO  Server started on 8080  │
│ 2025-12-10T08:50:04.000Z INFO  Swagger UI enabled      │
│ 2025-12-10T08:50:10.000Z INFO  POST /api/auth/login    │
│ 2025-12-10T08:50:11.000Z INFO  JWT token generated     │
│ 2025-12-10T08:50:12.000Z INFO  POST /api/applications  │
│ 2025-12-10T08:50:13.000Z INFO  Application created     │
│                                                         │
└─────────────────────────────────────────────────────────┘

Look for:
✓ "Started Spring Boot Application"
✓ "Flyway migrations applied"
✓ "Server started on port 8080"
✓ No ERROR messages
```

**Expected Result:** ✅ Logs show successful startup and requests

---

## Step 12: Verify Database Migrations

```
In Render Dashboard:

1. Click on: coopcredit-postgres
2. Click on: Logs tab

You'll see PostgreSQL logs:

┌─────────────────────────────────────────────────────────┐
│ PostgreSQL Logs                                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ 2025-12-10T08:50:00.000Z LOG  database system is ready │
│ 2025-12-10T08:50:01.000Z LOG  coopcredit_db created    │
│ 2025-12-10T08:50:02.000Z LOG  user coopcredit created  │
│                                                         │
└─────────────────────────────────────────────────────────┘

Verify:
✓ Database "coopcredit_db" created
✓ User "coopcredit" created
✓ No connection errors
```

**Expected Result:** ✅ Database initialized correctly

---

## Step 13: Check Metrics (Optional)

```
Access Prometheus metrics:
https://credit-application-service.onrender.com/actuator/prometheus

You'll see metrics like:

# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Survivor Space"} 1234567

# HELP http_server_requests_seconds HTTP server requests
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{method="POST",status="200",uri="/api/auth/login"} 1

# HELP spring_boot_application_info Application info
# TYPE spring_boot_application_info gauge
spring_boot_application_info{version="1.0.0"} 1
```

**Expected Result:** ✅ Metrics available for monitoring

---

## Step 14: Set Up Auto-Deployment (Optional)

```
Auto-deploy is already enabled in render.yaml!

This means:
1. Every git push to main branch
2. Render automatically:
   ├─ Detects changes
   ├─ Rebuilds Docker images
   ├─ Redeploys services
   └─ Zero-downtime update

To test:
1. Make a code change locally
2. git add .
3. git commit -m "Test auto-deploy"
4. git push origin main
5. Watch Render Dashboard for automatic redeployment
```

**Expected Result:** ✅ Auto-deploy working

---

## Step 15: Celebrate! 🎉

```
Your CoopCredit system is now:

✅ Deployed on Render
✅ Using PostgreSQL 18
✅ Running 2 microservices
✅ Health checks passing
✅ API endpoints working
✅ Database migrations applied
✅ JWT authentication working
✅ Auto-deployment enabled
✅ Monitoring available
✅ Production ready!

Next Steps:
1. Share the URLs with your team
2. Monitor logs regularly
3. Set up alerts (optional)
4. Consider upgrading to Starter plan
5. Configure backups (optional)

URLs to Share:
- API: https://credit-application-service.onrender.com
- Swagger: https://credit-application-service.onrender.com/swagger-ui.html
- Risk Central: https://risk-central-mock-service.onrender.com
```

**Expected Result:** ✅ System fully deployed and operational!

---

## Troubleshooting Quick Reference

| Issue | Solution |
|-------|----------|
| **Service shows "Building" for too long** | Check logs for build errors |
| **Service shows "Deploying" for too long** | Check logs for startup errors |
| **Health check fails** | Verify database is connected |
| **"Service unavailable" error** | Wait 30-50 sec (plan free sleeps) |
| **Database connection error** | Verify PostgreSQL is "Live" |
| **JWT token errors** | Check JWT_SECRET in Environment |
| **Risk Central integration fails** | Verify Risk Central URL in Environment |

---

**Deployment Guide Version:** 1.0
**Last Updated:** 2025-12-10
**Status:** Complete & Ready

