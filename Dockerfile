# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy the pom.xml file to download dependencies
COPY pom.xml .
# Download all required dependencies into one layer
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src
# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/passwordle-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
