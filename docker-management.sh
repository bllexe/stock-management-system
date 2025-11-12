#!/bin/bash
# docker-management.sh  -User Service Docker Management


set -e
#Color for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
# MAGENTA='\033[0;35m'
# CYAN='\033[0;36m'
NC='\033[0m' #No-Color

print_status(){
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success(){
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error(){
    echo -e "${RED}[ERROR]${NC} $1"
}

create_directories(){
    print_status "Createing required directories..."
    mkdir -p monitoring
    mkdir -p monitoring/grafana/provisioning/dashboards
    mkdir -p monitoring/grafana/provisioning/datasources
}

#Build and start all services
build_all(){
    print_status "Building ans Starting all services..."
    docker-compose up -d 
    print_success "All Services started"
}
#stop all services
stop_all(){
    print_status "Stopping all services..."
    docker-compose down
    print_success "All Services stopped"
}
# Stop and remove all (including volumes)
 clean_all() {
     print_warning "This will remove all containers, networks, and volumes!"
     read -p "Are you sure? (y/N): " -n 1 -r
     echo
     if [[ $REPLY =~ ^[Yy]$ ]]; then
         print_status "Cleaning up everything..."
         docker-compose down -v --remove-orphans
         docker system prune -f
         print_success "Cleanup completed"
     else
         print_status "Cleanup cancelled"
     fi
 }
#show logs for a spesicif service
show_logs(){
    print_status "Showing logs for $1 service..."
    docker-compose logs -f $1
}

 # Show status of all services
 show_status() {
     print_status "Docker Compose Services Status:"
     docker-compose ps
     echo
     print_status "Container Resource Usage:"
     docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}\t{{.NetIO}}"
 }

#Main command handler 
case "${1:-help}" in
    setup)
        create_directories
        ;;
    build)
        build_all
        ;;
    stop)
        stop_all
        ;;
    clean)
        clean_all
        ;;
    logs)
        show_logs ${2}
        ;;
    status)
        show_status
        ;;
    help)
        show_help
        ;;
    *) 
    print_error "Unknown command : $1"
    show_help
    exit 1
    ;;
esac
