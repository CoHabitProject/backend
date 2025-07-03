# Requirements
- Java 17 +
- Maven 3.8.7 +

# Local Quick Start
## Maven
```bash
mvn clean install -DskipTests
mvn spring-boot:run -pl co-habit-application
```

## Spring (.jar)
```bash
mvn clean install -DskipTests
java -jar ./co-habit-application/target/co-habit-application-1.0-SNAPSHOT.jar
```

## DOCKER

### Build
```bash
docker-compose up -d
```

Puis dans IntelliJ, lancer le projet co-habit-application.

# Devops Launcher

Pour lancer l'infrastructure de développement, se référer au README.md du dossier devops.
https://github.com/CoHabitProject/devops
