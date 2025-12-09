# 📊 Diagramas del Sistema CoopCredit

Este documento contiene todos los diagramas arquitectónicos y de diseño del sistema CoopCredit en formato Mermaid.

---

## 🏗️ 1. Arquitectura Hexagonal

```mermaid
graph TB
    subgraph "🌐 Infrastructure Layer - Adaptadores Externos"
        REST[REST Controllers<br/>AffiliateController<br/>CreditApplicationController<br/>AuthController]
        JPA[JPA Repositories<br/>AffiliateJpaRepository<br/>CreditApplicationJpaRepository]
        HTTP[HTTP Client<br/>RiskCentralRestAdapter]
        SEC[Security<br/>JwtAuthenticationFilter<br/>SecurityConfig]
    end

    subgraph "⚙️ Application Layer - Casos de Uso"
        AFF_SERVICE[AffiliateService<br/>registerAffiliate<br/>updateAffiliate]
        CREDIT_SERVICE[CreditApplicationService<br/>createApplication<br/>evaluateApplication]
        AUTH_SERVICE[AuthService<br/>register<br/>login]
    end

    subgraph "💎 Domain Layer - Lógica de Negocio Pura"
        subgraph "Entidades"
            AFFILIATE[Affiliate<br/>validate<br/>isActive<br/>cumpleAntiguedad]
            APPLICATION[CreditApplication<br/>validate<br/>aprobar<br/>rechazar]
            RISK[RiskEvaluation<br/>cumpleScoreMinimo<br/>isRiskAcceptable]
        end
        
        subgraph "Puertos IN - Use Cases"
            AFF_UC[AffiliateUseCase]
            CREDIT_UC[CreditApplicationUseCase]
            AUTH_UC[AuthUseCase]
        end
        
        subgraph "Puertos OUT - Dependencies"
            AFF_REPO[AffiliateRepositoryPort]
            CREDIT_REPO[CreditApplicationRepositoryPort]
            RISK_PORT[RiskCentralPort]
            USER_REPO[UserRepositoryPort]
        end
    end

    subgraph "🗄️ Database"
        DB[(MySQL<br/>affiliates<br/>credit_applications<br/>risk_evaluations<br/>users)]
    end

    subgraph "🔗 External Service"
        RISK_SERVICE[Risk Central Mock<br/>POST /risk-evaluation]
    end

    %% Flujo de entrada
    REST -->|DTO| AFF_SERVICE
    REST -->|DTO| CREDIT_SERVICE
    REST -->|DTO| AUTH_SERVICE
    SEC -->|Filter| REST

    %% Implementación de Use Cases
    AFF_SERVICE -.implements.-> AFF_UC
    CREDIT_SERVICE -.implements.-> CREDIT_UC
    AUTH_SERVICE -.implements.-> AUTH_UC

    %% Uso del Dominio
    AFF_SERVICE -->|usa| AFFILIATE
    CREDIT_SERVICE -->|usa| APPLICATION
    CREDIT_SERVICE -->|usa| RISK

    %% Dependencias de Puertos
    AFF_SERVICE -->|depende| AFF_REPO
    CREDIT_SERVICE -->|depende| CREDIT_REPO
    CREDIT_SERVICE -->|depende| AFF_REPO
    CREDIT_SERVICE -->|depende| RISK_PORT
    AUTH_SERVICE -->|depende| USER_REPO

    %% Implementación de Adaptadores
    JPA -.implements.-> AFF_REPO
    JPA -.implements.-> CREDIT_REPO
    JPA -.implements.-> USER_REPO
    HTTP -.implements.-> RISK_PORT

    %% Conexiones externas
    JPA -->|SQL| DB
    HTTP -->|REST API| RISK_SERVICE

    style REST fill:#e1f5ff
    style JPA fill:#fff4e1
    style HTTP fill:#ffe1f0
    style SEC fill:#e1ffe1
    style AFFILIATE fill:#ffd6d6
    style APPLICATION fill:#ffd6d6
    style RISK fill:#ffd6d6
    style DB fill:#d6f5ff
    style RISK_SERVICE fill:#f0d6ff
```

