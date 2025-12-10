# 🧪 Guía de Ejecución de Tests - CoopCredit

## 📋 Descripción General

Este proyecto cuenta con una suite completa de tests automatizados usando **JUnit 5**, **Mockito** y **Spring Boot Test**. Total: **~95+ tests** cubriendo servicios, controllers e integraciones.

## 🗂️ Tests Disponibles

### Tests Unitarios (Services)
- **`CreditApplicationServiceTest.java`** - 20 tests de lógica de solicitudes de crédito
- **`AffiliateServiceTest.java`** - 23 tests de gestión de afiliados
- **`AuthServiceTest.java`** - 20 tests de autenticación y autorización

### Tests de Integración (Controllers)
- **`AffiliateControllerIntegrationTest.java`** - 20 tests de API REST de afiliados

### Tests End-to-End
- **`CreditApplicationIntegrationTest.java`** - 7 tests de integración completa con DB
- **`CreditApplicationE2ETest.java`** - 9 tests end-to-end con TestContainers

---

## 🚀 Comandos de Ejecución

### 1️⃣ Ejecutar TODOS los Tests

```powershell
cd C:\Users\HP1\Desktop\CoopCredit\CoopCredit_SkillsTest_SpringBootM6\credit-application-service
.\mvnw.cmd test
```

**Resultado esperado:**
```
[INFO] Tests run: 99, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

### 2️⃣ Ejecutar Tests de un Archivo Específico

#### Tests de Servicios

```powershell
# CreditApplicationService (lógica de crédito)
.\mvnw.cmd test -Dtest=CreditApplicationServiceTest

# AffiliateService (gestión de afiliados)
.\mvnw.cmd test -Dtest=AffiliateServiceTest

# AuthService (autenticación)
.\mvnw.cmd test -Dtest=AuthServiceTest
```

#### Tests de Controllers

```powershell
# Controller de Afiliados
.\mvnw.cmd test -Dtest=AffiliateControllerIntegrationTest
```

#### Tests End-to-End

```powershell
# Integración con Base de Datos
.\mvnw.cmd test -Dtest=CreditApplicationIntegrationTest

# End-to-End con TestContainers
.\mvnw.cmd test -Dtest=CreditApplicationE2ETest
```

---

### 3️⃣ Ejecutar un Test Individual

```powershell
# Formato: -Dtest=ClaseTest#nombreDelMetodo

# Ejemplo: Test específico de creación de solicitud
.\mvnw.cmd test -Dtest=CreditApplicationServiceTest#shouldCreateApplicationSuccessfully

# Ejemplo: Test de validación de salario negativo
.\mvnw.cmd test -Dtest=AffiliateServiceTest#shouldThrowExceptionWhenSalaryIsNegative
```

---

### 4️⃣ Ejecutar Tests por Patrón

```powershell
# Todos los tests de servicios (que terminen en ServiceTest)
.\mvnw.cmd test -Dtest=*ServiceTest

# Todos los tests de integración (que contengan Integration)
.\mvnw.cmd test -Dtest=*Integration*

# Todos los tests E2E
.\mvnw.cmd test -Dtest=*E2E*
```

---

### 5️⃣ Ejecutar con Reportes de Cobertura

```powershell
# Generar reporte de cobertura con JaCoCo
.\mvnw.cmd clean test jacoco:report

# Ver el reporte (abre en navegador)
start target/site/jacoco/index.html
```

**📊 El reporte mostrará:**
- Cobertura de líneas por clase
- Cobertura de ramas
- Clases sin cubrir
- Métodos sin testear

---

### 6️⃣ Modo Verbose (Debug)

```powershell
# Ver logs detallados de la ejecución
.\mvnw.cmd test -X

