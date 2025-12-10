# 🏗️ CoopCredit Deployment Architecture

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         RENDER CLOUD PLATFORM                           │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    LOAD BALANCER / ROUTER                        │  │
│  │              (Render manages automatically)                      │  │
│  └────────┬─────────────────────────────────────────────────────────┘  │
│           │                                                             │
│           ├─────────────────────┬──────────────────────┐               │
│           │                     │                      │               │
│           ▼                     ▼                      ▼               │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐    │
│  │   PostgreSQL 18  │  │  Risk Central    │  │  Credit App      │    │
│  │  (coopcredit-    │  │  Mock Service    │  │  Service         │    │
│  │   postgres)      │  │  (8081)          │  │  (8080)          │    │
│  │                  │  │                  │  │                  │    │
│  │ ┌──────────────┐ │  │ ┌──────────────┐ │  │ ┌──────────────┐ │    │
│  │ │ Database:    │ │  │ │ Spring Boot  │ │  │ │ Spring Boot  │ │    │
│  │ │ coopcredit_  │ │  │ │ 4.0          │ │  │ │ 4.0          │ │    │
│  │ │ db           │ │  │ │              │ │  │ │              │ │    │
│  │ │              │ │  │ │ Endpoints:   │ │  │ │ Endpoints:   │ │    │
│  │ │ User:        │ │  │ │ - /health    │ │  │ │ - /health    │ │    │
│  │ │ coopcredit   │ │  │ │ - /risk-eval │ │  │ │ - /auth/*    │ │    │
│  │ │              │ │  │ │ - /info      │ │  │ │ - /api/*     │ │    │
│  │ │ Port: 5432   │ │  │ │              │ │  │ │ - /swagger   │ │    │
│  │ │              │ │  │ │ Memory:      │ │  │ │              │ │    │
│  │ │ Storage:     │ │  │ │ 512MB        │ │  │ │ Memory:      │ │    │
│  │ │ 1GB (Free)   │ │  │ │              │ │  │ │ 512MB        │ │    │
│  │ │              │ │  │ │ Plan: Free   │ │  │ │              │ │    │
│  │ │ Migrations:  │ │  │ │ Region: OR   │ │  │ │ Plan: Free   │ │    │
│  │ │ Flyway       │ │  │ │              │ │  │ │ Region: OR   │ │    │
│  │ │ (V1, V2, V3) │ │  │ │ Auto-deploy: │ │  │ │              │ │    │
│  │ │              │ │  │ │ Enabled      │ │  │ │ Auto-deploy: │ │    │
│  │ │ Healthcheck: │ │  │ │              │ │  │ │ Enabled      │ │    │
│  │ │ pg_isready   │ │  │ │ Healthcheck: │ │  │ │              │ │    │
│  │ │              │ │  │ │ /actuator/   │ │  │ │ Healthcheck: │ │    │
│  │ │ Plan: Free   │ │  │ │ health       │ │  │ │ /actuator/   │ │    │
│  │ │ (90 days)    │ │  │ │              │ │  │ │ health       │ │    │
│  │ │              │ │  │ │ Depends on:  │ │  │ │              │ │    │
│  │ │ Region: OR   │ │  │ │ None         │ │  │ │ Depends on:  │ │    │
│  │ └──────────────┘ │  │ └──────────────┘ │  │ │ - PostgreSQL │ │    │
│  │                  │  │                  │  │ │ - Risk Cent. │ │    │
│  │ Connections:     │  │ Connections:     │  │ │              │ │    │
│  │ - Credit App     │  │ - Credit App     │  │ │ Connections: │ │    │
│  │   (JDBC)         │  │   (HTTP/REST)    │  │ │ - PostgreSQL │ │    │
│  │                  │  │                  │  │ │   (JDBC)     │ │    │
│  └──────────────────┘  └──────────────────┘  │ │ - Risk Cent. │ │    │
│                                               │ │   (HTTP)     │ │    │
│                                               │ │              │ │    │
│                                               │ │ Features:    │ │    │
│                                               │ │ - JWT Auth   │ │    │
│                                               │ │ - Flyway     │ │    │
│                                               │ │ - Prometheus │ │    │
│                                               │ │ - Swagger UI │ │    │
│                                               │ └──────────────┘ │    │
│                                               │                  │    │
│                                               └──────────────────┘    │
│                                                                       │
└─────────────────────────────────────────────────────────────────────────┘

                              EXTERNAL USERS
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
                    ▼                               ▼
            ┌──────────────────┐          ┌──────────────────┐
            │  Web Browser     │          │  API Client      │
            │  (Swagger UI)    │          │  (Postman, curl) │
            │                  │          │                  │
            │ https://credit-  │          │ https://credit-  │
            │ application-     │          │ application-     │
            │ service.onrender │          │ service.onrender │
            │ .com/swagger-ui  │          │ .com/api/*       │
            └──────────────────┘          └──────────────────┘
```

---

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    USER INTERACTION FLOW                        │
└─────────────────────────────────────────────────────────────────┘

1. AUTHENTICATION
   ┌──────────────┐
   │ User Browser │
   └──────┬───────┘
          │ POST /api/auth/register
          │ or /api/auth/login
          ▼
   ┌──────────────────────────────┐
   │ Credit Application Service   │
   │ (Authentication Controller)  │
   └──────┬───────────────────────┘
          │ Validate credentials
          │ Generate JWT_SECRET
          ▼
   ┌──────────────────────────────┐
   │ PostgreSQL Database          │
   │ (User table)                 │
   └──────┬───────────────────────┘
          │ Return user data
          ▼
   ┌──────────────────────────────┐
   │ Credit Application Service   │
   │ (JWT Token Generation)       │
   └──────┬───────────────────────┘
          │ Return JWT Token
          ▼
   ┌──────────────┐
   │ User Browser │
   │ (Token saved)│
   └──────────────┘

2. CREDIT APPLICATION CREATION
   ┌──────────────┐
   │ User Browser │
   │ (with token) │
   └──────┬───────┘
          │ POST /api/applications
          │ Authorization: Bearer {token}
          ▼
   ┌──────────────────────────────┐
   │ Credit Application Service   │
   │ (Application Controller)     │
   └──────┬───────────────────────┘
          │ Validate token (JWT)
          │ Create application
          ▼
   ┌──────────────────────────────┐
   │ PostgreSQL Database          │
   │ (Applications table)         │
   └──────┬───────────────────────┘
          │ Save application
          ▼
   ┌──────────────┐
   │ User Browser │
   │ (app created)│
   └──────────────┘

3. RISK EVALUATION (Internal)
   ┌──────────────────────────────┐
   │ Credit Application Service   │
   │ (Evaluation Service)         │
   └──────┬───────────────────────┘
          │ POST /risk-evaluation
          │ (Internal HTTP call)
          ▼
   ┌──────────────────────────────┐
   │ Risk Central Mock Service    │
   │ (Risk Evaluation API)        │
   └──────┬───────────────────────┘
          │ Calculate risk score
          │ Determine risk level
          ▼
   ┌──────────────────────────────┐
   │ Risk Central Mock Service    │
   │ (Response)                   │
   └──────┬───────────────────────┘
          │ Return risk data
          ▼
   ┌──────────────────────────────┐
   │ Credit Application Service   │
   │ (Decision Logic)             │
   └──────┬───────────────────────┘
          │ Apply business rules
          │ Approve/Reject
          ▼
   ┌──────────────────────────────┐
   │ PostgreSQL Database          │
   │ (Update application status)  │
   └──────┬───────────────────────┘
          │ Save decision
          ▼
   ┌──────────────────────────────┐
   │ Credit Application Service   │
   │ (Response to user)           │
   └──────┬───────────────────────┘
          │ Return evaluation result
          ▼
   ┌──────────────┐
   │ User Browser │
   │ (result)     │
   └──────────────┘
```

---

## Deployment Sequence Diagram

```
┌────────────────────────────────────────────────────────────────┐
│              RENDER BLUEPRINT DEPLOYMENT SEQUENCE              │
└────────────────────────────────────────────────────────────────┘

Time    Action                          Status
────────────────────────────────────────────────────────────────

T+0min  User clicks "Apply" in Render
        └─► Render reads render.yaml

T+1min  PostgreSQL service starts
        ├─► Building image
        ├─► Starting container
        └─► Waiting for health check
            └─► pg_isready

T+3min  PostgreSQL is LIVE ✓
        └─► Connection string generated
            └─► Passed to dependent services

T+4min  Risk Central Mock Service starts
        ├─► Building Docker image
        │   ├─► Maven build
        │   ├─► Package JAR
        │   └─► Create runtime image
        ├─► Starting container
        └─► Waiting for health check
            └─► /actuator/health

T+8min  Risk Central is LIVE ✓
        └─► URL generated: https://risk-central-...

T+9min  Credit Application Service starts
        ├─► Building Docker image
        │   ├─► Maven build
        │   ├─► Package JAR
        │   └─► Create runtime image
        ├─► Starting container
        ├─► Waiting for database connection
        ├─► Running Flyway migrations
        │   ├─► V1__initial_schema.sql
        │   ├─► V2__add_constraints.sql
        │   └─► V3__initial_data.sql
        └─► Waiting for health check
            └─► /actuator/health

T+15min Credit Application is LIVE ✓
        └─► All services ready
            ├─► PostgreSQL: https://...
            ├─► Risk Central: https://...
            └─► Credit App: https://...

T+15min DEPLOYMENT COMPLETE ✓
        └─► System ready for use
```

---

## Network Communication Diagram

```
┌─────────────────────────────────────────────────────────────┐
│              NETWORK COMMUNICATION PATHS                    │
└─────────────────────────────────────────────────────────────┘

EXTERNAL TRAFFIC (HTTPS)
═════════════════════════

User/Client
    │
    ├─► https://credit-application-service.onrender.com:443
    │   ├─► /api/auth/* (Authentication)
    │   ├─► /api/applications/* (Credit applications)
    │   ├─► /api/affiliates/* (Affiliate management)
    │   ├─► /swagger-ui.html (Documentation)
    │   ├─► /actuator/health (Health check)
    │   └─► /actuator/prometheus (Metrics)
    │
    └─► https://risk-central-mock-service.onrender.com:443
        ├─► /actuator/health (Health check)
        └─► /risk-evaluation (Risk evaluation)


INTERNAL TRAFFIC (HTTP)
═══════════════════════

Credit Application Service (8080)
    │
    ├─► PostgreSQL (5432)
    │   ├─► JDBC connection
    │   ├─► SQL queries
    │   └─► Transaction management
    │
    └─► Risk Central Mock Service (8081)
        ├─► HTTP POST /risk-evaluation
        ├─► Request: {documento, montoSolicitado}
        └─► Response: {score, nivelRiesgo, detalle}


DATABASE CONNECTIONS
════════════════════

Credit Application Service
    │
    └─► PostgreSQL (coopcredit-postgres)
        ├─► Connection Pool (HikariCP)
        ├─► Tables:
        │   ├─► users
        │   ├─► affiliates
        │   ├─► credit_applications
        │   ├─► evaluation_results
        │   └─► flyway_schema_history
        │
        └─► Flyway Migrations
            ├─► V1__initial_schema.sql
            ├─► V2__add_constraints.sql
            └─► V3__initial_data.sql
```

---

## Service Dependencies Diagram

```
┌─────────────────────────────────────────────────────────────┐
│              SERVICE DEPENDENCY GRAPH                       │
└─────────────────────────────────────────────────────────────┘

                    ┌─────────────────┐
                    │  PostgreSQL 18  │
                    │  (coopcredit-   │
                    │   postgres)     │
                    └────────┬────────┘
                             │
                    ┌────────┴────────┐
                    │                 │
                    ▼                 ▼
        ┌──────────────────┐  ┌──────────────────┐
        │ Credit App       │  │ Risk Central     │
        │ Service          │  │ Mock Service     │
        │ (depends on)     │  │ (no dependency)  │
        └────────┬─────────┘  └──────────────────┘
                 │
                 │ (HTTP call)
                 │
                 └──────────────────────────────┐
                                                │
                                                ▼
                                        ┌──────────────────┐
                                        │ Risk Central     │
                                        │ Mock Service     │
                                        │ (called by)      │
                                        └──────────────────┘


DEPLOYMENT ORDER
════════════════

1. PostgreSQL (no dependencies)
   └─► Starts immediately
       └─► Waits for health check

2. Risk Central Mock (no dependencies)
   └─► Starts immediately
       └─► Waits for health check

3. Credit Application (depends on both)
   └─► Waits for PostgreSQL to be healthy
   └─► Waits for Risk Central to be started
       └─► Runs Flyway migrations
           └─► Waits for health check
```

---

## Environment Variables Flow

```
┌─────────────────────────────────────────────────────────────┐
│         ENVIRONMENT VARIABLES CONFIGURATION FLOW            │
└─────────────────────────────────────────────────────────────┘

render.yaml (Blueprint)
    │
    ├─► PostgreSQL Service
    │   ├─► POSTGRES_DB = "coopcredit_db"
    │   ├─► POSTGRES_USER = "coopcredit"
    │   └─► POSTGRES_PASSWORD = [AUTO-GENERATED]
    │       │
    │       └─► Render generates secure password
    │           └─► Stored in Render vault
    │
    ├─► Risk Central Mock Service
    │   ├─► JAVA_OPTS = "-Xmx512m -Xms256m"
    │   └─► SERVER_PORT = "8081"
    │
    └─► Credit Application Service
        ├─► JAVA_OPTS = "-Xmx512m -Xms256m"
        ├─► SERVER_PORT = "8080"
        ├─► SPRING_PROFILES_ACTIVE = "prod"
        ├─► SPRING_DATASOURCE_URL = [FROM PostgreSQL]
        │   └─► jdbc:postgresql://coopcredit-postgres:5432/coopcredit_db
        ├─► SPRING_DATASOURCE_USERNAME = "coopcredit"
        ├─► SPRING_DATASOURCE_PASSWORD = [FROM PostgreSQL]
        ├─► SPRING_JPA_HIBERNATE_DDL_AUTO = "validate"
        ├─► SPRING_FLYWAY_ENABLED = "true"
        ├─► JWT_SECRET = [AUTO-GENERATED]
        │   └─► Render generates secure secret
        │       └─► Stored in Render vault
        └─► RISK_CENTRAL_URL = "https://risk-central-mock-service.onrender.com"
            └─► Auto-populated with service URL


VARIABLE INJECTION
══════════════════

At Runtime:
    │
    ├─► Render injects variables into container
    │   └─► Environment variables available to application
    │
    ├─► Spring Boot reads variables
    │   └─► Configures datasource
    │   └─► Configures JWT
    │   └─► Configures Risk Central URL
    │
    └─► Application starts
        └─► Connects to PostgreSQL
        └─► Runs Flyway migrations
        └─► Ready to serve requests
```

---

## Monitoring & Observability Architecture

```
┌─────────────────────────────────────────────────────────────┐
│         MONITORING & OBSERVABILITY ARCHITECTURE             │
└─────────────────────────────────────────────────────────────┘

Applications
    │
    ├─► Prometheus Metrics Endpoint
    │   └─► /actuator/prometheus
    │       ├─► HTTP requests
    │       ├─► JVM metrics
    │       ├─► Database connections
    │       └─► Custom business metrics
    │
    ├─► Health Check Endpoint
    │   └─► /actuator/health
    │       ├─► Database status
    │       ├─► Disk space
    │       ├─► Liveness state
    │       └─► Readiness state
    │
    ├─► Logs
    │   └─► Sent to Render Logs
    │       ├─► Real-time streaming
    │       ├─► Searchable
    │       └─► Retention: 30 days
    │
    └─► Render Dashboard
        ├─► Service status
        ├─► CPU/Memory usage
        ├─► Deployment history
        └─► Alert configuration


OPTIONAL: EXTERNAL MONITORING
═════════════════════════════

Prometheus (optional)
    │
    └─► Scrapes /actuator/prometheus
        └─► Stores metrics
            └─► Grafana visualizes
                └─► Dashboards & Alerts
```

---

## Security Architecture

```
┌─────────────────────────────────────────────────────────────┐
│              SECURITY ARCHITECTURE                          │
└─────────────────────────────────────────────────────────────┘

EXTERNAL TRAFFIC
════════════════

User/Client
    │
    └─► HTTPS (TLS 1.2+)
        └─► Render manages SSL certificates
            └─► Auto-renewal
                └─► Encrypted communication


AUTHENTICATION
══════════════

Request
    │
    └─► Authorization Header
        └─► Bearer {JWT_TOKEN}
            └─► JWT_SECRET (auto-generated)
                └─► Token validation
                    └─► Role-based access control
                        ├─► ROLE_ADMIN
                        ├─► ROLE_ANALISTA
                        └─► ROLE_AFILIADO


DATABASE SECURITY
═════════════════

Connection
    │
    └─► PostgreSQL User: coopcredit
        └─► Password: [AUTO-GENERATED]
            └─► Stored in Render vault
                └─► Encrypted in transit
                    └─► JDBC connection pool


CONTAINER SECURITY
══════════════════

Docker Image
    │
    ├─► Non-root user (spring:spring)
    │   └─► Prevents privilege escalation
    │
    ├─► Alpine Linux base
    │   └─► Minimal attack surface
    │
    ├─► Multi-stage build
    │   └─► No build tools in runtime image
    │
    └─► Read-only filesystem (where possible)
        └─► Prevents tampering
```

---

**Deployment Architecture Version:** 1.0
**Last Updated:** 2025-12-10
**Status:** Ready for Production

