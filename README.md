# Butler AI Chat App

Butler is an AI chat app, which covers all fronts of development, from frontend to backend, and from design to deployment.

## Installation

### Backend Orchestration

The backend is organized into microservices. Each microservice is a separate project and has a `Dockerfile` to build a Docker image. Kubernetes can be used to deploy the Docker images.

#### Secrets

You have to create a `butler-secrets.yaml` `kind: Secret` file with the following keys and values:

```yaml
data:
    JWT_SECRET: asdf # your base64 encoded secret
    POSTGRES_PASSWORD: asdf # your password
```

Run the following commands to build the Docker images and deploy them to Kubernetes:

```sh
# Build the Docker images
docker-compose build

# Start minikube (should set kubectl to use minikube's context)
minikube start
minikube addons enable ingress

# Set the Docker environment variables to the minikube Docker daemon
eval $(minikube docker-env)
# or `& minikube -p minikube docker-env --shell powershell | Invoke-Expression` for Windows PowerShell

# Deploy the config files to Kubernetes
kubectl apply -f butler-configmap.yaml
kubectl apply -f butler-secrets.yaml # You have to create this file
kubectl apply -f services/postgresql-claim.yaml
kubectl apply -f services/postgresql-deployment.yaml
kubectl apply -f services/postgresql-pv.yaml
kubectl apply -f services/postgresql-service.yaml
kubectl apply -f services/postgresql-pv.yaml
kubectl apply -f services/chat_service/deployment.yaml
kubectl apply -f services/chat_service/service.yaml
```

## Usage

## Contributing

See more information in [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[GPLv3](https://www.gnu.org/licenses/gpl-3.0.html)
