FROM gradle:8.6-jdk21 AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:21

RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/butler_ai_service.jar

ENTRYPOINT ["java", "-jar", "/app/butler_ai_service.jar"]