#!/bin/bash

# ============================================================================
# CoopCredit System - Script de Gestión Unificado
# ============================================================================
# Uso: ./manage.sh [comando]
# Comandos:
#   start         - Inicia todo el sistema (MySQL + Risk Central + API)
#   stop          - Detiene todos los servicios
#   restart       - Reinicia todo el sistema
#   status        - Muestra el estado de los servicios
#   mysql         - Solo inicia MySQL
#   mock          - Solo inicia Risk Central Mock
#   docker        - Usa Docker Compose para todo
#   logs          - Muestra los logs de los servicios
#   clean         - Limpia contenedores y compilaciones
# ============================================================================

set -e

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Directorios
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RISK_DIR="$SCRIPT_DIR/risk-central-mock-service"
API_DIR="$SCRIPT_DIR/credit-application-service"

# ============================================================================
# Funciones de Utilidad
# ============================================================================

print_header() {
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

check_mysql() {
    docker exec coopcredit-mysql mysqladmin ping -h localhost -u root -proot > /dev/null 2>&1
    return $?
}

check_risk_central() {
    curl -s http://localhost:8081/health > /dev/null 2>&1
    return $?
}

check_api() {
    curl -s http://localhost:8080/actuator/health > /dev/null 2>&1
    return $?
}

# ============================================================================
# Funciones de Gestión
# ============================================================================

start_mysql() {
    print_header "📦 Iniciando MySQL"
    
    if check_mysql; then
        print_success "MySQL ya está corriendo"
        return 0
    fi
    
    echo "🐬 Iniciando MySQL con Docker..."
    
    # Detener contenedor existente
    docker stop coopcredit-mysql 2>/dev/null || true
    docker rm coopcredit-mysql 2>/dev/null || true
    
    # Iniciar MySQL
    docker run -d \
      --name coopcredit-mysql \
      -e MYSQL_ROOT_PASSWORD=root \
      -e MYSQL_DATABASE=coopcredit_db \
      -e MYSQL_USER=coopcredit \
      -e MYSQL_PASSWORD=coopcredit \
      -p 3306:3306 \
      mysql:8.0 > /dev/null 2>&1
    
    if [ $? -ne 0 ]; then
        print_error "Error al iniciar MySQL"
        echo ""
        print_info "Intenta ejecutar con sudo o añade tu usuario al grupo docker:"
        echo "  sudo usermod -aG docker \$USER"
        exit 1
    fi
    
    # Esperar a que esté listo
    echo "⏳ Esperando a que MySQL esté listo..."
    COUNTER=0
    until check_mysql || [ $COUNTER -eq 30 ]; do
        printf "."
        sleep 2
        ((COUNTER++))
    done
    echo ""
    
    if [ $COUNTER -eq 30 ]; then
        print_error "MySQL no respondió en 30 segundos"
        exit 1
    fi
    
    print_success "MySQL listo en localhost:3306"
}

start_risk_central() {
    print_header "📡 Iniciando Risk Central Mock"
    
    if check_risk_central; then
        print_success "Risk Central Mock ya está corriendo"
        return 0
    fi
    
    cd "$RISK_DIR"
    
    # Compilar si es necesario
    if [ ! -d "target" ]; then
        echo "🔨 Compilando Risk Central Mock..."
        ./mvnw clean package -DskipTests > /dev/null 2>&1
    fi
    
    # Iniciar en background
    echo "🚀 Iniciando Risk Central Mock Service..."
    ./mvnw spring-boot:run > /tmp/risk-central.log 2>&1 &
    echo $! > /tmp/risk-central.pid
    
    # Esperar a que esté listo
    echo "⏳ Esperando a que Risk Central esté listo..."
    COUNTER=0
    until check_risk_central || [ $COUNTER -eq 30 ]; do
        printf "."
        sleep 1
        ((COUNTER++))
    done
    echo ""
    
    if [ $COUNTER -eq 30 ]; then
        print_warning "Risk Central no respondió (continuando de todas formas)"
    else
        print_success "Risk Central Mock listo en http://localhost:8081"
    fi
    
    cd "$SCRIPT_DIR"
}

start_api() {
    print_header "🚀 Iniciando Credit Application Service"
    
    if check_api; then
        print_success "Credit Application Service ya está corriendo"
        return 0
    fi
    
    cd "$API_DIR"
    
    # Compilar
    echo "🔨 Compilando Credit Application Service..."
    ./mvnw clean package -DskipTests > /dev/null 2>&1
    
    if [ $? -ne 0 ]; then
        print_error "Error al compilar"
        exit 1
    fi
    
    # Iniciar
    echo "🚀 Iniciando Credit Application Service..."
    ./mvnw spring-boot:run > /tmp/credit-api.log 2>&1 &
    echo $! > /tmp/credit-api.pid
    
    # Esperar a que esté listo
    echo "⏳ Esperando a que la API esté lista..."
    COUNTER=0
    until check_api || [ $COUNTER -eq 60 ]; do
        printf "."
        sleep 2
        ((COUNTER++))
    done
    echo ""
    
    if [ $COUNTER -eq 60 ]; then
        print_error "API no respondió en 60 segundos"
        exit 1
    fi
    
    print_success "Credit Application Service listo en http://localhost:8080"
    
    cd "$SCRIPT_DIR"
}

start_all() {
    print_header "🚀 Sistema CoopCredit - Inicio Completo"
    
    start_mysql
    start_risk_central
    start_api
    
    print_header "🎉 ¡SISTEMA COMPLETO INICIADO!"
    
    echo "📊 Servicios disponibles:"
    echo ""
    echo "  🗄️  MySQL:                   localhost:3306"
    echo "  📡 Risk Central Mock:        http://localhost:8081"
    echo "       Health:                 http://localhost:8081/health"
    echo ""
    echo "  🚀 Credit Application API:   http://localhost:8080"
    echo "       Swagger UI:             http://localhost:8080/swagger-ui.html"
    echo "       Health Check:           http://localhost:8080/actuator/health"
    echo "       API Docs:               http://localhost:8080/v3/api-docs"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    print_info "Para detener los servicios: ./manage.sh stop"
    print_info "Para ver los logs: ./manage.sh logs"
    echo ""
}

stop_all() {
    print_header "🛑 Deteniendo Sistema CoopCredit"
    
    # Detener Spring Boot services
    echo "🔴 Deteniendo Credit Application Service..."
    pkill -f "credit-application-service" 2>/dev/null || true
    if [ -f /tmp/credit-api.pid ]; then
        kill $(cat /tmp/credit-api.pid) 2>/dev/null || true
        rm /tmp/credit-api.pid
    fi
    
    echo "🔴 Deteniendo Risk Central Mock..."
    pkill -f "risk-central-mock-service" 2>/dev/null || true
    if [ -f /tmp/risk-central.pid ]; then
        kill $(cat /tmp/risk-central.pid) 2>/dev/null || true
        rm /tmp/risk-central.pid
    fi
    
    # Detener MySQL
    echo "🔴 Deteniendo MySQL..."
    docker stop coopcredit-mysql 2>/dev/null || true
    docker rm coopcredit-mysql 2>/dev/null || true
    
    echo ""
    print_success "Todos los servicios detenidos"
    echo ""
}

show_status() {
    print_header "📊 Estado de los Servicios"
    
    echo "MySQL:"
    if check_mysql; then
        print_success "Corriendo en localhost:3306"
    else
        print_error "No está corriendo"
    fi
    
    echo ""
    echo "Risk Central Mock:"
    if check_risk_central; then
        print_success "Corriendo en http://localhost:8081"
    else
        print_error "No está corriendo"
    fi
    
    echo ""
    echo "Credit Application API:"
    if check_api; then
        print_success "Corriendo en http://localhost:8080"
    else
        print_error "No está corriendo"
    fi
    
    echo ""
    echo "Procesos Java:"
    ps aux | grep -E "spring-boot|mvnw" | grep -v grep | awk '{print "  PID " $2 ": " $11 " " $12 " " $13}'
    
    echo ""
    echo "Contenedores Docker:"
    docker ps --filter "name=coopcredit" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    echo ""
}

show_logs() {
    print_header "📋 Logs de los Servicios"
    
    echo "Selecciona el servicio:"
    echo "  1) MySQL"
    echo "  2) Risk Central Mock"
    echo "  3) Credit Application API"
    echo "  4) Todos (tail -f)"
    echo ""
    read -p "Opción: " option
    
    case $option in
        1)
            docker logs -f coopcredit-mysql
            ;;
        2)
            if [ -f /tmp/risk-central.log ]; then
                tail -f /tmp/risk-central.log
            else
                print_error "Log no encontrado"
            fi
            ;;
        3)
            if [ -f /tmp/credit-api.log ]; then
                tail -f /tmp/credit-api.log
            else
                print_error "Log no encontrado"
            fi
            ;;
        4)
            tail -f /tmp/risk-central.log /tmp/credit-api.log 2>/dev/null &
            docker logs -f coopcredit-mysql
            ;;
        *)
            print_error "Opción inválida"
            ;;
    esac
}

