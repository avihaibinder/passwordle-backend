# Passwordle Backend

This is the backend service for the Passwordle application.

## Prerequisites

- Java 17
- Maven

## How to Run

### Using Maven

You can run the application directly using Maven:

```bash
mvn clean spring-boot:run
```

The application will start on port 8080.

### Auto-Restart
I have added `spring-boot-devtools` to the project. This allows for faster restarts when you recompile files. 
- If using an IDE (IntelliJ/Eclipse), compiling a file should trigger a restart.
- If running from the command line, you still need to stop `Ctrl+C` and restart, but it will be faster.

### Using the Packaged JAR

First, package the application:

```bash
mvn package -DskipTests
```

Then run the JAR file:

```bash
java -jar target/passwordle-backend-0.0.1-SNAPSHOT.jar
```

## Health Check

Once running, you can verify the application status:

```
GET http://localhost:8080/health
```

Response: `{"status":"UP"}`