---

## 🎯 2. Diagrama de Casos de Uso

```mermaid
graph LR
    subgraph "👥 Actores"
        AFILIADO[👤 Afiliado]
        ANALISTA[👨‍💼 Analista]
        ADMIN[👨‍💻 Admin]
    end

    subgraph "🔐 Autenticación"
        UC1[Registrar Usuario]
        UC2[Iniciar Sesión]
    end

    subgraph "👥 Gestión de Afiliados"
        UC3[Crear Afiliado]
        UC4[Consultar Afiliado]
        UC5[Actualizar Afiliado]
        UC6[Cambiar Estado]
        UC7[Listar Afiliados]
    end

    subgraph "💰 Gestión de Solicitudes"
        UC8[Crear Solicitud]
        UC9[Evaluar Solicitud]
        UC10[Consultar Solicitud]
        UC11[Listar Pendientes]
        UC12[Listar por Afiliado]
        UC13[Listar por Estado]
    end

    subgraph "🎯 Evaluación de Riesgo"
        UC14[Consultar Central de Riesgo]
        UC15[Aplicar Políticas Internas]
        UC16[Decidir Aprobación]
    end

    %% Autenticación
    AFILIADO --> UC1
    ANALISTA --> UC1
    ADMIN --> UC1
    AFILIADO --> UC2
    ANALISTA --> UC2
    ADMIN --> UC2

    %% Afiliados - Solo ADMIN y ANALISTA pueden crear/editar
    ADMIN --> UC3
    ANALISTA --> UC3
    ADMIN --> UC5
    ANALISTA --> UC5
    ADMIN --> UC6
    ANALISTA --> UC6
    
    %% Todos pueden consultar afiliados
    AFILIADO --> UC4
    ANALISTA --> UC4
    ADMIN --> UC4
    AFILIADO --> UC7
    ANALISTA --> UC7
    ADMIN --> UC7

    %% Solicitudes - AFILIADO puede crear
    AFILIADO --> UC8
    ADMIN --> UC8
    
    %% Evaluar - Solo ANALISTA y ADMIN
    ANALISTA --> UC9
    ADMIN --> UC9
    
    %% Listar pendientes - Solo ANALISTA y ADMIN
    ANALISTA --> UC11
    ADMIN --> UC11

    %% Consultar solicitudes
    AFILIADO --> UC10
    ANALISTA --> UC10
    ADMIN --> UC10
    AFILIADO --> UC12
    ANALISTA --> UC12
    ADMIN --> UC12
    ANALISTA --> UC13
    ADMIN --> UC13

    %% Flujo interno de evaluación
    UC9 -->|include| UC14
    UC9 -->|include| UC15
    UC9 -->|include| UC16

    style AFILIADO fill:#a8d5ff
    style ANALISTA fill:#ffd6a8
    style ADMIN fill:#ffa8a8
    style UC9 fill:#ffcccc
    style UC14 fill:#ccffcc
    style UC15 fill:#ccffcc
    style UC16 fill:#ccffcc
```

---

## 🔄 3. Diagrama de Secuencia - Evaluación de Solicitud

