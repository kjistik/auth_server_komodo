# Use the official PostgreSQL Alpine image
FROM postgres:17.4-alpine3.21

# Set environment variables for PostgreSQL configuration
ENV POSTGRES_DB=
ENV POSTGRES_USER=
ENV POSTGRES_PASSWORD=

# Environment variables for the admin user (no default values)
ENV KOMODO_ADMIN_USERNAME=
ENV KOMODO_ADMIN_EMAIL=
ENV KOMODO_ADMIN_GIVENNAME=
ENV KOMODO_ADMIN_LASTNAME=
ENV KOMODO_ADMIN_PASSWORD=

# Install apache2-utils (contains `htpasswd` for bcrypt hashing)
RUN apk add --no-cache apache2-utils

# Copy the setup.sql script and init-admin.sh script
COPY setup.sql /docker-entrypoint-initdb.d/a.sql
COPY init-admin.sh /docker-entrypoint-initdb.d/

# Make the init-admin.sh script executable
RUN chmod +x /docker-entrypoint-initdb.d/init-admin.sh

# Run PostgreSQL
CMD ["postgres"]
