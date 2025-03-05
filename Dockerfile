# Use the official OpenJDK 21 base image
FROM openjdk:21-jdk-slim as build

# Set the working directory
WORKDIR /app

# Copy the Maven or Gradle wrapper and application code
COPY . .

# Build the Spring Boot application (assuming you are using Maven)
RUN ./mvnw clean package -DskipTests

# Use the OpenJDK base image to run the application
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file built in the previous step
COPY --from=build /app/target/auth_server-0.0.1-SNAPSHOT.jar your-app.jar

# Copy the wait-for-it script into the container
COPY ./wait-for-it.sh /wait-for-it.sh

# Make the script executable
RUN chmod +x /wait-for-it.sh

# Set environment variables
ENV KOMODO_JWT_SECRET_KEY=${KOMODO_JWT_SECRET_KEY}
ENV KOMODO_JWT_EXPIRATION_TIME=${KOMODO_JWT_EXPIRATION_TIME}
ENV KOMODO_JWT_EMAIL_VERIFICATION_TIME=${KOMODO_JWT_EMAIL_VERIFICATION_TIME}
ENV KOMODO_MAILGUN_KEY=${KOMODO_MAILGUN_KEY}
ENV KOMODO_MAILGUN_DOMAIN=${KOMODO_MAILGUN_DOMAIN}
ENV KOMODO_DATASOURCE_URL=${KOMODO_DATASOURCE_URL}
ENV KOMODO_DATASOURCE_USER=${KOMODO_DATASOURCE_USER}
ENV KOMODO_DATASOURCE_PASSWORD=${KOMODO_DATASOURCE_PASSWORD}
ENV KOMODO_DOMAIN_URL=${KOMODO_DOMAIN_URL}

# Command to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/your-app.jar"]
