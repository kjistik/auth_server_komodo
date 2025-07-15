# Use a multi-stage build for efficiency and smaller final image size.

# --- Build Stage ---
FROM openjdk:21-jdk-slim as build

# Set the working directory inside the build container
WORKDIR /app

# Copy Maven wrapper files first to leverage Docker layer caching.
# This ensures that if only source code changes, Maven dependencies aren't re-downloaded.
COPY mvnw .
COPY .mvn .mvn

# Copy the pom.xml (or build.gradle) next for dependency resolution
COPY pom.xml .

# Pre-download dependencies by running a compile command.
# This layer will only be invalidated if pom.xml changes.
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -B

# Copy the rest of the application code
COPY src ./src

# Build the Spring Boot application.
# Using --mount=type=cache for the Maven repository to speed up subsequent builds.
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package -DskipTests

# --- Run Stage ---
# Use a smaller JRE-only image for the final runtime, as we don't need the full JDK.
FROM openjdk:21-jre-slim

# Define arguments for the JAR file name and version for easier management
ARG JAR_NAME=auth_server
ARG JAR_VERSION=1.1.1-RELEASE

# Create a non-root user and group for security best practices.
# Using a single RUN command for efficiency and smaller image layers.
RUN addgroup --system spring && adduser --system --no-create-home --ingroup spring springuser

# Create directory for secrets/keys with proper permissions.
# Ownership and permissions are set in a single command.
# 0500 means owner can read/execute, no permissions for group/others.
RUN mkdir -p /secrets && chown springuser:spring /secrets && chmod 0500 /secrets

# Set the working directory for the application
WORKDIR /app

# Copy the JAR file from the build stage to the run stage.
# Use ARG variables for the JAR name.
# Ensure correct ownership.
COPY --from=build --chown=springuser:spring /app/target/${JAR_NAME}-${JAR_VERSION}.jar /app/app.jar

# Copy the wait-for-it script and make it executable.
# Ensure correct ownership.
COPY --chown=springuser:spring ./wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# Expose ports that the application listens on
# Removed 5006 as it's no longer used for debugging.
EXPOSE 8080

# Switch to the non-root user for running the application.
USER springuser

# Set environment variables that the application expects.
# All values are left empty, enforcing that they MUST be provided at runtime.

# Server settings
ENV SERVER_PORT=""

# Spring Cloud Vault settings
ENV VAULT_ADDR=""
ENV VAULT_AUTH_METHOD=""
ENV VAULT_ROLE_ID=""
ENV VAULT_SECRET_ID=""
ENV VAULT_TRANSIT_KEY_PATH=""
ENV VAULT_TRANSIT_SIGN_PATH=""

# Spring Data Redis settings
ENV KOMODO_REDIS_HOST=""
ENV KOMODO_REDIS_PORT=""

# R2DBC Datasource settings
ENV KOMODO_DATASOURCE_URL=""
ENV KOMODO_DATASOURCE_USER=""
ENV KOMODO_DATASOURCE_PASSWORD=""

# Komodo general settings
ENV KOMODO_DOMAIN_URL=""

# JWT settings
ENV KOMODO_JWT_EXPIRATION_TIME=""
ENV KOMODO_JWT_EMAIL_VERIFICATION_TIME=""
ENV KOMODO_REFRESH_TOKEN_EXPIRATION_TIME=""

# Mailgun settings
ENV KOMODO_MAILGUN_KEY=""
ENV KOMODO_MAILGUN_DOMAIN=""

# Komodo Admin settings
ENV KOMODO_ADMIN_EMAIL=""
ENV KOMODO_ADMIN_PASSWORD=""
ENV KOMODO_ADMIN_GIVENNAME=""
ENV KOMODO_ADMIN_LASTNAME=""

# Entrypoint to run the Spring Boot application.
# The remote debugging arguments are removed.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]