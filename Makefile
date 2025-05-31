# Variables for the application containers
APP_CONTAINER_IMAGE=my-rest-nginx-app
APP_CONTAINER_NAME=rest-nginx-app
APP_DOCKER_CONTEXT=.
APP_DOCKERFILE=./src/main/docker/app/Dockerfile
APP_PORT=8081

# Variables for the Nginx reverse proxy
NGINX_CONTAINER_IMAGE=my-nginx-proxy
NGINX_CONTAINER_NAME=nginx-reverse-proxy
NGINX_DOCKERFILE=./src/main/docker/nginx/Dockerfile
NGINX_DOCKER_CONTEXT=./src/main/docker/nginx
NGINX_PORT=443

# Network for the application and RabbitMQ containers
NETWORK=app-network

# Running in development mode
dev:
	@echo "Running in development mode..."
	./mvnw spring-boot:run

# Building the application as a JAR file
# This will run Maven Lifecycle phase "package": clean → validate → compile → test → package, 
# which cleans the target directory, compiles the code, runs tests, and packages the application into a JAR file.
package:
	@echo "Building the application as a JAR file..."
	./mvnw clean package -DskipTests


# Docker related targets
# Create a Docker network if it does not exist
docker-create-network:
	docker network inspect $(NETWORK) >NUL 2>&1 || docker network create $(NETWORK)

# Remove the Docker network if it exists
docker-remove-network:
	docker network rm $(NETWORK)

# --- Application Docker Targets ---
# Build the application in Docker
docker-build-app:
	docker build -f $(APP_DOCKERFILE) -t $(APP_CONTAINER_IMAGE) $(APP_DOCKER_CONTEXT)

# Run the application in Docker
docker-run-app: 
	docker run --name $(APP_CONTAINER_NAME) --network $(NETWORK) -p $(APP_PORT):$(APP_PORT) \
	-e SERVER_PORT=$(APP_PORT) \
	-d $(APP_CONTAINER_IMAGE)

# Build and run the application container
docker-build-run-app: docker-build-app docker-run-app

# Remove the application container
docker-remove-app:
	docker stop $(APP_CONTAINER_NAME)
	docker rm $(APP_CONTAINER_NAME)


# --- Nginx Docker Targets ---
# Build the Nginx reverse proxy
docker-build-nginx:
	docker build -f $(NGINX_DOCKERFILE) -t $(NGINX_CONTAINER_IMAGE) $(NGINX_DOCKER_CONTEXT)

# Run the Nginx reverse proxy container
docker-run-nginx:
	docker run --name $(NGINX_CONTAINER_NAME) --network $(NETWORK) -p $(NGINX_PORT):$(NGINX_PORT) \
	-v nginx-ssl-data:/etc/nginx/ssl \
	-d $(NGINX_CONTAINER_IMAGE)

# Build and run the Nginx reverse proxy container
docker-build-run-nginx: docker-build-nginx docker-run-nginx

# Remove the Nginx container
docker-remove-nginx:
	docker stop $(NGINX_CONTAINER_NAME)
	docker rm $(NGINX_CONTAINER_NAME)

# Optional: Run everything
docker-up: docker-create-network docker-build-run-app docker-build-run-nginx
docker-down: docker-remove-nginx docker-remove-app docker-remove-network

.PHONY: dev package docker-create-network docker-remove-network \
	docker-build-app docker-run-app docker-build-run-app docker-remove-app \
	docker-build-nginx docker-run-nginx docker-build-run-nginx docker-remove-nginx \
	docker-up docker-down