```mermaid
sequenceDiagram
    actor Analista
    participant Controller as CreditApplicationController
    participant Service as CreditApplicationService
    participant AffRepo as AffiliateRepository
    participant AppRepo as ApplicationRepository
    participant RiskAdapter as RiskCentralAdapter
    participant RiskMock as Risk Central Mock
    participant DB as Database

    Analista->>Controller: POST /api/applications/{id}/evaluate
    activate Controller
    
    Controller->>Service: evaluateApplication(id)
    activate Service
    
    Service->>AppRepo: findById(id)
    AppRepo->>DB: SELECT * FROM credit_applications
    DB-->>AppRepo: CreditApplication
    AppRepo-->>Service: CreditApplication
    
    Note over Service: Verificar estado PENDIENTE
    
    Service->>AffRepo: findById(afiliadoId)
    AffRepo->>DB: SELECT * FROM affiliates
    DB-->>AffRepo: Affiliate
    AffRepo-->>Service: Affiliate
    
    Note over Service: ✓ Validar afiliado ACTIVO
    Note over Service: ✓ Validar antigüedad ≥ 6 meses
    Note over Service: ✓ Validar monto ≤ 3x salario
    
    Service->>RiskAdapter: evaluateRisk(documento, monto, plazo)
    activate RiskAdapter
    RiskAdapter->>RiskMock: POST /risk-evaluation
    activate RiskMock
    
    Note over RiskMock: Generar seed = hash(documento) % 1000<br/>Score = 300 + (seed % 651)<br/>Clasificar riesgo
    
    RiskMock-->>RiskAdapter: {score, nivelRiesgo, detalle}
    deactivate RiskMock
    RiskAdapter-->>Service: RiskEvaluation
    deactivate RiskAdapter
    
    Note over Service: ✓ Validar score ≥ 500
    Note over Service: ✓ Validar nivel ≠ ALTO
    Note over Service: ✓ Validar ratio cuota/ingreso ≤ 40%
    
    alt Todas las validaciones OK
        Note over Service: application.aprobar()
        Service->>AppRepo: save(application)
        AppRepo->>DB: UPDATE credit_applications<br/>SET estado='APROBADO'
        DB-->>AppRepo: OK
        AppRepo-->>Service: CreditApplication (APROBADO)
    else Alguna validación falla
        Note over Service: application.rechazar(motivo)
        Service->>AppRepo: save(application)
        AppRepo->>DB: UPDATE credit_applications<br/>SET estado='RECHAZADO'
        DB-->>AppRepo: OK
        AppRepo-->>Service: CreditApplication (RECHAZADO)
    end
    
    Service-->>Controller: CreditApplication
    deactivate Service
    
    Controller-->>Analista: 200 OK + CreditApplicationDto
    deactivate Controller
```

---

## 🌐 4. Arquitectura de Microservicios

```mermaid
graph TB
    subgraph "🌍 External Clients"
        WEB[Web Browser]
        MOBILE[Mobile App]
        POSTMAN[Postman/API Client]
    end

    subgraph "🐳 Docker Network - coopcredit-network"
        subgraph "🚀 credit-application-service:8080"
            API[Spring Boot Application<br/>- REST Controllers<br/>- Security JWT<br/>- Business Logic<br/>- JPA/Hibernate]
            ACTUATOR[Actuator<br/>Health Check<br/>Metrics<br/>Prometheus]
        end

        subgraph "🎲 risk-central-mock-service:8081"
            MOCK[Mock Service<br/>- Risk Evaluation<br/>- Deterministic Algorithm<br/>- No Database<br/>- No Security]
        end

        subgraph "🗄️ MySQL Database:3306"
            DB[(MySQL 8.0<br/>coopcredit_db<br/>- affiliates<br/>- credit_applications<br/>- risk_evaluations<br/>- users)]
        end

        subgraph "📊 Monitoring Stack (Optional)"
            PROM[Prometheus<br/>Metrics Collection]
            GRAF[Grafana<br/>Dashboards]
        end
    end

    %% External access
    WEB -->|HTTPS/HTTP| API
    MOBILE -->|HTTPS/HTTP| API
    POSTMAN -->|HTTP| API

    %% Internal communication
    API -->|REST API<br/>POST /risk-evaluation| MOCK
    API -->|JDBC<br/>SQL Queries| DB
    API -->|Expose Metrics| ACTUATOR

    %% Monitoring
    ACTUATOR -->|/actuator/prometheus| PROM
    PROM -->|Query Metrics| GRAF

    %% Health checks
    API -.Health Check.-> DB
    MOCK -.Health Check.-> MOCK

    style API fill:#e1f5ff
    style MOCK fill:#ffe1f5
    style DB fill:#fff4e1
    style ACTUATOR fill:#e1ffe1
    style PROM fill:#ffd6d6
    style GRAF fill:#d6f5ff
```

