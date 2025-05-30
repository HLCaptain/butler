FROM gradle:8.14-jdk21 AS build

COPY --chown=gradle:gradle ../.. /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:23

EXPOSE 8080

RUN mkdir /app
COPY --from=build /home/gradle/src/server/build/libs/*.jar /app/butler_server.jar

ENTRYPOINT ["java", "-jar", "/app/butler_server.jar"]