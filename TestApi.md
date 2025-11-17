# Test All Service Manuel

## start all service 
```bash
    docker-compose up -d
``` 
curl http://localhost:8761  # discovery-service
curl http://localhost:8080/actuator/health  # api-gateway

## Add new User for Admin
``` bash

POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "admin",
  "email": "admin@stockmanagement.com",
  "password": "Admin@123",
  "firstName": "Stock",
  "lastName": "Manager",
  "roles": ["ROLE_ADMIN"]
}
```

## Login for Admin
``` bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin@123"
}
```

## Checking Redis
``` bash
    docker exec -it redis redis-cli
    KEYS user:*
    GET user:username:admin
```
- send request for without token 
GET http://localhost:8080/api/auth/login

Expected Result : 401 Unauthorized

- send request for with token 

GET http://localhost:8080/api/auth/login
Content-Type: application/json

Expected Result : 200 OK

KEYS rate:limit:admin
GET rate:limit:admin

## Adding Warehouse

docker exec -it postgres-inventory psql -U postgres -d inventory_db

INSERT INTO warehouses (name, code, address, city, country, active, created_at, updated_at)
VALUES 
  ('Warehouse 1', 'WH001', 'Address 1', 'City 1', 'Country 1', true, NOW(), NOW()),
  ('Warehouse 2', 'WH002', 'Adress 2', 'City 2', 'Country 2', true, NOW(), NOW());

SELECT * FROM warehouses;
\q

## Adding Product
``` bash
POST http://localhost:8080/api/products
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "Dell Latitude 5420",
  "description": "14-inch business laptop with Intel i5",
  "sku": "DELL-LAT-5420",
  "barcode": "5420123456789",
  "price": 15000.00,
  "category": "Electronics",
  "brand": "Dell",
  "unit": "PCS",
  "active": true
}
```
- Checking Product database
``` bash
    docker exec -it postgres-inventory psql -U postgres -d inventory_db
    SELECT * FROM products;
    \q
```
- Checking Redis 
``` bash
    docker exec -it redis redis-cli
    KEYS product:*
    GET products:1
    GET products:sku:DELL-LAT-5420
```

- Prometheus Metrics:
``` bash
    curl http://localhost:8081/actuator/prometheus | grep product_created_total
```

##  Adding more products 
``` bash
# Mouse
POST http://localhost:8080/api/products
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "Logitech MX Master 3",
  "description": "Wireless mouse for professionals",
  "sku": "LOG-MX-MASTER3",
  "barcode": "5051794032106",
  "price": 1500.00,
  "category": "Electronics",
  "brand": "Logitech",
  "unit": "PCS",
  "active": true
}

# Keyboard
POST http://localhost:8080/api/products
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "Keychron K2 Mechanical Keyboard",
  "description": "Wireless mechanical keyboard RGB",
  "sku": "KEY-K2-RGB",
  "barcode": "6970251390195",
  "price": 2000.00,
  "category": "Electronics",
  "brand": "Keychron",
  "unit": "PCS",
  "active": true
}
```

- Checking Product Service logs
``` bash
    tail -f logs/product-service.log | grep "cache"
```
** Expected Result : 
Product found in cache: 1
Cache hit

- Filtre by category
GET http://localhost:8080/api/products/category/Electronics
Authorization: Bearer {accessToken}

- Redis 
KEYS products:category:*
GET products:category:Electronics


## Inventory service
```bash
POST http://localhost:8080/api/inventory
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "productId": 1,
  "warehouseId": 1,
  "quantity": 100,
  "minimumQuantity": 10,
  "shelfLocation": "A-01-01"
}
```
--FAIL Redis kaydi yok


- Checking Inventory database
```bash
    docker exec -it postgres-inventory psql -U postgres -d inventory_db
    SELECT * FROM inventory;
    \q
- Redis 
KEYS inventory:*
GET inventory:1:1

```bash 
# Mouse 
POST http://localhost:8080/api/inventory
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "productId": 2,
  "warehouseId": 1,
  "quantity": 50,
  "minimumQuantity": 5,
  "shelfLocation": "B-02-03"
}