start_docker_compose() {
    print_header "🐳 Iniciando con Docker Compose"
    
    # Verificar permisos de Docker
    if groups | grep -q docker; then
        print_success "Usuario tiene permisos de Docker"
        SUDO=""
    else
        print_warning "Usuario no está en grupo docker, usando sudo"
        SUDO="sudo"
    fi
    
    echo "🔨 Construyendo e iniciando servicios..."
    $SUDO docker compose up --build -d
    
    sleep 5
    
    echo ""
    echo "📊 Estado de los servicios:"
    $SUDO docker compose ps
    
    echo ""
    print_success "Sistema iniciado con Docker Compose"
    echo ""
    print_info "Ver logs: $SUDO docker compose logs -f"
    print_info "Detener: ./manage.sh stop o $SUDO docker compose down"
    echo ""
}

clean_all() {
    print_header "🧹 Limpieza del Sistema"
    
    stop_all
    
    echo "🗑️  Eliminando contenedores Docker..."
    docker rm -f coopcredit-mysql 2>/dev/null || true
    
    echo "🗑️  Limpiando compilaciones Maven..."
    cd "$API_DIR" && ./mvnw clean > /dev/null 2>&1 || true
    cd "$RISK_DIR" && ./mvnw clean > /dev/null 2>&1 || true
    cd "$SCRIPT_DIR"
    
    echo "🗑️  Eliminando archivos temporales..."
    rm -f /tmp/risk-central.log /tmp/credit-api.log
    rm -f /tmp/risk-central.pid /tmp/credit-api.pid
    
    echo ""
    print_success "Limpieza completada"
    echo ""
}

