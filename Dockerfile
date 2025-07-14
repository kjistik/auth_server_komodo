# Use the official OpenJDK 21 base image
FROM openjdk:21-jdk-slim as build

# Set the working directory
WORKDIR /app

# Copy the Maven or Gradle wrapper and application code
COPY . .

# Build the Spring Boot application
RUN ./mvnw clean package -DskipTests

# Use the OpenJDK base image to run the application
FROM openjdk:21-jdk-slim

# Create a non-root user for security
RUN addgroup --system spring && adduser --system --no-create-home --ingroup spring springuser

# Create directory for keys with proper permissions
RUN mkdir -p /secrets && chown springuser:spring /secrets && chmod 0500 /secrets

RUN addgroup --system spring && adduser --system --no-create-home --ingroup spring springuser
RUN chown -R springuser:spring /secrets
RUN chmod 0500 /secrets  # Read/execute for owner only

# Set the working directory
WORKDIR /app

# Copy the JAR file
COPY --from=build --chown=springuser:spring /app/target/auth_server-1.1.1-RELEASE.jar /app/app.jar

# Copy the wait-for-it script
COPY --chown=springuser:spring ./wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# Expose ports
EXPOSE 8080 5006

# Switch to non-root user
USER springuser

# Set environment variables (no values - to be provided at runtime)
ENV KOMODO_JWT_PRIVATE_KEY_PATH=/secrets/ec-private-key.pem
ENV KOMODO_JWT_PUBLIC_KEY=""
ENV KOMODO_JWT_EXPIRATION_TIME=""
ENV KOMODO_JWT_EMAIL_VERIFICATION_TIME=""
ENV KOMODO_MAILGUN_KEY=""
ENV KOMODO_MAILGUN_DOMAIN=""
ENV KOMODO_DATASOURCE_URL=""
ENV KOMODO_DATASOURCE_USER=""
ENV KOMODO_DATASOURCE_PASSWORD=""
ENV KOMODO_DOMAIN_URL=""

# Entrypoint with debugging enabled
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006", "-jar", "/app/app.jar"]