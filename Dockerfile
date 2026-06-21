FROM maven:3.9.14-eclipse-temurin-25-alpine AS builder
WORKDIR /app

COPY . .

RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:25-jdk-alpine
RUN apk add --no-cache curl
WORKDIR /app

ARG MODULE_NAME

COPY --from=builder /app/${MODULE_NAME}/target/*.jar app.jar

EXPOSE 8081 8082 8761 8888

ENTRYPOINT ["java", "-jar", "app.jar"]