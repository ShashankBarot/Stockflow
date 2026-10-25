# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S -g 10001 stockflow && adduser -S -u 10001 -G stockflow stockflow
COPY --from=build --chown=stockflow:stockflow /app/target/*.jar app.jar
USER stockflow
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
