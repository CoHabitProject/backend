FROM eclipse-temurin:23-jdk

# Set working directory
WORKDIR /app

# Copy pom files first
COPY pom.xml ./
COPY co-habit-application/pom.xml ./co-habit-application/
COPY co-habit-security/pom.xml ./co-habit-security/
COPY co-habit-rest/pom.xml ./co-habit-rest/
COPY co-habit-domain/pom.xml ./co-habit-domain/
COPY co-habit-persistence/pom.xml ./co-habit-persistence/
COPY co-habit-web/pom.xml ./co-habit-web/

# Copy source code
COPY src/ src/
COPY co-habit-application/ co-habit-application/
COPY co-habit-security/ co-habit-security/
COPY co-habit-rest/ co-habit-rest/
COPY co-habit-domain/ co-habit-domain/
COPY co-habit-persistence/ co-habit-persistence/
COPY co-habit-web/ co-habit-web/

# Package the application using the Maven executable from the container
RUN ./mvnw clean install -DskipTests || \
    apt-get update && \
    apt-get install -y maven && \
    mvn clean install -DskipTests

# Expose the port the app runs on
EXPOSE 8081

# Run the application
CMD ["java", "-jar", "co-habit-application/target/co-habit-application-1.0-SNAPSHOT.jar"]