FROM gradle:8.11-jdk21 AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:23

ARG JWT_SECRET=your_jwt_secret
ARG JWT_ISSUER=yout_jwt_issuer
ARG JWT_AUDIENCE=your_jwt_audience
ARG JWT_REALM=your_jwt_realm
ARG KTOR_PORT=8080
ARG KTOR_DEVELOPMENT=false
ARG KTOR_DEFAULT_CONTENT_TYPE=application/json

ENV JWT_SECRET=$JWT_SECRET
ENV JWT_ISSUER=$JWT_ISSUER
ENV JWT_AUDIENCE=$JWT_AUDIENCE
ENV JWT_REALM=$JWT_REALM
ENV KTOR_PORT=$KTOR_PORT
ENV KTOR_DEVELOPMENT=$KTOR_DEVELOPMENT
ENV KTOR_DEFAULT_CONTENT_TYPE=$KTOR_DEFAULT_CONTENT_TYPE

EXPOSE $KTOR_PORT

RUN mkdir /app
COPY --from=build /home/gradle/src/server/build/libs/*.jar /app/butler_server.jar

ENTRYPOINT ["java", "-jar", "/app/butler_server.jar"]