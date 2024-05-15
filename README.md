# Butler AI Chat App

Butler is an AI chat app, which covers all fronts of development, from frontend to backend, and from design to deployment.

## Installation

### Backend Orchestration

The backend is organized into microservices. Each microservice is a separate project and has a `Dockerfile` to build a Docker image. Kubernetes can be used to deploy the Docker images.

#### Secrets

You have to create a `butler-secret.yaml` `kind: Secret` file with the following keys and values:

```yaml
data:
    JWT_SECRET: cGFzc3dvcmQ= # "password" in base64
    POSTGRES_PASSWORD: cGFzc3dvcmQ= # "password" in base64
    REDIS_PASSWORD: cGFzc3dvcmQ= # "password" in base64
    PGADMIN_DEFAULT_PASSWORD: cGFzc3dvcmQ= # "password" in base64
```

#### Deployment to Kubernetes

Run the following commands to build the Docker images and deploy them to Kubernetes:

```sh
# Start minikube (should set kubectl to use minikube's context)
minikube start
minikube addons enable ingress

minikube docker-env
# Set the Docker environment variables to the minikube Docker daemon
eval $(minikube docker-env)
# or `& minikube -p minikube docker-env --shell powershell | Invoke-Expression` for Windows PowerShell

# Build the Docker images (in the correct minikube docker-env)
docker-compose build

# Load images

# Deploy the config files to Kubernetes
kubectl apply -f butler-configmap.yaml
kubectl apply -f butler-secret.yaml # You have to create this file
kubectl apply -f api_gateway/deployment.yaml
kubectl apply -f api_gateway/service.yaml
# kubectl apply -f services/localai/deployment.yaml # Uncomment if you want self-hosted AI
kubectl apply -f services/postgresql/postgresql-statefulset.yaml
kubectl apply -f services/postgresql/pgadmin-deployment.yaml
kubectl apply -f services/redis/redis-configmap.yaml
kubectl apply -f services/redis/redis-statefulset.yaml
kubectl apply -f services/redis/sentinel-statefulset.yaml
kubectl apply -f services/chat_service/deployment.yaml
kubectl apply -f services/chat_service/service.yaml
kubectl apply -f services/identity_service/deployment.yaml
kubectl apply -f services/identity_service/service.yaml
kubectl apply -f services/ai_service/deployment.yaml
kubectl apply -f services/ai_service/service.yaml

# Separate namespace for monitoring
kubectl create namespace monitoring
kubectl apply -f services/prometheus/clusterRole.yaml
kubectl apply -f services/prometheus/config-map.yaml
kubectl apply -f services/prometheus/prometheus-deployment.yaml
kubectl apply -f services/prometheus/prometheus-service.yaml
kubectl apply -f services/grafana/grafana-datasource-config.yaml
kubectl apply -f services/grafana/deployment.yaml
kubectl apply -f services/grafana/service.yaml

# For Jaeger and OpenTelemetry
# Install cert manager needed for Jaeger
kubectl create namespace observability
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.14.5/cert-manager.yaml
kubectl apply -n observability -f https://github.com/jaegertracing/jaeger-operator/releases/download/v1.57.0/jaeger-operator.yaml
# 5 second delay in PowerShell
Start-Sleep -Seconds 5.0
kubectl apply -n observability -f services/jaeger/simplest.yaml
```

To expose the API Gateway's and pgAdmin's port, use command

```sh
minikube service butler-api-gateway-service
minikube service pgadmin
minikube service prometheus-service --namespace monitoring
minikube service grafana --namespace monitoring
minikube service
```

#### Monitoring

To monitor PostgreSQL, check the exposed url got from running `minikube service pgadmin` and login with the credentials, found in `butler-configmap.yaml` and `butler-secret.yaml` (created by you). Then you can connect to your database on port `5432` with the credentials found in `butler-secret.yaml`. Keep in mind, that you may have to modify the PostgreSQL superuser in a pod's shell with the following command:

```sh
psql -U postgres
\password postgres
# Enter the new password
```

### Frontend

To build and run the Compose Multiplatform app, you should have a `local.properties` file in the `client` directory with the following content:

```properties
RELEASE_KEYSTORE_PASSWORD=your_release_keystore_password
RELEASE_KEY_PASSWORD=your_release_key_password
RELEASE_KEY_ALIAS=your_release_key_alias
RELEASE_KEY_PATH=path/to/release.keystore

DEBUG_KEYSTORE_PASSWORD=your_debug_keystore_password
DEBUG_KEY_PASSWORD=your_debug_key_password
DEBUG_KEY_ALIAS=your_debug_key_alias
DEBUG_KEY_PATH=path/to/debug.keystore

API_GATEWAY_URL=http://127.0.0.1:12345
```

## Usage

## Contributing

See more information in [CONTRIBUTING.md](CONTRIBUTING.md).

To reset deployments and images, the following commands should help:

```sh
# Delete the deployments
kubectl delete -n default deployment butler-api-gateway
kubectl delete -n default deployment butler-chat-service
kubectl delete -n default deployment butler-identity-service
kubectl delete -n default deployment butler-ai-service

# Delete the images
minikube image rm illyan1337/butler-api-gateway:latest
minikube image rm illyan1337/butler-chat-service:latest
minikube image rm illyan1337/butler-identity-service:latest
minikube image rm illyan1337/butler-ai-service:latest

# Build the images again
docker-compose build

# Redeploy the microservices
kubectl apply -f api_gateway/deployment.yaml
kubectl apply -f services/chat_service/deployment.yaml
kubectl apply -f services/identity_service/deployment.yaml
kubectl apply -f services/ai_service/deployment.yaml
```

## License

[GPLv3](https://www.gnu.org/licenses/gpl-3.0.html)
