#!/bin/bash

# Namespace oluştur
kubectl apply -f namespace.yaml

# ConfigMap
kubectl apply -f configmap.yaml

# Databases
kubectl apply -f postgres-deployments.yaml

# Infrastructure
kubectl apply -f redis-deployment.yaml
kubectl apply -f rabbitmq-deployment.yaml

# Wait for databases
echo "Waiting for databases..."
sleep 30

# Discovery Service
kubectl apply -f discovery-service-deployment.yaml

# Wait for Eureka
echo "Waiting for Eureka..."
sleep 30

# Microservices
kubectl apply -f product-service-deployment.yaml
kubectl apply -f inventory-service-deployment.yaml
kubectl apply -f order-service-deployment.yaml
kubectl apply -f customer-service-deployment.yaml
kubectl apply -f supplier-service-deployment.yaml
kubectl apply -f authentication-service-deployment.yaml

# API Gateway
kubectl apply -f api-gateway-deployment.yaml

# Monitoring
kubectl apply -f monitoring-deployment.yaml

echo "Deployment complete!"
echo "Check status: kubectl get pods -n stock-management"