.PHONY: help build start stop clean logs test

help: ## Show this help message
	@echo 'Microservices Management Commands:'
	@echo ''
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Build all services
	@echo "Building all microservices..."
	mvn clean package -DskipTests
	docker-compose build

start: ## Start all services
	@echo "Starting microservices stack..."
	docker-compose up -d

start-dev: ## Start with development configuration
	@echo "Starting microservices stack in development mode..."
	docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d

start-monitoring: ## Start with monitoring enabled
	@echo "Starting microservices stack with monitoring..."
	docker-compose --profile monitoring up -d

stop: ## Stop all services
	@echo "Stopping microservices stack..."
	docker-compose down

clean: ## Clean up containers, networks, and volumes
	@echo "Cleaning up..."
	docker-compose down -v --remove-orphans
	docker system prune -f

logs: ## Show logs for all services
	docker-compose logs -f

logs-service: ## Show logs for specific service (usage: make logs-service SERVICE=auth-service)
	docker-compose logs -f $(SERVICE)

test: ## Run tests for all services
	@echo "Running tests..."
	mvn test

restart: ## Restart all services
	@echo "Restarting microservices stack..."
	docker-compose restart

status: ## Show status of all services
	docker-compose ps