# Ver logs solo de tests
.\mvnw.cmd test -Dtest=CreditApplicationServiceTest -X
```

---

### 7️⃣ Ejecutar sin Compilar (más rápido)

```powershell
# Solo ejecutar tests sin recompilar
.\mvnw.cmd surefire:test
```

---

### 8️⃣ Tests en Paralelo (más rápido para suites grandes)

```powershell
# Ejecutar tests en paralelo (4 threads)
.\mvnw.cmd test -Djunit.jupiter.execution.parallel.enabled=true -Djunit.jupiter.execution.parallel.config.strategy=fixed -Djunit.jupiter.execution.parallel.config.fixed.parallelism=4
```

---

## 🔍 Ejemplos Prácticos

### Escenario 1: Validar una nueva funcionalidad

```powershell
# 1. Ejecutar solo los tests del servicio modificado
.\mvnw.cmd test -Dtest=CreditApplicationServiceTest

# 2. Si pasan, ejecutar todos los tests
.\mvnw.cmd test

# 3. Generar reporte de cobertura
.\mvnw.cmd clean test jacoco:report
start target/site/jacoco/index.html
```

### Escenario 2: Debugging de un test fallido

```powershell
# 1. Ejecutar solo el test fallido con verbose
.\mvnw.cmd test -Dtest=AffiliateServiceTest#shouldUpdateAffiliateSuccessfully -X

# 2. Ver logs detallados
# Los logs mostrarán stack traces completos
```

### Escenario 3: Pre-commit (antes de hacer commit)

```powershell
# Ejecutar todos los tests rápidamente
.\mvnw.cmd test -Dmaven.test.failure.ignore=false
```

---

## 📊 Salida Típica de los Tests

### ✅ Tests Exitosos

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.coopcredit.credit_application_service.application.services.CreditApplicationServiceTest
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.341 s
[INFO] Running com.coopcredit.credit_application_service.application.services.AffiliateServiceTest
[INFO] Tests run: 23, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.892 s
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 99, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

### ❌ Test Fallido

```
[ERROR] Failures:
[ERROR]   CreditApplicationServiceTest.shouldRejectWhenScoreLow:145
    Expected: ApplicationStatus.RECHAZADO
    but was: ApplicationStatus.APROBADO

[INFO] Tests run: 99, Failures: 1, Errors: 0, Skipped: 0

[INFO] BUILD FAILURE
```

---

## 🛠️ Requisitos Previos

### ✅ Verificar Instalación

```powershell
# Java 17 o superior
java -version

# Maven (incluido en el proyecto)
.\mvnw.cmd --version
```

### ✅ Variables de Entorno (Opcional)

```powershell
# Configurar memoria para tests (si son grandes)
$env:MAVEN_OPTS="-Xmx2048m"
```

---

## 🐳 Tests con Docker (E2E)

Los tests E2E usan **TestContainers** para crear una base de datos PostgreSQL temporal.

### Requisitos:
- **Docker Desktop** debe estar ejecutándose

### Ejecutar:

```powershell
# Verificar que Docker está corriendo
docker --version

# Ejecutar tests E2E (levantará PostgreSQL automáticamente)
.\mvnw.cmd test -Dtest=CreditApplicationE2ETest
```

**Nota:** TestContainers descargará la imagen `postgres:18` la primera vez (puede tardar unos minutos).

---

## 📈 Cobertura de Tests

### Estructura de Cobertura Esperada:

| Módulo | Tests | Cobertura |
|--------|-------|-----------|
| **CreditApplicationService** | 20 | 95%+ |
| **AffiliateService** | 23 | 95%+ |
| **AuthService** | 20 | 95%+ |
| **AffiliateController** | 20 | 90%+ |
| **Integration Tests** | 7 | 85%+ |
| **E2E Tests** | 9 | 80%+ |
| **Total** | **99** | **~90%** |

---

## 🎯 Mejores Prácticas

### ✅ Antes de Hacer Commit

```powershell
# 1. Ejecutar todos los tests
.\mvnw.cmd test

