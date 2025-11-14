# Build images
chmod +x build-images.sh
./build-images.sh

# Deploy
cd k8s
chmod +x deploy.sh
./deploy.sh

# Status kontrol
kubectl get pods -n stock-management
kubectl get svc -n stock-management

# Logs
kubectl logs -f <pod-name> -n stock-management

# Port forward (alternatif)
kubectl port-forward svc/api-gateway 8080:8080 -n stock-management

# Minikube service URL
minikube service api-gateway -n stock-management --url

# Dashboard
minikube dashboard

# API Gateway
minikube service api-gateway -n stock-management

# Eureka Dashboard
minikube service discovery-service -n stock-management

# Prometheus
minikube service prometheus -n stock-management

# Grafana
minikube service grafana -n stock-management

# Zipkin
minikube service zipkin -n stock-management

# RabbitMQ Management
kubectl port-forward svc/rabbitmq 15672:15672 -n stock-management
