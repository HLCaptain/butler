FROM gradle:8.5-jdk21 AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:21

ARG PORT=8080
ARG JWT_SECRET=your_jwt_secret
ARG JWT_ISSUER=yout_jwt_issuer
ARG JWT_AUDIENCE=your_jwt_audience
ARG JWT_REALM=your_jwt_realm
ARG KTOR_DEVELOPMENT=true
ARG KTOR_DEFAULT_CONTENT_TYPE=application/json

ENV PORT=$PORT
ENV JWT_SECRET=$JWT_SECRET
ENV JWT_ISSUER=$JWT_ISSUER
ENV JWT_AUDIENCE=$JWT_AUDIENCE
ENV JWT_REALM=$JWT_REALM
ENV KTOR_DEVELOPMENT=$KTOR_DEVELOPMENT
ENV KTOR_DEFAULT_CONTENT_TYPE=$KTOR_DEFAULT_CONTENT_TYPE

EXPOSE $PORT:$PORT

RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/ktor-service.jar

ENTRYPOINT ["java", "-jar", "/app/ktor-service.jar"]