---

## 🔐 5. Diagrama de Autenticación y Autorización

```mermaid
sequenceDiagram
    actor User
    participant AuthController
    participant AuthService
    participant UserRepo
    participant JwtService
    participant SecurityFilter
    participant PasswordEncoder
    participant DB

    %% Registro
    rect rgb(200, 230, 255)
        Note over User,DB: Flujo de Registro
        User->>AuthController: POST /api/auth/register<br/>{username, password, role}
        AuthController->>AuthService: register(user)
        AuthService->>UserRepo: findByUsername(username)
        UserRepo->>DB: SELECT * FROM users
        DB-->>UserRepo: null (no existe)
        UserRepo-->>AuthService: Optional.empty()
        AuthService->>PasswordEncoder: encode(password)
        PasswordEncoder-->>AuthService: $2a$10$...
        AuthService->>UserRepo: save(user)
        UserRepo->>DB: INSERT INTO users
        DB-->>UserRepo: User
        UserRepo-->>AuthService: User
        AuthService-->>AuthController: UserDto
        AuthController-->>User: 201 Created + UserDto
    end

    %% Login
    rect rgb(200, 255, 230)
        Note over User,DB: Flujo de Login
        User->>AuthController: POST /api/auth/login<br/>{username, password}
        AuthController->>AuthService: login(username, password)
        AuthService->>UserRepo: findByUsername(username)
        UserRepo->>DB: SELECT * FROM users
        DB-->>UserRepo: User
        UserRepo-->>AuthService: User
        AuthService->>PasswordEncoder: matches(password, user.password)
        PasswordEncoder-->>AuthService: true
        AuthService->>JwtService: generateToken(username, role)
        JwtService-->>AuthService: eyJhbGciOiJIUzI1NiIs...
        AuthService-->>AuthController: AuthResponse{token, username, role}
        AuthController-->>User: 200 OK + JWT Token
    end

    %% Request autenticado
    rect rgb(255, 230, 200)
        Note over User,DB: Request con JWT
        User->>SecurityFilter: GET /api/affiliates<br/>Authorization: Bearer token
        SecurityFilter->>JwtService: isTokenValid(token)
        JwtService-->>SecurityFilter: true
        SecurityFilter->>JwtService: extractUsername(token)
        JwtService-->>SecurityFilter: username
        SecurityFilter->>UserRepo: findByUsername(username)
        UserRepo-->>SecurityFilter: UserDetails
        Note over SecurityFilter: Crear Authentication<br/>con roles/authorities
        SecurityFilter->>AuthController: Request autenticado
        Note over AuthController: Verificar @PreAuthorize<br/>hasAuthority("ROLE_X")
        AuthController-->>User: 200 OK + Data
    end
```

---

## 📊 6. Modelo de Datos (ER Diagram)