# Keyboard 
POST http://localhost:8080/api/inventory
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "productId": 3,
  "warehouseId": 2,
  "quantity": 30,
  "minimumQuantity": 5,
  "shelfLocation": "C-01-05"
}

POST http://localhost:8080/api/inventory/movement
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "productId": 1,
  "warehouseId": 1,
  "type": "IN",
  "quantity": 50,
  "reason": "New stock arrival",
  "referenceNo": "PO-2024-001"
}

```

- Checking Inventory database
```bash
    docker exec -it postgres-inventory psql -U postgres -d inventory_db
    SELECT * FROM stock_movements;
    SELECT * FROM inventory WHERE product_id = 1;
    \q
```

@FAIL cikis ve giris calismadi (pom xml java.time.localDatetime hatsi yuzunde jr300 eklendi)

- Checking low stock products
```bash
    # Stok Çıkışı (140 adet)
POST http://localhost:8080/api/inventory/movement
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "productId": 1,
  "warehouseId": 1,
  "type": "OUT",
  "quantity": 140,
  "reason": "Sales",
  "referenceNo": "SO-2024-001"
}

# Düşük stok kontrolü
GET http://localhost:8080/api/inventory/low-stock
Authorization: Bearer {accessToken}
```

## Customer Service 
``` bash 
POST http://localhost:8080/api/customers
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "customerCode": "CUST-001",
  "firstName": "user 1",
  "lastName": "user 1",
  "email": "user1.user1@example.com",
  "phone": "+905551234567",
  "taxNumber": "1234567890",
  "type": "INDIVIDUAL",
  "status": "ACTIVE",
  "creditLimit": 50000.00,
  "segment": "REGULAR",
  "notes": "VIP Customer"
}
POST http://localhost:8080/api/customers/1/addresses
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "type": "BOTH",
  "addressLine1": "address line 1",
  "addressLine2": "address line 2",
  "city": "city1",
  "state": "state1",
  "country": "Country1",
  "postalCode": "12345",
  "isDefault": true
}

```
!!!!!!!!!!FAIL order create edilmedi
## OrderService-SAGA Pattern   
```bash
POST http://localhost:8080/api/orders
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "customerId": 1,
  "warehouseId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 3
    }
  ],
  "notes": "Acil teslimat"
}

```

- Checking logs 

# Logs
tail -f logs/order-service.log | grep "Calling product-service"

# Stok kontrol edildi
GET http://localhost:8080/api/inventory/product/1/warehouse/1
Authorization: Bearer {accessToken}

Redis:
# Reserved quantity 
redis-cli
GET inventory:1:1

SELECT product_id, quantity, reserved_quantity, available_quantity 
FROM inventory 
WHERE product_id IN (1, 2);

!!! BURAYA KADAR TEST EDILDI
    
- Event publishing (RabbitMQ)
# RabbitMQ Management UI
open http://localhost:15672
# Login: admin/admin

# Queues kontrol
# order.created.queue - 1 message
# customer.order.confirmed.queue - 1 message
# inventory.order.created.queue - 1 message

- Customer Service EventListener

# Customer logs
tail -f logs/customer-service.log | grep "order confirmed"

Received order confirmed event for order: ORD-20241203140530
Added 10 loyalty points to customer: 1

- Checking customer loyalty points

GET http://localhost:8080/api/customers/1
Authorization: Bearer {accessToken}

## Zipkin Distributed Tracing

# Zipkin UI
open http://localhost:9411

# Search:
# - Service: order-service
# - Span Name: createOrder

createOrder - Order Service
getProductById - Product Service (Feign)
getInventory - Inventory Service (Feign)
reserveStock - Inventory Service
acquireLock - Redis lock
publishEvent - RabbitMQ

- Low Stock Alert
```bash
POST http://localhost:8080/api/orders
Authorization: Bearer {accessToken}
Content-Type: application/json
```

{
  "customerId": 1,
  "warehouseId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 100
    }
  ]
}

## Cancel Order (Rollback)
```bash
DELETE http://localhost:8080/api/orders/1
Authorization: Bearer {accessToken}
204 No Content

- Order Status 

GET http://localhost:8080/api/orders/1
Authorization: Bearer {accessToken}

