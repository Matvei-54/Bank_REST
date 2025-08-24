FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml /workspace
COPY src /workspace/src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-alpine
WORKDIR /application
COPY --from=build /workspace/target/*.jar app.jar
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown -R appuser:appgroup /application
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]