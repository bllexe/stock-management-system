#!/bin/bash

eval $(minikube docker-env)

services=(
    "authentication-service"
    "config-server"
    "customer-service"
    "eureka-server"
    "inventory-service"
    "order-service"
    "product-service"
    "supplier-service"
    "api-gateway"
)

for service in ${services[@]}; do
    echo "Building $service"
    cd $service
    mvn clean package -DskipTests
    docker build -t $service:latest .
    cd ..
done

echo "All services built successfully"