show_help() {
    echo ""
    echo "CoopCredit System - Script de Gestión"
    echo "======================================"
    echo ""
    echo "Uso: ./manage.sh [comando]"
    echo ""
    echo "Comandos disponibles:"
    echo ""
    echo "  start         - Inicia todo el sistema (MySQL + Risk Central + API)"
    echo "  stop          - Detiene todos los servicios"
    echo "  restart       - Reinicia todo el sistema"
    echo "  status        - Muestra el estado de los servicios"
    echo "  mysql         - Solo inicia MySQL"
    echo "  mock          - Solo inicia Risk Central Mock"
    echo "  docker        - Usa Docker Compose para todo"
    echo "  logs          - Muestra los logs de los servicios"
    echo "  clean         - Limpia contenedores y compilaciones"
    echo "  help          - Muestra esta ayuda"
    echo ""
    echo "Ejemplos:"
    echo ""
    echo "  ./manage.sh start      # Inicia todo el sistema"
    echo "  ./manage.sh status     # Ver el estado"
    echo "  ./manage.sh logs       # Ver logs"
    echo "  ./manage.sh stop       # Detener todo"
    echo ""
}

# ============================================================================
# Main
# ============================================================================

case "${1:-help}" in
    start)
        start_all
        ;;
    stop)
        stop_all
        ;;
    restart)
        stop_all
        sleep 2
        start_all
        ;;
    status)
        show_status
        ;;
    mysql)
        start_mysql
        ;;
    mock)
        start_mysql
        start_risk_central
        ;;
    docker)
        start_docker_compose
        ;;
    logs)
        show_logs
        ;;
    clean)
        clean_all
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Comando desconocido: $1"
        show_help
        exit 1
        ;;
esac

exit 0
