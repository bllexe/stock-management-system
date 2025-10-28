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