```mermaid
erDiagram
    AFFILIATES ||--o{ CREDIT_APPLICATIONS : "solicita"
    CREDIT_APPLICATIONS ||--o| RISK_EVALUATIONS : "tiene"
    USERS ||--o| AFFILIATES : "puede_ser"

    AFFILIATES {
        BIGINT id PK
        VARCHAR documento UK "NOT NULL"
        VARCHAR nombre "NOT NULL"
        DECIMAL salario "NOT NULL, > 0"
        DATE fecha_afiliacion "NOT NULL"
        VARCHAR estado "ACTIVO/INACTIVO"
    }

    CREDIT_APPLICATIONS {
        BIGINT id PK
        BIGINT afiliado_id FK "NOT NULL"
        DECIMAL monto_solicitado "NOT NULL, > 0"
        INT plazo_meses "NOT NULL, 1-120"
        DECIMAL tasa_propuesta "NOT NULL, 0-100"
        DATETIME fecha_solicitud "NOT NULL"
        VARCHAR estado "PENDIENTE/APROBADO/RECHAZADO"
        TEXT motivo_rechazo "nullable"
        BIGINT evaluacion_riesgo_id FK "nullable"
    }

    RISK_EVALUATIONS {
        BIGINT id PK
        VARCHAR documento "NOT NULL"
        INT score "NOT NULL, 300-950"
        VARCHAR nivel_riesgo "BAJO/MEDIO/ALTO"
        TEXT detalle
        DATETIME fecha_evaluacion "NOT NULL"
    }

    USERS {
        BIGINT id PK
        VARCHAR username UK "NOT NULL"
        VARCHAR password "NOT NULL, BCrypt"
        VARCHAR role "ROLE_AFILIADO/ROLE_ANALISTA/ROLE_ADMIN"
        VARCHAR documento FK "nullable"
    }
```

---

## 🔄 7. Flujo Completo de Evaluación de Crédito

```mermaid
flowchart TD
    START([👤 Afiliado solicita crédito]) --> CREATE[Crear Solicitud<br/>Estado: PENDIENTE]
    CREATE --> WAIT[⏳ Esperar Evaluación]
    
    WAIT --> ANALYST[👨‍💼 Analista inicia evaluación]
    
    ANALYST --> CHECK1{¿Afiliado ACTIVO?}
    CHECK1 -->|NO| REJECT1[❌ RECHAZAR<br/>Motivo: Afiliado inactivo]
    CHECK1 -->|SÍ| CHECK2{¿Antigüedad ≥ 6 meses?}
    
    CHECK2 -->|NO| REJECT2[❌ RECHAZAR<br/>Motivo: Antigüedad insuficiente]
    CHECK2 -->|SÍ| CHECK3{¿Monto ≤ 3x salario?}
    
    CHECK3 -->|NO| REJECT3[❌ RECHAZAR<br/>Motivo: Monto excede máximo]
    CHECK3 -->|SÍ| CALL_RISK[🌐 Consultar Central de Riesgo]
    
    CALL_RISK --> RISK_CALC[📊 Calcular Score<br/>seed = hash(doc) % 1000<br/>score = 300 + seed % 651]
    
    RISK_CALC --> CLASSIFY{Clasificar Riesgo}
    CLASSIFY -->|300-500| HIGH[🔴 ALTO]
    CLASSIFY -->|501-700| MEDIUM[🟡 MEDIO]
    CLASSIFY -->|701-950| LOW[🟢 BAJO]
    
    HIGH --> CHECK4
    MEDIUM --> CHECK4
    LOW --> CHECK4
    
    CHECK4{¿Score ≥ 500?} -->|NO| REJECT4[❌ RECHAZAR<br/>Motivo: Score bajo]
    CHECK4 -->|SÍ| CHECK5{¿Nivel ≠ ALTO?}
    
    CHECK5 -->|NO| REJECT5[❌ RECHAZAR<br/>Motivo: Riesgo alto]
    CHECK5 -->|SÍ| CALC_RATIO[📈 Calcular Ratio Cuota/Ingreso]
    
    CALC_RATIO --> CHECK6{¿Ratio ≤ 40%?}
    CHECK6 -->|NO| REJECT6[❌ RECHAZAR<br/>Motivo: Ratio excede máximo]
    CHECK6 -->|SÍ| APPROVE[✅ APROBAR]
    
    REJECT1 --> SAVE_REJECT[💾 Guardar en BD]
    REJECT2 --> SAVE_REJECT
    REJECT3 --> SAVE_REJECT
    REJECT4 --> SAVE_REJECT
    REJECT5 --> SAVE_REJECT
    REJECT6 --> SAVE_REJECT
    
    APPROVE --> SAVE_APPROVE[💾 Guardar en BD]
    
    SAVE_REJECT --> NOTIFY_REJECT[📧 Notificar Rechazo]
    SAVE_APPROVE --> NOTIFY_APPROVE[📧 Notificar Aprobación]
    
    NOTIFY_REJECT --> END([🏁 FIN])
    NOTIFY_APPROVE --> END

    style START fill:#a8d5ff
    style APPROVE fill:#a8ffa8
    style REJECT1 fill:#ffa8a8
    style REJECT2 fill:#ffa8a8
    style REJECT3 fill:#ffa8a8
    style REJECT4 fill:#ffa8a8
    style REJECT5 fill:#ffa8a8
    style REJECT6 fill:#ffa8a8
    style CALL_RISK fill:#ffffa8
    style END fill:#d5a8ff
```

