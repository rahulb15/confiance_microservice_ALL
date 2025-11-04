# Microservices Architecture - Industrial Level

## 🏗️ Architecture Overview

This is a complete, production-ready microservices architecture built with Spring Boot, featuring:

- **Service Discovery** (Eureka Server)
- **Configuration Management** (Config Server)
- **API Gateway** with security, rate limiting, and circuit breakers
- **Authentication Service** with JWT and role-based security
- **User Management Service** with comprehensive CRUD operations
- **Shared Library** with common utilities and exception handling

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- Git

### 1. Clone and Build
```bash
git clone <repository-url>
cd microservices-project
make build
```

### 2. Start Infrastructure
```bash
# Start all services
make start

# Or with monitoring
make start-monitoring

# For development (with debug ports)
make start-dev
```

### 3. Verify Services
```bash
# Check status
make status

# View logs
make logs

# Check specific service
make logs-service SERVICE=auth-service
```

## 📋 Service Endpoints

### API Gateway (Port 8080)
- **Main Entry Point**: `http://localhost:8080`
- **Health Check**: `http://localhost:8080/actuator/health`

### Authentication Service (via Gateway)
```bash
# Register User
POST /auth/register
{
  "username": "testuser",
  "email": "test@example.com", 
  "password": "Test@123"
}

# Login
POST /auth/login
{
  "username": "testuser",
  "password": "Test@123"
}

# Refresh Token
POST /auth/refresh
{
  "refreshToken": "your-refresh-token"
}
```

### User Service (via Gateway)
```bash
# Get Current User (Authenticated)
GET /users/me
Authorization: Bearer <jwt-token>

# Update Profile
PUT /users/me
Authorization: Bearer <jwt-token>
{
  "firstName": "John",
  "lastName": "Doe",
  "bio": "Software Developer"
}

# Admin: Get All Users
GET /users?page=0&size=10
Authorization: Bearer <admin-jwt-token>
```

## 🔐 Authentication Flow

1. **Register/Login** → Get JWT tokens (access + refresh)
2. **Include JWT** in Authorization header: `Bearer <token>`
3. **API Gateway validates** JWT and routes to services
4. **Services receive** user info in headers (`X-User-Id`, `X-User-Roles`)

### Default Users
- **Admin**: `admin` / `Admin@123`
- **Test User**: `testuser` / `Test@123`

## 🏗️ Development Guide

### Project Structure
```
microservices-project/
├── common-lib/          # Shared utilities, DTOs, security
├── eureka-server/       # Service discovery
├── config-server/       # Centralized configuration
├── auth-service/        # JWT authentication
├── user-service/        # User management
├── api-gateway/         # Request routing & security
├── docker-compose.yml   # Container orchestration
└── scripts/            # Database initialization
```

### Adding New Service

1. **Create Maven Module**
```xml
<parent>
    <groupId>com.microservices</groupId>
    <artifactId>microservices-parent</artifactId>
    <version>1.0.0</version>
</parent>
```

2. **Add Dependencies**
```xml
<dependency>
    <groupId>com.microservices</groupId>
    <artifactId>common-lib</artifactId>
</dependency>
```

3. **Enable Eureka Client**
```java
@SpringBootApplication
@EnableEurekaClient
public class NewServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NewServiceApplication.class, args);
    }
}
```

4. **Add Gateway Route**
```java
.route("new-service", r -> r
    .path("/new/**")
    .filters(f -> f.filter(authenticationFilter))
    .uri("lb://new-service"))
```

### Testing

```bash
# Unit Tests
mvn test

# Integration Tests  
mvn verify

# Specific Service
mvn test -pl auth-service
```

## 🔧 Configuration

### Environment Variables
- `JWT_SECRET`: JWT signing secret
- `MYSQL_ROOT_PASSWORD`: MySQL root password
- `POSTGRES_PASSWORD`: PostgreSQL password
- `REDIS_HOST`: Redis host for rate limiting

### Service Configuration
Each service can be configured via:
1. **Config Server** (centralized)
2. **Environment Variables**
3. **application.yml** (local fallback)

## 📊 Monitoring

### Health Checks
- **Individual**: `http://localhost:808X/actuator/health`
- **Gateway**: `http://localhost:8080/actuator/health`

### Metrics (with monitoring profile)
- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3000` (admin/admin)

### Logs
```bash
# All services
make logs

# Specific service
make logs-service SERVICE=user-service
```

## 🚀 Production Deployment

### Docker Swarm
```bash
docker swarm init
docker stack deploy -c docker-compose.yml microservices
```

### Kubernetes
```bash
kubectl apply -f deployment/kubernetes/
```

### Cloud Deployment
- **AWS ECS/EKS**
- **Azure Container Instances/AKS**
- **Google Cloud Run/GKE**

## 🔒 Security Features

- **JWT Authentication** with refresh tokens
- **Role-based Authorization** (USER, ADMIN, MODERATOR)
- **Rate Limiting** (60 req/min general, 10 req/min auth)
- **Account Locking** after failed login attempts
- **CORS Protection**
- **Circuit Breakers** for fault tolerance

## 📈 Performance Features

- **Connection Pooling** (HikariCP)
- **Caching** (Redis)
- **Async Processing** (WebFlux in Gateway)
- **Load Balancing** (Eureka + Ribbon)
- **Health Checks** & **Metrics**

## 🛠️ Troubleshooting

### Common Issues

1. **Services not registering**
    - Check Eureka Server is running
    - Verify `eureka.client.service-url.defaultZone`

2. **Database connection issues**
    - Ensure databases are healthy: `docker-compose ps`
    - Check connection strings and credentials

3. **JWT issues**
    - Verify JWT_SECRET is consistent across services
    - Check token expiration times

4. **Gateway routing issues**
    - Check service registration in Eureka: `http://localhost:8761`
    - Verify route configuration

### Debugging

```bash
# Enable debug mode
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d

# Connect debugger to port 5005-5007
# Check logs
make logs-service SERVICE=problematic-service
```

## 📚 API Documentation

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/v3/api-docs`

Each service also exposes its own documentation:
- Auth Service: `http://localhost:8081/swagger-ui.html`
- User Service: `http://localhost:8082/swagger-ui.html`

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -am 'Add new feature'`
4. Push to branch: `git push origin feature/new-feature`
5. Submit Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.