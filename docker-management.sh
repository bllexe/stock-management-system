#!/bin/bash
# docker-management.sh - Stock Management System Docker Management

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Service lists
INFRASTRUCTURE_SERVICES=("postgres-product" "postgres-inventory" "postgres-order" "postgres-customer" "postgres-supplier" "postgres-auth" "redis" "rabbitmq" "prometheus" "grafana" "zipkin")
MICROSERVICES=("config-server" "discovery-service" "api-gateway" "product-service" "inventory-service" "order-service" "customer-service" "supplier-service" "authentication-service")

# Helper functions
print_header() {
    echo -e "${CYAN}================================${NC}"
    echo -e "${CYAN}  $1${NC}"
    echo -e "${CYAN}================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# Check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    print_success "Docker is running"
}

# Start infrastructure services
start_infrastructure() {
    print_header "Starting Infrastructure Services"
    
    if ! docker-compose ps | grep -q "Up"; then
        docker-compose up -d
        print_success "Infrastructure services started"
    else
        print_warning "Infrastructure services already running"
    fi
    
    # Wait for services to be healthy
    print_info "Waiting for services to be healthy (30s)..."
    sleep 30
}

# Stop infrastructure services
stop_infrastructure() {
    print_header "Stopping Infrastructure Services"
    docker-compose down
    print_success "Infrastructure services stopped"
}

# Build specific service
build_service() {
    local service=$1
    print_info "Building $service..."
    
    cd $service
    if mvn clean package -DskipTests > /dev/null 2>&1; then
        print_success "$service built successfully"
    else
        print_error "Failed to build $service"
        exit 1
    fi
    cd ..
}

# Build all microservices
build_all() {
    print_header "Building All Microservices"
    
    for service in "${MICROSERVICES[@]}"; do
        build_service $service
    done
    
    print_success "All services built successfully"
}

# Start specific microservice
start_service() {
    local service=$1
    print_info "Starting $service..."
    
    # Check if already running
    if pgrep -f "$service" > /dev/null; then
        print_warning "$service is already running"
        return
    fi
    
    cd $service
    nohup mvn spring-boot:run > ../logs/$service.log 2>&1 &
    local pid=$!
    echo $pid > ../logs/$service.pid
    cd ..
    
    print_success "$service started (PID: $pid)"
}

# Stop specific microservice
stop_service() {
    local service=$1
    print_info "Stopping $service..."
    
    if [ -f "logs/$service.pid" ]; then
        local pid=$(cat logs/$service.pid)
        if kill -0 $pid 2>/dev/null; then
            kill $pid
            rm logs/$service.pid
            print_success "$service stopped"
        else
            print_warning "$service not running"
            rm logs/$service.pid
        fi
    else
        print_warning "$service PID file not found"
    fi
}

# Start all microservices in order
start_all_services() {
    print_header "Starting All Microservices"
    
    # Create logs directory
    mkdir -p logs
    
    # Start in order
    print_info "Starting Config Server..."
    start_service "config-server"
    sleep 15
    
    print_info "Starting Discovery Service..."
    start_service "discovery-service"
    sleep 20
    
    print_info "Starting Business Services..."
    start_service "product-service" &
    start_service "inventory-service" &
    start_service "order-service" &
    start_service "customer-service" &
    start_service "supplier-service" &
    start_service "authentication-service" &
    wait
    sleep 20
    
    print_info "Starting API Gateway..."
    start_service "api-gateway"
    sleep 10
    
    print_success "All services started"
}

# Stop all microservices
stop_all_services() {
    print_header "Stopping All Microservices"
    
    for service in "${MICROSERVICES[@]}"; do
        stop_service $service
    done
    
    print_success "All services stopped"
}

# Check service status
check_status() {
    print_header "Service Status"
    
    echo -e "${CYAN}Infrastructure Services:${NC}"
    docker-compose ps
    
    echo ""
    echo -e "${CYAN}Microservices:${NC}"
    printf "%-25s %-10s %-10s\n" "SERVICE" "STATUS" "PID"
    echo "---------------------------------------------------"
    
    for service in "${MICROSERVICES[@]}"; do
        if [ -f "logs/$service.pid" ]; then
            local pid=$(cat logs/$service.pid)
            if kill -0 $pid 2>/dev/null; then
                printf "%-25s ${GREEN}%-10s${NC} %-10s\n" "$service" "RUNNING" "$pid"
            else
                printf "%-25s ${RED}%-10s${NC} %-10s\n" "$service" "STOPPED" "N/A"
            fi
        else
            printf "%-25s ${RED}%-10s${NC} %-10s\n" "$service" "STOPPED" "N/A"
        fi
    done
}

# Show logs for specific service
show_logs() {
    local service=$1
    
    if [ -z "$service" ]; then
        print_error "Please specify a service name"
        echo "Usage: $0 logs <service-name>"
        return
    fi
    
    if [ -f "logs/$service.log" ]; then
        tail -f logs/$service.log
    else
        print_error "Log file not found for $service"
    fi
}