---

## 🔧 8. Diagrama de Deployment

```mermaid
graph TB
    subgraph "☁️ Production Environment"
        subgraph "🐳 Docker Host"
            subgraph "Container: credit-app"
                APP[Spring Boot App<br/>Port: 8080<br/>Memory: 512MB<br/>CPU: 1 core]
            end
            
            subgraph "Container: risk-mock"
                MOCK[Risk Mock Service<br/>Port: 8081<br/>Memory: 256MB<br/>CPU: 0.5 core]
            end
            
            subgraph "Container: mysql"
                DB[(MySQL 8.0<br/>Port: 3306<br/>Volume: mysql_data<br/>Memory: 1GB)]
            end
            
            subgraph "Container: prometheus (optional)"
                PROM[Prometheus<br/>Port: 9090<br/>Scrape: 30s]
            end
            
            subgraph "Container: grafana (optional)"
                GRAF[Grafana<br/>Port: 3000<br/>Dashboards]
            end
        end
        
        subgraph "🌐 Network: coopcredit-network"
            BRIDGE[Bridge Network<br/>172.18.0.0/16]
        end
        
        subgraph "💾 Volumes"
            VOL1[mysql_data<br/>Persistent]
            VOL2[prometheus_data<br/>Persistent]
            VOL3[grafana_data<br/>Persistent]
        end
    end

    subgraph "🌍 External"
        CLIENT[Clients<br/>Web/Mobile/API]
        LB[Load Balancer<br/>nginx/HAProxy]
    end

    CLIENT -->|HTTP/HTTPS| LB
    LB -->|:8080| APP
    
    APP -->|:3306| DB
    APP -->|:8081| MOCK
    APP -->|Metrics| PROM
    
    PROM -->|Query| GRAF
    
    APP -.-> BRIDGE
    MOCK -.-> BRIDGE
    DB -.-> BRIDGE
    
    DB -.-> VOL1
    PROM -.-> VOL2
    GRAF -.-> VOL3

    style APP fill:#e1f5ff
    style MOCK fill:#ffe1f5
    style DB fill:#fff4e1
    style PROM fill:#ffd6d6
    style GRAF fill:#d6f5ff
    style CLIENT fill:#a8d5ff
```

---

## 📝 Notas sobre los Diagramas

### Cómo visualizar
Estos diagramas están en formato Mermaid y se pueden visualizar en:
- GitHub (automático)
- GitLab (automático)
- VS Code (extensión Mermaid Preview)
- [Mermaid Live Editor](https://mermaid.live/)

### Convenciones
- 🔵 **Azul**: Capa de infraestructura / Adaptadores de entrada
- 🟡 **Amarillo**: Capa de aplicación / Casos de uso
- 🔴 **Rojo**: Capa de dominio / Lógica de negocio
- 🟢 **Verde**: Servicios externos / Base de datos
- 🟣 **Morado**: Servicios mock / Utilidades

### Arquitectura Destacada
1. **Hexagonal**: Dominio completamente independiente
2. **Puertos y Adaptadores**: Interfaces bien definidas
3. **Microservicios**: Separación clara de responsabilidades
4. **Seguridad**: JWT con roles granulares
5. **Observabilidad**: Actuator + Prometheus + Grafana
