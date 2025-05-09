# Requirements
- Java 17 +
- Maven 3.8.7 +

# Quick Start
### Maven 
```bash
mvn clean install -DskipTests
mvn spring-boot:run -pl co-habit-application
```

### Spring (.jar)
```bash
mvn clean install -DskipTests
java -jar ./co-habit-application/target/co-habit-application-1.0-SNAPSHOT.jar 
```

# DOCKER

### Build
docker build -t co-habit-app .

### Run 
docker run -p 8081:8081 co-habit-app

### Run (with network)
docker run --network co-habit-network -p 8081:8081 co-habit-app