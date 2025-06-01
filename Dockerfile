# Stage 1: Cache Gradle dependencies
FROM gradle:latest AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME=/home/gradle/cache_home
COPY --chown=gradle:gradle /server/ /build-logic/ /gradle/ /home/gradle/src/
WORKDIR /home/gradle/src
RUN gradle :server:dependencies --no-daemon || true

# Stage 2: Build Application
FROM gradle:latest AS build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle :server:buildFatJar --no-daemon

# Stage 3: Create the Runtime Image
FROM eclipse-temurin:23 AS runtime
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/server/build/libs/*.jar /app/butler_server.jar
ENTRYPOINT ["java", "-jar", "/app/butler_server.jar"]
