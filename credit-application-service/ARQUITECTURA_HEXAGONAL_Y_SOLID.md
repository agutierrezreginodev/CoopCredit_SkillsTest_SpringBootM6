# 🏗️ Arquitectura Hexagonal y Principios SOLID - CoopCredit

## 📋 Tabla de Contenidos

1. [Introducción](#introducción)
2. [Arquitectura Hexagonal](#arquitectura-hexagonal)
3. [Principios SOLID](#principios-solid)
4. [Estructura del Proyecto](#estructura-del-proyecto)
5. [Flujo de Datos](#flujo-de-datos)
6. [Beneficios de la Arquitectura](#beneficios-de-la-arquitectura)
7. [Conclusiones](#conclusiones)

---

## 📖 Introducción

Este documento explica cómo se implementa la **Arquitectura Hexagonal (Ports and Adapters)** en el proyecto CoopCredit y cómo se aplican los **principios SOLID** en cada una de sus capas.

### ¿Qué es la Arquitectura Hexagonal?

La Arquitectura Hexagonal, propuesta por Alistair Cockburn, es un patrón arquitectónico que:
- **Separa el dominio del negocio** de las dependencias técnicas
- **Facilita el testing** al poder reemplazar componentes externos
- **Permite flexibilidad** para cambiar implementaciones sin afectar el núcleo
- **Invierte las dependencias**: las capas externas dependen del dominio, no al revés

---

## 🎯 Arquitectura Hexagonal

### Estructura de Capas

```
┌─────────────────────────────────────────────────────────┐
│              CAPA DE INFRAESTRUCTURA                    │
│  (Controllers, Adapters, Config, Security)              │
│                                                           │
│  ┌───────────────────────────────────────────────────┐  │
│  │         CAPA DE APLICACIÓN                        │  │
│  │    (Services - Casos de Uso)                      │  │
│  │                                                     │  │
│  │  ┌─────────────────────────────────────────────┐  │  │
│  │  │      DOMINIO (Core)                         │  │  │
│  │  │  - Model                                    │  │  │
│  │  │  - Ports (In/Out)                           │  │  │
│  │  │  - Enums                                    │  │  │
│  │  │  - Exceptions                               │  │  │
│  │  └─────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 1. 🎯 Capa de DOMINIO (Core)

**Ubicación**: `domain/`

Es el **corazón de la aplicación**, contiene la lógica de negocio pura.

#### 1.1 Model - Entidades de Dominio

**Archivos**:
- `domain/model/CreditApplication.java`
- `domain/model/Affiliate.java`
- `domain/model/RiskEvaluation.java`
- `domain/model/User.java`

**Características**:
- ✅ **Sin dependencias de frameworks** (solo Java y Lombok)
- ✅ Contienen **lógica de negocio**
- ✅ Implementan **validaciones**
- ✅ Métodos de comportamiento del dominio

**Ejemplo - CreditApplication.java**:
```java
public class CreditApplication {
    // Validación de reglas de negocio
    public void validate() {
        if (montoSolicitado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("El monto debe ser mayor a 0");
        }
        // ... más validaciones
    }
    
    // Cálculo de cuota mensual (lógica de negocio)
    public BigDecimal calcularCuotaMensual() {
        // Fórmula de préstamo
        BigDecimal tasaMensual = tasaPropuesta.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        // ... cálculo completo
    }
    
    // Comportamiento del dominio
    public void aprobar() {
        this.estado = ApplicationStatus.APROBADO;
        this.motivoRechazo = null;
    }
}
```

#### 1.2 Ports - Interfaces de Comunicación

Los **Ports** definen contratos sin implementación.

##### 📥 Ports IN (Casos de Uso)

**Ubicación**: `domain/ports/in/`

Definen **qué puede hacer la aplicación** (operaciones disponibles).

**Archivos**:
- `CreditApplicationUseCase.java`
- `AffiliateUseCase.java`
- `AuthUseCase.java`

**Ejemplo**:
```java
public interface CreditApplicationUseCase {
    CreditApplication createApplication(CreditApplication application);
    CreditApplication evaluateApplication(Long applicationId);
    Optional<CreditApplication> getApplicationById(Long id);
    List<CreditApplication> getAllApplications();
}
```

##### 📤 Ports OUT (Dependencias Externas)

**Ubicación**: `domain/ports/out/`

Definen **qué necesita el dominio del exterior** (persistencia, servicios externos).

**Archivos**:
- `CreditApplicationRepositoryPort.java` - Persistencia
- `AffiliateRepositoryPort.java` - Persistencia
- `RiskCentralPort.java` - Servicio externo REST
- `UserRepositoryPort.java` - Persistencia

**Ejemplo**:
```java
public interface CreditApplicationRepositoryPort {
    CreditApplication save(CreditApplication application);
    Optional<CreditApplication> findById(Long id);
    List<CreditApplication> findAll();
}

public interface RiskCentralPort {
    RiskEvaluation evaluateRisk(String documento, Double monto, Integer plazo);
}
```

#### 1.3 Enums y Exceptions

**Enums** (`domain/enums/`):
- `ApplicationStatus` - Estados de solicitud (PENDIENTE, APROBADO, RECHAZADO)
- `AffiliateStatus` - Estados de afiliado (ACTIVO, INACTIVO, SUSPENDIDO)
- `RiskLevel` - Niveles de riesgo (BAJO, MEDIO, ALTO, MUY_ALTO)

**Exceptions** (`domain/exceptions/`):
- `BusinessRuleException` - Violaciones de reglas de negocio
- `ResourceNotFoundException` - Recursos no encontrados

---

### 2. 🎬 Capa de APLICACIÓN (Use Cases)

**Ubicación**: `application/services/`

Implementa los **casos de uso** orquestando el dominio.

**Archivos**:
- `CreditApplicationService.java`
- `AffiliateService.java`
- `AuthService.java`

**Responsabilidades**:
- ✅ Implementar los **Ports IN** (casos de uso)
- ✅ Orquestar la lógica de negocio
- ✅ Coordinar múltiples puertos de salida
- ✅ Gestionar transacciones

**Ejemplo - CreditApplicationService.java**:
```java
public class CreditApplicationService implements CreditApplicationUseCase {
    
    private final CreditApplicationRepositoryPort applicationRepository;
    private final AffiliateRepositoryPort affiliateRepository;
    private final RiskCentralPort riskCentralPort;

    // Constructor con inyección de dependencias
    public CreditApplicationService(
            CreditApplicationRepositoryPort applicationRepository,
            AffiliateRepositoryPort affiliateRepository,
            RiskCentralPort riskCentralPort) {
        this.applicationRepository = applicationRepository;
        this.affiliateRepository = affiliateRepository;
        this.riskCentralPort = riskCentralPort;
    }

    @Override
    @Transactional
    public CreditApplication evaluateApplication(Long applicationId) {
        // 1. Obtener solicitud
        CreditApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        
        // 2. Obtener afiliado
        Affiliate affiliate = affiliateRepository.findById(application.getAfiliadoId())
            .orElseThrow(() -> new ResourceNotFoundException("Afiliado no encontrado"));
        
        // 3. Verificar reglas de negocio
        if (!affiliate.isActive()) {
            application.rechazar("Afiliado no activo");
            return applicationRepository.save(application);
        }
        
        // 4. Consultar servicio externo
        RiskEvaluation riskEvaluation = riskCentralPort.evaluateRisk(
            affiliate.getDocumento(),
            application.getMontoSolicitado().doubleValue(),
            application.getPlazoMeses()
        );
        
        // 5. Evaluar y aprobar/rechazar
        if (riskEvaluation.isRiskAcceptable()) {
            application.aprobar();
        } else {
            application.rechazar("Riesgo crediticio alto");
        }
        
        return applicationRepository.save(application);
    }
}
```

---

### 3. 🔌 Capa de INFRAESTRUCTURA (Adapters)

**Ubicación**: `infrastructure/`

Implementa los **adaptadores** que conectan el dominio con el mundo externo.

#### 3.1 Adapters (Implementaciones de Ports OUT)

##### 📦 Adapters JPA - Persistencia

**Ubicación**: `infrastructure/adapters/jpa/`

Implementan los **Repository Ports** usando JPA/PostgreSQL.

**Archivos**:
- `CreditApplicationRepositoryAdapter.java`
- `AffiliateRepositoryAdapter.java`
- `UserRepositoryAdapter.java`

**Características**:
- ✅ Implementan interfaces de `ports/out`
- ✅ Traducen entre modelo de dominio y entidades JPA
- ✅ Usan Spring Data JPA

**Ejemplo**:
```java
@Component
public class CreditApplicationRepositoryAdapter implements CreditApplicationRepositoryPort {
    
    private final CreditApplicationJpaRepository jpaRepository;
    private final CreditApplicationMapper mapper;

    @Override
    public CreditApplication save(CreditApplication application) {
        // Convertir de dominio a entidad JPA
        var entity = mapper.toEntity(application);
        
        // Guardar con JPA
        var saved = jpaRepository.save(entity);
        
        // Convertir de entidad JPA a dominio
        return mapper.toDomain(saved);
    }
}
```

**Separación Dominio/Infraestructura**:
- **Entidades JPA** (`entities/`): Con anotaciones `@Entity`, `@Table`, etc.
- **Modelo de Dominio** (`domain/model/`): POJOs puros sin anotaciones JPA
- **Mappers**: Traducen entre ambos mundos

##### 🌐 Adapters REST - Servicios Externos

**Ubicación**: `infrastructure/adapters/rest/`

Implementan comunicación con servicios externos.

**Archivo**: `RiskCentralAdapter.java`

**Características**:
- ✅ Implementa `RiskCentralPort`
- ✅ Usa `RestTemplate` para llamadas HTTP
- ✅ Maneja errores de comunicación
- ✅ Mapea DTOs externos a modelo de dominio

**Ejemplo**:
```java
@Component
public class RiskCentralAdapter implements RiskCentralPort {
    
    private final RestTemplate restTemplate;
    
    @Value("${risk.central.url}")
    private String riskCentralUrl;

    @Override
    public RiskEvaluation evaluateRisk(String documento, Double monto, Integer plazo) {
        // Preparar request
        Map<String, Object> request = new HashMap<>();
        request.put("documento", documento);
        request.put("monto", monto);
        request.put("plazo", plazo);
        
        // Llamar servicio externo
        RiskEvaluationResponse response = restTemplate.postForObject(
            riskCentralUrl, request, RiskEvaluationResponse.class
        );
        
        // Mapear a modelo de dominio
        return RiskEvaluation.builder()
            .documento(response.getDocumento())
            .score(response.getScore())
            .nivelRiesgo(RiskLevel.valueOf(response.getNivelRiesgo()))
            .build();
    }
}
```

#### 3.2 Controllers (Adaptadores de Entrada)

**Ubicación**: `infrastructure/controllers/`

Exponen la API REST como punto de entrada.

**Archivos**:
- `CreditApplicationController.java`
- `AffiliateController.java`
- `AuthController.java`

**Características**:
- ✅ Dependen de **Ports IN** (casos de uso)
- ✅ Reciben DTOs de entrada
- ✅ Devuelven DTOs de salida
- ✅ Manejan autenticación/autorización

**Ejemplo**:
```java
@RestController
@RequestMapping("/api/applications")
public class CreditApplicationController {
    
    // Dependencia de Port IN (caso de uso)
    private final CreditApplicationUseCase applicationUseCase;
    private final CreditApplicationDtoMapper mapper;

    @PostMapping
    public ResponseEntity<CreditApplicationDto> createApplication(
            @Valid @RequestBody CreditApplicationDto dto) {
        
        // Convertir DTO a modelo de dominio
        CreditApplication application = mapper.toDomain(dto);
        
        // Ejecutar caso de uso
        CreditApplication saved = applicationUseCase.createApplication(application);
        
        // Convertir dominio a DTO de respuesta
        return ResponseEntity.ok(mapper.toDto(saved));
    }
}
```

#### 3.3 Configuration

**Ubicación**: `infrastructure/config/`

**Archivos**:
- `ApplicationConfig.java` - Configuración de beans de casos de uso
- `SecurityConfig.java` - Configuración de seguridad JWT
- `OpenApiConfig.java` - Configuración de Swagger

**Ejemplo - ApplicationConfig.java**:
```java
@Configuration
public class ApplicationConfig {

    @Bean
    public CreditApplicationUseCase creditApplicationUseCase(
            CreditApplicationRepositoryPort applicationRepository,
            AffiliateRepositoryPort affiliateRepository,
            RiskCentralPort riskCentralPort) {
        return new CreditApplicationService(
            applicationRepository, 
            affiliateRepository, 
            riskCentralPort
        );
    }
}
```

---

## 🎯 Principios SOLID

### 1. 🔤 S - Single Responsibility Principle (SRP)

**"Una clase debe tener una única razón para cambiar"**

#### ✅ Aplicación en el Proyecto:

**CreditApplication (Model)**:
- **Responsabilidad única**: Representar una solicitud de crédito
- Contiene validaciones y cálculos relacionados con solicitudes
- No maneja persistencia, ni comunicación HTTP, ni autenticación

```java
public class CreditApplication {
    // Solo se encarga de:
    // 1. Validar reglas de solicitud
    // 2. Calcular cuota mensual
    // 3. Cambiar estados (aprobar/rechazar)
    
    public void validate() { /* validaciones */ }
    public BigDecimal calcularCuotaMensual() { /* cálculos */ }
    public void aprobar() { /* cambio de estado */ }
}
```

**CreditApplicationRepositoryAdapter**:
- **Responsabilidad única**: Persistencia de solicitudes
- Solo maneja operaciones con base de datos

**RiskCentralAdapter**:
- **Responsabilidad única**: Comunicación con servicio externo de riesgo
- Solo maneja llamadas REST al servicio de evaluación

**CreditApplicationController**:
- **Responsabilidad única**: Exponer endpoints REST
- Solo maneja requests HTTP y validaciones de entrada

---

### 2. 🚪 O - Open/Closed Principle (OCP)

**"Abierto para extensión, cerrado para modificación"**

#### ✅ Aplicación en el Proyecto:

**Ports (Interfaces)** permiten extender sin modificar:

```java
// Interface cerrada para modificación
public interface CreditApplicationRepositoryPort {
    CreditApplication save(CreditApplication application);
    Optional<CreditApplication> findById(Long id);
}

// Abierta para extensión - Múltiples implementaciones
@Component
public class CreditApplicationRepositoryAdapter implements CreditApplicationRepositoryPort {
    // Implementación con PostgreSQL
}

// Se puede crear otra implementación sin modificar el puerto
public class CreditApplicationMongoAdapter implements CreditApplicationRepositoryPort {
    // Implementación con MongoDB
}

// El caso de uso no cambia, solo se configura qué implementación usar
```

**Ejemplo Real**:

Si quisieras cambiar de PostgreSQL a MongoDB:
1. ✅ Crear nuevo adapter: `CreditApplicationMongoAdapter`
2. ✅ Cambiar configuración en `ApplicationConfig`
3. ❌ **NO modificar**: Dominio, Casos de Uso, Controllers

---

### 3. 🔄 L - Liskov Substitution Principle (LSP)

**"Los objetos de una superclase deben poder reemplazarse por objetos de sus subclases sin afectar el programa"**

#### ✅ Aplicación en el Proyecto:

```java
// El caso de uso depende de la abstracción
public class CreditApplicationService {
    private final CreditApplicationRepositoryPort repository;
    
    // Puede recibir CUALQUIER implementación del port
    public CreditApplicationService(CreditApplicationRepositoryPort repository) {
        this.repository = repository;
    }
}

// Todas estas implementaciones son intercambiables
CreditApplicationRepositoryPort repo1 = new CreditApplicationRepositoryAdapter();
CreditApplicationRepositoryPort repo2 = new CreditApplicationInMemoryAdapter();
CreditApplicationRepositoryPort repo3 = new CreditApplicationMongoAdapter();

// El servicio funciona con cualquiera de ellas sin cambios
```

**Testing**:

```java
// En tests se usa implementación en memoria
@Test
public void testEvaluateApplication() {
    CreditApplicationRepositoryPort mockRepo = new InMemoryRepository();
    RiskCentralPort mockRisk = new MockRiskCentralAdapter();
    
    CreditApplicationService service = new CreditApplicationService(
        mockRepo, affiliateRepo, mockRisk
    );
    
    // El servicio funciona igual con mocks
}
```

---

### 4. 🧩 I - Interface Segregation Principle (ISP)

**"Los clientes no deben depender de interfaces que no usan"**

#### ✅ Aplicación en el Proyecto:

**Ports segregados por responsabilidad**:

```java
// ❌ MAL: Interface gigante
public interface MegaRepositoryPort {
    CreditApplication save(CreditApplication app);
    Affiliate saveAffiliate(Affiliate aff);
    User saveUser(User user);
    RiskEvaluation evaluateRisk(String doc);
    void sendEmail(String email);
    // ... 20 métodos más
}

// ✅ BIEN: Interfaces segregadas
public interface CreditApplicationRepositoryPort {
    CreditApplication save(CreditApplication application);
    Optional<CreditApplication> findById(Long id);
    List<CreditApplication> findAll();
}

public interface AffiliateRepositoryPort {
    Affiliate save(Affiliate affiliate);
    Optional<Affiliate> findById(Long id);
}

public interface RiskCentralPort {
    RiskEvaluation evaluateRisk(String documento, Double monto, Integer plazo);
}
```

**Beneficio**: Cada servicio solo depende de lo que necesita:

```java
public class CreditApplicationService {
    // Solo inyecta lo que realmente usa
    private final CreditApplicationRepositoryPort applicationRepository;
    private final AffiliateRepositoryPort affiliateRepository;
    private final RiskCentralPort riskCentralPort;
}
```

---

### 5. 🔀 D - Dependency Inversion Principle (DIP)

**"Depender de abstracciones, no de implementaciones concretas"**

#### ✅ Aplicación en el Proyecto:

**Inversión de Dependencias**:

```
┌──────────────────────────────────────────┐
│  INFRAESTRUCTURA (Capa Externa)          │
│                                           │
│  CreditApplicationRepositoryAdapter  ────┐│
│  RiskCentralAdapter                  ────┤│
│  CreditApplicationController         ────┤│
└──────────────────────────────────────────┘│
                ↓ implementa                 │
                ↓ implementa                 │
                ↓ usa                        │
┌──────────────────────────────────────────┐│
│  DOMINIO (Core)                          ││
│                                           ││
│  CreditApplicationRepositoryPort  ←──────┘│
│  RiskCentralPort                  ←───────┘
│  CreditApplicationUseCase         ←───────┘
└──────────────────────────────────────────┘
        ↑ depende de abstracciones
┌──────────────────────────────────────────┐
│  APLICACIÓN (Casos de Uso)               │
│                                           │
│  CreditApplicationService                │
└──────────────────────────────────────────┘
```

**Código**:

```java
// ✅ CORRECTO: Depende de abstracciones (Ports)
public class CreditApplicationService implements CreditApplicationUseCase {
    
    private final CreditApplicationRepositoryPort applicationRepository; // Interfaz
    private final AffiliateRepositoryPort affiliateRepository;           // Interfaz
    private final RiskCentralPort riskCentralPort;                       // Interfaz
    
    // Constructor recibe abstracciones
    public CreditApplicationService(
            CreditApplicationRepositoryPort applicationRepository,
            AffiliateRepositoryPort affiliateRepository,
            RiskCentralPort riskCentralPort) {
        this.applicationRepository = applicationRepository;
        this.affiliateRepository = affiliateRepository;
        this.riskCentralPort = riskCentralPort;
    }
}

// ❌ INCORRECTO: Depender de implementaciones concretas
public class BadCreditApplicationService {
    private final CreditApplicationRepositoryAdapter adapter; // Clase concreta
    private final RestTemplate restTemplate;                  // Clase concreta
}
```

**Inyección de Dependencias (Spring)**:

```java
@Configuration
public class ApplicationConfig {

    @Bean
    public CreditApplicationUseCase creditApplicationUseCase(
            // Spring inyecta las implementaciones
            CreditApplicationRepositoryPort applicationRepository,
            AffiliateRepositoryPort affiliateRepository,
            RiskCentralPort riskCentralPort) {
        
        return new CreditApplicationService(
            applicationRepository,
            affiliateRepository,
            riskCentralPort
        );
    }
}
```

---

## 📂 Estructura del Proyecto

```
credit-application-service/
└── src/main/java/com/coopcredit/credit_application_service/
    │
    ├── 🎯 domain/                              # CAPA DE DOMINIO
    │   ├── model/                              # Entidades de negocio
    │   │   ├── CreditApplication.java          # - Solicitud de crédito
    │   │   ├── Affiliate.java                  # - Afiliado
    │   │   ├── RiskEvaluation.java             # - Evaluación de riesgo
    │   │   └── User.java                       # - Usuario
    │   │
    │   ├── ports/                              # Contratos de comunicación
    │   │   ├── in/                             # 📥 Puertos de entrada (Casos de Uso)
    │   │   │   ├── CreditApplicationUseCase.java
    │   │   │   ├── AffiliateUseCase.java
    │   │   │   └── AuthUseCase.java
    │   │   │
    │   │   └── out/                            # 📤 Puertos de salida (Dependencias)
    │   │       ├── CreditApplicationRepositoryPort.java
    │   │       ├── AffiliateRepositoryPort.java
    │   │       ├── RiskCentralPort.java
    │   │       └── UserRepositoryPort.java
    │   │
    │   ├── enums/                              # Enumeraciones de negocio
    │   │   ├── ApplicationStatus.java
    │   │   ├── AffiliateStatus.java
    │   │   └── RiskLevel.java
    │   │
    │   └── exceptions/                         # Excepciones de dominio
    │       ├── BusinessRuleException.java
    │       └── ResourceNotFoundException.java
    │
    ├── 🎬 application/                         # CAPA DE APLICACIÓN
    │   └── services/                           # Implementación de casos de uso
    │       ├── CreditApplicationService.java   # - Gestión de solicitudes
    │       ├── AffiliateService.java           # - Gestión de afiliados
    │       └── AuthService.java                # - Autenticación
    │
    └── 🔌 infrastructure/                      # CAPA DE INFRAESTRUCTURA
        │
        ├── adapters/                           # Adaptadores (Puertos OUT)
        │   ├── jpa/                            # 📦 Adaptadores de persistencia
        │   │   ├── CreditApplicationRepositoryAdapter.java
        │   │   ├── AffiliateRepositoryAdapter.java
        │   │   ├── UserRepositoryAdapter.java
        │   │   ├── entities/                   # Entidades JPA
        │   │   │   ├── CreditApplicationEntity.java
        │   │   │   ├── AffiliateEntity.java
        │   │   │   └── UserEntity.java
        │   │   └── repositories/               # Spring Data JPA
        │   │       ├── CreditApplicationJpaRepository.java
        │   │       ├── AffiliateJpaRepository.java
        │   │       └── UserJpaRepository.java
        │   │
        │   └── rest/                           # 🌐 Adaptadores REST externos
        │       └── RiskCentralAdapter.java     # Integración con Risk Central
        │
        ├── controllers/                        # 🎮 Controladores REST (Entrada)
        │   ├── CreditApplicationController.java
        │   ├── AffiliateController.java
        │   └── AuthController.java
        │
        ├── config/                             # ⚙️ Configuración
        │   ├── ApplicationConfig.java          # Beans de casos de uso
        │   ├── SecurityConfig.java             # Seguridad JWT
        │   └── OpenApiConfig.java              # Swagger/OpenAPI
        │
        ├── security/                           # 🔐 Seguridad
        │   ├── JwtUtil.java
        │   └── JwtAuthenticationFilter.java
        │
        ├── web/                                # 🌐 DTOs y Mappers
        │   ├── dto/                            # DTOs de entrada/salida
        │   │   ├── CreditApplicationDto.java
        │   │   ├── AffiliateDto.java
        │   │   └── LoginRequest.java
        │   │
        │   └── mapper/                         # Mappers (Domain ↔ DTO ↔ Entity)
        │       ├── CreditApplicationMapper.java
        │       ├── CreditApplicationDtoMapper.java
        │       └── AffiliateMapper.java
        │
        └── exceptions/                         # 🚨 Manejo global de excepciones
            └── GlobalExceptionHandler.java
```

---

## 🔄 Flujo de Datos

### Ejemplo: Crear y Evaluar una Solicitud de Crédito

```
1. HTTP POST /api/applications/1/evaluate
          ↓
2. CreditApplicationController (Infrastructure)
   - Recibe request HTTP
   - Valida entrada
          ↓
3. creditApplicationUseCase.evaluateApplication(1)
          ↓
4. CreditApplicationService (Application)
   - Orquesta el proceso de evaluación
   - Coordina múltiples componentes
          ↓
5. applicationRepository.findById(1)        [Port OUT - Persistencia]
   → CreditApplicationRepositoryAdapter
   → CreditApplicationJpaRepository
   → PostgreSQL
          ↓
6. affiliateRepository.findById(afiliadoId) [Port OUT - Persistencia]
   → AffiliateRepositoryAdapter
   → AffiliateJpaRepository
   → PostgreSQL
          ↓
7. Domain Logic (CreditApplication + Affiliate)
   - application.validate()
   - affiliate.isActive()
   - affiliate.cumpleAntiguedad()
   - affiliate.calcularMontoMaximoCredito()
          ↓
8. riskCentralPort.evaluateRisk(...)       [Port OUT - REST]
   → RiskCentralAdapter
   → RestTemplate
   → Risk Central Mock Service (HTTP)
          ↓
9. Domain Logic (CreditApplication)
   - application.setEvaluacionRiesgo()
   - application.calcularRatioCuotaIngreso()
   - application.aprobar() / rechazar()
          ↓
10. applicationRepository.save(application) [Port OUT - Persistencia]
    → CreditApplicationRepositoryAdapter
    → CreditApplicationJpaRepository
    → PostgreSQL
          ↓
11. CreditApplicationService
    - Devuelve CreditApplication (Domain)
          ↓
12. CreditApplicationController
    - Mapea Domain → DTO
    - Devuelve ResponseEntity<CreditApplicationDto>
          ↓
13. HTTP 200 OK + JSON Response
```

---

## ✅ Beneficios de la Arquitectura

### 1. 🧪 **Testabilidad**

```java
// Fácil de testear con mocks
@Test
public void testEvaluateApplication() {
    // Mocks de ports
    CreditApplicationRepositoryPort mockRepo = mock(CreditApplicationRepositoryPort.class);
    AffiliateRepositoryPort mockAffiliateRepo = mock(AffiliateRepositoryPort.class);
    RiskCentralPort mockRiskPort = mock(RiskCentralPort.class);
    
    // Crear servicio con mocks (sin necesidad de base de datos o servicios externos)
    CreditApplicationService service = new CreditApplicationService(
        mockRepo, mockAffiliateRepo, mockRiskPort
    );
    
    // Configurar comportamiento
    when(mockRepo.findById(1L)).thenReturn(Optional.of(application));
    when(mockAffiliateRepo.findById(1L)).thenReturn(Optional.of(affiliate));
    when(mockRiskPort.evaluateRisk(any(), any(), any())).thenReturn(riskEvaluation);
    
    // Ejecutar test
    CreditApplication result = service.evaluateApplication(1L);
    
    // Verificar
    assertEquals(ApplicationStatus.APROBADO, result.getEstado());
}
```

### 2. 🔧 **Mantenibilidad**

- **Cambios localizados**: Cada capa tiene responsabilidades claras
- **Fácil de entender**: Separación clara de conceptos
- **Bajo acoplamiento**: Los cambios en infraestructura no afectan el dominio

### 3. 🔄 **Flexibilidad**

**Cambiar implementaciones sin afectar el core**:

```java
// De PostgreSQL a MongoDB
@Bean
public CreditApplicationRepositoryPort applicationRepository() {
    // return new PostgresAdapter();  // Antes
    return new MongoDbAdapter();       // Ahora
}

// De REST a gRPC
@Bean
public RiskCentralPort riskCentralPort() {
    // return new RestRiskAdapter();  // Antes
    return new GrpcRiskAdapter();      // Ahora
}
```

### 4. 🛡️ **Protección del Dominio**

- El dominio no depende de frameworks (Spring, JPA, etc.)
- Las reglas de negocio están protegidas
- Cambios tecnológicos no afectan la lógica de negocio

### 5. 🚀 **Escalabilidad**

- Fácil agregar nuevos casos de uso
- Fácil agregar nuevos adaptadores
- Fácil reemplazar componentes

---

## 📊 Matriz SOLID en el Proyecto

| Principio | Dónde se Aplica | Ejemplo Concreto |
|-----------|----------------|------------------|
| **SRP** | Todas las capas | `CreditApplication` solo maneja lógica de solicitud<br>`CreditApplicationRepositoryAdapter` solo maneja persistencia |
| **OCP** | Ports (Interfaces) | Puedes crear `MongoAdapter` sin modificar `CreditApplicationService` |
| **LSP** | Implementaciones de Ports | Todas las implementaciones de `CreditApplicationRepositoryPort` son intercambiables |
| **ISP** | Segregación de Ports | `CreditApplicationRepositoryPort`, `AffiliateRepositoryPort`, `RiskCentralPort` son interfaces pequeñas y específicas |
| **DIP** | Servicios → Ports | `CreditApplicationService` depende de `RiskCentralPort` (interfaz), no de `RiskCentralAdapter` (implementación) |

---

## 🎓 Conclusiones

### Arquitectura Hexagonal

✅ **Logros Alcanzados**:

1. **Separación clara de responsabilidades**
   - Dominio puro sin dependencias externas
   - Lógica de negocio protegida y centralizada
   - Infraestructura intercambiable

2. **Alta testabilidad**
   - Fácil crear tests unitarios
   - Fácil usar mocks y stubs
   - Tests rápidos sin dependencias externas

3. **Flexibilidad tecnológica**
   - Cambiar base de datos sin afectar el core
   - Cambiar frameworks sin afectar la lógica
   - Agregar nuevos adaptadores fácilmente

4. **Mantenibilidad mejorada**
   - Código más legible y organizado
   - Cambios localizados en capas específicas
   - Evolución del sistema simplificada

### Principios SOLID

✅ **Aplicación Completa**:

| Principio | Estado | Impacto |
|-----------|--------|---------|
| **S** - Single Responsibility | ✅ 100% | Cada clase tiene una única razón para cambiar |
| **O** - Open/Closed | ✅ 100% | Extensible sin modificación mediante Ports |
| **L** - Liskov Substitution | ✅ 100% | Implementaciones intercambiables de Ports |
| **I** - Interface Segregation | ✅ 100% | Interfaces pequeñas y específicas |
| **D** - Dependency Inversion | ✅ 100% | Dependencias hacia abstracciones (Ports) |

### Ventajas del Proyecto

1. **Código limpio y profesional**
2. **Fácil de testear y mantener**
3. **Preparado para crecer y evolucionar**
4. **Tecnología independiente en el core**
5. **Excelente para trabajar en equipo**

---

## 📚 Referencias

- **Arquitectura Hexagonal**: Alistair Cockburn
- **SOLID Principles**: Robert C. Martin (Uncle Bob)
- **Clean Architecture**: Robert C. Martin
- **Domain-Driven Design**: Eric Evans

---

**Autor**: CoopCredit Development Team  
**Última actualización**: 2025-12-10  
**Versión**: 1.0.0
