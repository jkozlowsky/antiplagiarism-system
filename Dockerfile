FROM maven:3.9.14-eclipse-temurin-25-alpine AS builder
WORKDIR /app

# Kopiujemy cały projekt (wszystkie moduły)
COPY . .

# Budujemy wszystko naraz
RUN mvn clean package -Dmaven.test.skip=true

### ETAP URUCHOMIENIOWY ###
FROM eclipse-temurin:25-jdk-alpine
RUN apk add --no-cache curl
WORKDIR /app

# Argument, który powie Dockerowi, który z 3 modułów ma wrzucić do tego kontenera
ARG MODULE_NAME

# Wyciągamy wybudowany plik .jar z konkretnego modułu
COPY --from=builder /app/${MODULE_NAME}/target/*.jar app.jar

# Porty (odblokowujemy wszystkie używane przez nasze apki)
EXPOSE 8081 8082 8761

ENTRYPOINT ["java", "-jar", "app.jar"]