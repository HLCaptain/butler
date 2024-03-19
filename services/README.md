# Butler AI Backend

Backend for Butler AI chat application. Implements Microservice architecture.

## Usage

Each service has its own `Dockerfile` to build a Docker image. Kubernetes can be used to deploy the Docker images.

## Persistence

Redis and PostgreSQL are used for persistence.

Services communicating with databases should implement a reactive cache subscribing to a topic on top of Redis and a database interface interacting with PostgreSQL.
