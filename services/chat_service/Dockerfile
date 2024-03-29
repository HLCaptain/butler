FROM gradle:8.6-jdk21 AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:21

ARG JWT_SECRET=your_jwt_secret
ARG JWT_ISSUER=yout_jwt_issuer
ARG JWT_AUDIENCE=your_jwt_audience
ARG JWT_REALM=your_jwt_realm
ARG KTOR_PORT=8080
ARG KTOR_DEVELOPMENT=true
ARG KTOR_DEFAULT_CONTENT_TYPE=application/json
ARG IDENTITY_API_URL=http://localhost:8081
ARG DATABASE_URL=jdbc:postgresql://postgresql:5432/butlerdb
ARG DATABASE_NAME=butlerdb
ARG DATABASE_USER=butler
ARG DATABASE_PASSWORD=butler
ARG DATABASE_DRIVER=org.postgresql.Driver

ENV JWT_SECRET=$JWT_SECRET
ENV JWT_ISSUER=$JWT_ISSUER
ENV JWT_AUDIENCE=$JWT_AUDIENCE
ENV JWT_REALM=$JWT_REALM
ENV KTOR_PORT=$KTOR_PORT
ENV KTOR_DEVELOPMENT=$KTOR_DEVELOPMENT
ENV KTOR_DEFAULT_CONTENT_TYPE=$KTOR_DEFAULT_CONTENT_TYPE
ENV IDENTITY_API_URL=$IDENTITY_API_URL
ENV DATABASE_URL=$DATABASE_URL
ENV DATABASE_NAME=$DATABASE_NAME
ENV DATABASE_USER=$DATABASE_USER
ENV DATABASE_PASSWORD=$DATABASE_PASSWORD
ENV DATABASE_DRIVER=$DATABASE_DRIVER

EXPOSE $KTOR_PORT

RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/butler_chat_service.jar

ENTRYPOINT ["java", "-jar", "/app/butler_chat_service.jar"]