# Butler Chat Service

## Description

## Architecture

Ktor -> Redis -> Postgres

Routing -> DataSource access/updates Redis cache -> Writing to Postgres

1. `configureRouting()`
2. In `configureRouting()`, we have `chatRoutes()` where we list all the routes.
3. In `chatRoutes()`, we are serving requests by a `ChatService` class, which implements caching (Redis) and database access (Postgres).

We may listen to or poll Redis/Postgres for updates (e.g. new messages) and update the cache accordingly.