# 2. Verificar cobertura
.\mvnw.cmd jacoco:report
```

### ✅ Durante Desarrollo

```powershell
# Ejecutar solo los tests relacionados con tu cambio
.\mvnw.cmd test -Dtest=CreditApplicationServiceTest
```

### ✅ En CI/CD

```powershell
# Ejecutar con reportes para integración continua
.\mvnw.cmd clean verify jacoco:report
```

---

## 🚨 Troubleshooting

### Problema 1: Tests Fallan por Timeout

**Solución:**
```powershell
# Aumentar timeout de tests
.\mvnw.cmd test -Dsurefire.timeout=300
```

### Problema 2: TestContainers No Puede Levantar Docker

**Error:**
```
Could not find a valid Docker environment
```

**Solución:**
1. Verificar que Docker Desktop está corriendo
2. Ejecutar: `docker ps` para confirmar
3. Reiniciar Docker Desktop si es necesario

### Problema 3: Tests Pasan Localmente pero Fallan en CI

**Solución:**
```powershell
# Limpiar caché de Maven
.\mvnw.cmd clean

# Ejecutar tests desde cero
.\mvnw.cmd clean test
```

### Problema 4: Memoria Insuficiente

**Solución:**
```powershell
# Aumentar heap de JVM
$env:MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
.\mvnw.cmd test
```

---

## 📚 Tipos de Tests en el Proyecto

### 🔹 Tests Unitarios
- **Objetivo:** Testear lógica de negocio aislada
- **Herramientas:** JUnit 5, Mockito
- **Velocidad:** ⚡ Rápidos (< 5 segundos)
- **Ejemplo:** `CreditApplicationServiceTest.java`

### 🔹 Tests de Integración
- **Objetivo:** Testear controllers con Spring Context
- **Herramientas:** MockMvc, @WebMvcTest
- **Velocidad:** 🚀 Medios (5-15 segundos)
- **Ejemplo:** `AffiliateControllerIntegrationTest.java`

### 🔹 Tests E2E
- **Objetivo:** Testear flujo completo con BD real
- **Herramientas:** TestContainers, PostgreSQL
- **Velocidad:** 🐢 Lentos (15-30 segundos)
- **Ejemplo:** `CreditApplicationE2ETest.java`

---

## 🎓 Comandos Útiles Adicionales

```powershell
# Ver solo tests que fallaron en última ejecución
.\mvnw.cmd surefire-report:report
start target/surefire-reports/index.html

# Ejecutar tests saltando compilación
.\mvnw.cmd test-compile surefire:test

# Ejecutar solo tests unitarios (excluir Integration y E2E)
.\mvnw.cmd test -Dtest=*ServiceTest,*ControllerTest

# Ejecutar con perfil específico (si tienes perfiles de Maven)
.\mvnw.cmd test -P test-profile

# Limpiar y ejecutar tests desde cero
.\mvnw.cmd clean test

# Ver estadísticas de tests
.\mvnw.cmd surefire:test -Dsurefire.printSummary=true
```

---

## 📞 Soporte

### Si los tests fallan:

1. **Verificar Java:** `java -version` (debe ser 17+)
2. **Limpiar proyecto:** `.\mvnw.cmd clean`
3. **Ver logs:** `.\mvnw.cmd test -X`
4. **Verificar Docker:** `docker ps` (para tests E2E)
5. **Leer documentación:** `src/test/README_TESTS.md`

---

## ✅ Checklist Pre-Deployment

Antes de desplegar, ejecuta:

```powershell
# 1. Tests unitarios
.\mvnw.cmd test -Dtest=*ServiceTest
# ✅ Todos pasan

# 2. Tests de integración
.\mvnw.cmd test -Dtest=*Integration*
# ✅ Todos pasan

# 3. Tests E2E
.\mvnw.cmd test -Dtest=*E2E*
# ✅ Todos pasan

# 4. Suite completa
.\mvnw.cmd clean test
# ✅ 99 tests pasan

# 5. Reporte de cobertura
.\mvnw.cmd jacoco:report
# ✅ Cobertura > 90%

# 🚀 LISTO PARA DEPLOYMENT
```

---

## 🎉 ¡Éxito!

Si todos los tests pasan:
```
[INFO] Tests run: 99, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**🎊 ¡Tu código está listo para producción!**

---

## 📖 Referencias

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [TestContainers](https://testcontainers.com/)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)

---

**Documentación actualizada:** 2025-12-10  
**Versión del proyecto:** 0.0.1-SNAPSHOT  
**Framework:** Spring Boot 4.0.0 + JUnit 5 + Mockito
