# Étape 1 : build Maven
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# Copie les sources et le pom
COPY . .

# Compile et package l'application sans les tests
RUN mvn clean package -DskipTests

# Étape 2 : image exécutable avec JDK uniquement
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copie du JAR généré depuis l'étape précédente
COPY --from=build /app/co-habit-application/target/*.jar app.jar

# Expose le port par défaut de Spring Boot
EXPOSE 8080

# Commande de lancement
ENTRYPOINT ["java", "-jar", "app.jar"]
