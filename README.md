## 🚀 Project Structure

| Service Name | Description | Port |
|---------------|-------------|------|
| **config-server** | Provides configuration for all microservices via `config-repo`. | 8888 |
| **eureka-server** | Handles service discovery and registration. | 8761 |
| **api-gateway** | Acts as the single entry point for all microservices. | 8080 |
| **authentication-service** | Manages user registration and authentication. | 8090  |
| **product-service** | Manages product information (create, update, list). | 8081 |
| **order-service** | Handles order creation and management. | 8083 |
| **inventory-service** | Tracks stock quantities. | 8082 |
| **config-repo** | Repository containing configuration files for all services. Store local or github  |
| **supplier-service** | Manages supplier information (create, update, list). | 8085 |

---

## 🗄️ Technologies Used

- **Java 21+**
- **Spring Boot / Spring Cloud**
- **PostgreSQL** (database)
- **Eureka Server** (service discovery)
- **Spring Cloud Config Server**
- **Spring Cloud Gateway**
- **Docker & Docker Compose**
- **Spring Security / JWT**

---

## ⚙️ Startup Order

Services should be started in the following order:

1. 🧩 **config-server**
2. 🛰️ **eureka-server**
3. 🌉 **api-gateway**
4. 🔐 **authentication-service**
5. 🛒 **product-service**
6. 📦 **order-service**
7. 📊 **inventory-service**
8. 📦 **supplier-service**

Each service contains its own `application.yml` with configuration and discovery settings.

---

## 🐳 Run with Docker Compose

To start all services using Docker, run the following command in the project root:

```bash
docker-compose up --build

```

To stop all services, run the following command:
```bash
docker-compose down
```

## Connect to Database

docker exec -it <container_name> psql -U postgres -d <database_name>



# 1. Infrastructure
docker-compose up -d

# 2. Core Services
cd config-server && mvn spring-boot:run
cd discovery-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run

# 3. Business Services
cd product-service && mvn spring-boot:run
cd inventory-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd customer-service && mvn spring-boot:run
cd supplier-service && mvn spring-boot:run
cd authentication-service && mvn spring-boot:run

# 4. Test Flow

# 1. Register
POST /api/auth/register

# 2. Login (token al)
POST /api/auth/login

# 3. Create Product (token ile)
POST /api/products
Authorization: Bearer {token}

# 4. Create Inventory
POST /api/inventory

# 5. Create Customer
POST /api/customers

# 6. Create Order (event trigger)
POST /api/orders

## URLS

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)
- Actuator Endpoints (örnek):

- http://localhost:8081/actuator
- http://localhost:8081/actuator/health
- http://localhost:8081/actuator/metrics
- http://localhost:8081/actuator/prometheus

## GRAFANA

- http://localhost:3000
- Login: admin/admin
- Add Data Source → Prometheus

- URL: http://prometheus:9090
- Save & Test

- Import Dashboard:

- Dashboard ID: 11378 (JVM Micrometer)
- Dashboard ID: 4701 (Spring Boot Statistics)

## PROMETHEUS QUERIES

# CPU Usage 
- process_cpu_usage{application="product-service"}

# Memory Usage 
- jvm_memory_used_bytes{application="product-service"}

# HTTP Requests 
- http_server_requests_seconds_count{uri="/api/products"}

# Custom metrics
- product_created_total
- product_cache_hit_total
- product_cache_miss_total
- order_created_total
- order_processing_time_seconds_sum


## Health Check Endpoints

# Health check
- curl http://localhost:8081/actuator/health

# Metrics
- curl http://localhost:8081/actuator/metrics

# Prometheus format
- curl http://localhost:8081/actuator/prometheus