- Inventory Release

GET http://localhost:8080/api/inventory/product/1/warehouse/1
Authorization: Bearer {accessToken}
```
- Event Published

 RabbitMQ
 order.cancelled.queue - 1 message

 # Supplier Service

``` bash POST http://localhost:8080/api/suppliers
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "Tech Supplies Inc.",
  "code": "SUP-001",
  "taxNumber": "9876543210",
  "contactPerson": "John Doe",
  "email": "john@techsupplies.com",
  "phone": "+15551234567",
  "address": "123 Tech Street",
  "city": "San Francisco",
  "country": "USA",
  "postalCode": "94102",
  "paymentTerms": "Net 30",
  "creditLimit": 100000.00,
  "status": "ACTIVE",
  "rating": 5
}
```

``` bash POST http://localhost:8080/api/supplier-products/supplier/1
Authorization: Bearer {accessToken}
Content-Type: application/json
```
{
  "productId": 1,
  "supplierSku": "SUP-DELL-LAT-5420",
  "supplierProductName": "Dell Latitude 5420 (Supplier)",
  "unitPrice": 12000.00,
  "minimumOrderQuantity": 10,
  "leadTimeDays": 14,
  "active": true
}

- Purchase Order
``` bash POST http://localhost:8080/api/purchase-orders
Authorization: Bearer {accessToken}
Content-Type: application/json
```

{
  "supplierId": 1,
  "orderDate": "2024-12-03",
  "expectedDeliveryDate": "2024-12-17",
  "warehouseId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 50
    }
  ],
  "notes": "Urgent restocking"
}

# Rate Limiting
``` bash 
for i in {1..25}; do
  curl -w "\n%{http_code}\n" \
    -H "Authorization: Bearer {token}" \
    http://localhost:8080/api/products
  echo "Request $i"
done
``` 
Expected Result: 
200 OK for first 20 requests
429 Too Many Requests for remaining requests

- Redis
redis-cli
KEYS request_rate_limiter*

- Login Rate Limit 

``` bash for i in {1..6}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"wrong"}'
  echo "Attempt $i"
done
```

Expected Result: 
401 Unauthorized for first 5 requests
429 Too Many Requests for remaining requests

- Redis
redis-cli
GET rate:limit:admin

# Cache Hit/Miss

# (cache miss)
time curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/products/1

# (cache hit)
time curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/products/1

first request ~50-100 ms
second request ~5-10 ms

- Prometheus Metrics

curl http://localhost:8081/actuator/prometheus | grep product_cache

product_cache_hit_total 15
product_cache_miss_total 3

# Monitoring and Observability

open http://localhost:9090

# Queries:
# 1. HTTP Request  
http_server_requests_seconds_count{uri="/api/orders"}

# 2. JVM Memory
jvm_memory_used_bytes{application="product-service"}

# 3. Cache hit ratio
rate(product_cache_hit_total[5m]) / (rate(product_cache_hit_total[5m]) + rate(product_cache_miss_total[5m]))

# 4. Order creation rate
rate(order_created_total[1m])


# Grafana Dashboard
open http://localhost:3000
# Login: admin/admin

# Import Dashboard: 11378 (JVM Micrometer)


# Zipkin Tracing 

open http://localhost:9411

 Find Traces:
 - serviceName: order-service
 - minDuration: 100ms

 - Trace timeline
 - Service dependencies
 - Slow spans
 
 # Logout Token Blacklist 

 POST http://localhost:8080/api/auth/logout
Authorization: Bearer {accessToken}

- Redis 

redis-cli
KEYS blacklist:token:*
GET blacklist:token:{token}
GET http://localhost:8080/api/products
Authorization: Bearer {old-token}
```

**Expected:**

Authentication:           5/5  ✓
Product Service:          8/8  ✓
Inventory Service:        6/6  ✓
Order Service (Saga):     4/4  ✓
Customer Service:         3/3  ✓
Supplier Service:         4/4  ✓
Rate Limiting:            2/2  ✓
Cache Performance:        2/2  ✓
Monitoring:               3/3  ✓
Event-Driven:             3/3  ✓
----------------------------------
Total:                  40/40 ✓
