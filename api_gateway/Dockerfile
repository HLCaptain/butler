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
ARG CHAT_API_URL=http://localhost:8082

ENV JWT_SECRET=$JWT_SECRET
ENV JWT_ISSUER=$JWT_ISSUER
ENV JWT_AUDIENCE=$JWT_AUDIENCE
ENV JWT_REALM=$JWT_REALM
ENV KTOR_PORT=$KTOR_PORT
ENV KTOR_DEVELOPMENT=$KTOR_DEVELOPMENT
ENV KTOR_DEFAULT_CONTENT_TYPE=$KTOR_DEFAULT_CONTENT_TYPE
ENV IDENTITY_API_URL=$IDENTITY_API_URL
ENV CHAT_API_URL=$CHAT_API_URL

EXPOSE $KTOR_PORT

RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/butler_backend.jar

ENTRYPOINT ["java", "-jar", "/app/butler_backend.jar"]