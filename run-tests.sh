#!/bin/bash

# ============================================================================
# CoopCredit System - Script para Ejecutar Tests
# ============================================================================

set -e

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     CoopCredit System - Ejecución de Tests            ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# ============================================================================
# Credit Application Service Tests
# ============================================================================

echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}📦 Credit Application Service - Tests${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

cd credit-application-service

echo "🧪 Ejecutando tests..."
./mvnw clean test

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✅ Credit Application Service - Tests PASSED${NC}"
    
    # Generar reporte de cobertura
    echo ""
    echo "📊 Generando reporte de cobertura..."
    ./mvnw jacoco:report
    
    if [ -f "target/site/jacoco/index.html" ]; then
        echo -e "${GREEN}✅ Reporte de cobertura generado: target/site/jacoco/index.html${NC}"
    fi
else
    echo ""
    echo -e "${RED}❌ Credit Application Service - Tests FAILED${NC}"
    exit 1
fi

cd ..

# ============================================================================
# Risk Central Mock Service Tests
# ============================================================================

echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}📦 Risk Central Mock Service - Tests${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

cd risk-central-mock-service

echo "🧪 Ejecutando tests..."
./mvnw clean test

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✅ Risk Central Mock Service - Tests PASSED${NC}"
    
    # Generar reporte de cobertura
    echo ""
    echo "📊 Generando reporte de cobertura..."
    ./mvnw jacoco:report
    
    if [ -f "target/site/jacoco/index.html" ]; then
        echo -e "${GREEN}✅ Reporte de cobertura generado: target/site/jacoco/index.html${NC}"
    fi
else
    echo ""
    echo -e "${RED}❌ Risk Central Mock Service - Tests FAILED${NC}"
    exit 1
fi

cd ..

# ============================================================================
# Resumen Final
# ============================================================================

echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║          ✅ TODOS LOS TESTS PASARON                     ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}📊 Reportes de Cobertura:${NC}"
echo ""
echo "  • Credit Application Service:"
echo "    credit-application-service/target/site/jacoco/index.html"
echo ""
echo "  • Risk Central Mock Service:"
echo "    risk-central-mock-service/target/site/jacoco/index.html"
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
