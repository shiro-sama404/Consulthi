FROM maven:3-eclipse-temurin-23 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Executa o build (o .jar será gerado em /app/target/)
RUN mvn clean package -DskipTests

FROM eclipse-temurin:23-jre

WORKDIR /app

COPY --from=builder /app/target/Consulthi-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]