# Clean logs
clean_logs() {
    print_header "Cleaning Logs"
    
    if [ -d "logs" ]; then
        rm -rf logs/*
        print_success "Logs cleaned"
    else
        print_warning "No logs directory found"
    fi
}

# Restart specific service
restart_service() {
    local service=$1
    
    if [ -z "$service" ]; then
        print_error "Please specify a service name"
        echo "Usage: $0 restart <service-name>"
        return
    fi
    
    stop_service $service
    sleep 2
    start_service $service
}

# Health check
health_check() {
    print_header "Health Check"
    
    local services=(
        "config-server:8888"
        "discovery-service:8761"
        "api-gateway:8080"
        "product-service:8081"
        "inventory-service:8082"
        "order-service:8083"
        "customer-service:8084"
        "supplier-service:8085"
        "authentication-service:8090"
    )
    
    for service_port in "${services[@]}"; do
        IFS=':' read -r service port <<< "$service_port"
        
        if curl -s "http://localhost:$port/actuator/health" > /dev/null; then
            printf "%-25s ${GREEN}HEALTHY${NC}\n" "$service"
        else
            printf "%-25s ${RED}UNHEALTHY${NC}\n" "$service"
        fi
    done
}

# Show service URLs
show_urls() {
    print_header "Service URLs"
    
    echo -e "${CYAN}Microservices:${NC}"
    echo "  API Gateway:         http://localhost:8080"
    echo "  Eureka Dashboard:    http://localhost:8761"
    echo "  Config Server:       http://localhost:8888"
    echo ""
    echo -e "${CYAN}Infrastructure:${NC}"
    echo "  Prometheus:          http://localhost:9090"
    echo "  Grafana:             http://localhost:3000 (admin/admin)"
    echo "  Zipkin:              http://localhost:9411"
    echo "  RabbitMQ Management: http://localhost:15672 (admin/admin)"
    echo ""
    echo -e "${CYAN}Databases:${NC}"
    echo "  PostgreSQL:          localhost:5432-5437"
    echo "  Redis:               localhost:6379"
}

# Full system restart
full_restart() {
    print_header "Full System Restart"
    
    stop_all_services
    stop_infrastructure
    
    sleep 5
    
    start_infrastructure
    start_all_services
    
    print_success "System restarted"
}

# Clean everything
clean_all() {
    print_header "Cleaning Everything"
    
    read -p "This will stop all services and remove volumes. Continue? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_warning "Cancelled"
        return
    fi
    
    stop_all_services
    docker-compose down -v
    clean_logs
    
    print_success "Everything cleaned"
}

# Show help
show_help() {
    cat << EOF
${CYAN}Stock Management System - Docker Management${NC}

Usage: $0 [COMMAND] [OPTIONS]

${YELLOW}Commands:${NC}
  ${GREEN}Infrastructure:${NC}
    infra-start           Start infrastructure services (PostgreSQL, Redis, RabbitMQ, etc.)
    infra-stop            Stop infrastructure services
    infra-status          Show infrastructure status

  ${GREEN}Build:${NC}
    build <service>       Build specific service
    build-all             Build all microservices

  ${GREEN}Services:${NC}
    start <service>       Start specific microservice
    stop <service>        Stop specific microservice
    restart <service>     Restart specific microservice
    start-all             Start all microservices
    stop-all              Stop all microservices

  ${GREEN}Monitoring:${NC}
    status                Show all services status
    health                Check health of all services
    logs <service>        Show logs for specific service
    urls                  Show all service URLs

  ${GREEN}Maintenance:${NC}
    full-restart          Restart entire system
    clean-logs            Clean all log files
    clean-all             Stop everything and remove volumes

  ${GREEN}Help:${NC}
    help                  Show this help message

${YELLOW}Examples:${NC}
  $0 infra-start                    # Start infrastructure
  $0 build-all                      # Build all services
  $0 start-all                      # Start all microservices
  $0 status                         # Check status
  $0 logs product-service           # View product service logs
  $0 restart order-service          # Restart order service
  $0 health                         # Health check all services

${YELLOW}Service Names:${NC}
  config-server, discovery-service, api-gateway
  product-service, inventory-service, order-service
  customer-service, supplier-service, authentication-service

EOF
}

# Main script
main() {
    local command=$1
    local service=$2
    
    case $command in
        infra-start)
            check_docker
            start_infrastructure
            ;;
        infra-stop)
            stop_infrastructure
            ;;
        infra-status)
            docker-compose ps
            ;;
        build)
            if [ -z "$service" ]; then
                print_error "Please specify a service name"
                exit 1
            fi
            build_service $service
            ;;
        build-all)
            build_all
            ;;
        start)
            if [ -z "$service" ]; then
                print_error "Please specify a service name"
                exit 1
            fi
            start_service $service
            ;;
        stop)
            if [ -z "$service" ]; then
                print_error "Please specify a service name"
                exit 1
            fi
            stop_service $service
            ;;
        restart)
            restart_service $service
            ;;
        start-all)
            check_docker
            start_infrastructure
            start_all_services
            ;;
        stop-all)
            stop_all_services
            ;;
        status)
            check_status
            ;;
        health)
            health_check
            ;;
        logs)
            show_logs $service
            ;;
        urls)
            show_urls
            ;;
        clean-logs)
            clean_logs
            ;;
        full-restart)
            full_restart
            ;;
        clean-all)
            clean_all
            ;;
        help|--help|-h|"")
            show_help
            ;;
        *)
            print_error "Unknown command: $command"
            echo "Run '$0 help' for usage information"
            exit 1
            ;;
    esac
}

# Run main
main "$@"