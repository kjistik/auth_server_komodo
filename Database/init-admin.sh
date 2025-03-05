#!/bin/sh
set -e

# Read environment variables
ADMIN_USERNAME=${KOMODO_ADMIN_USERNAME}
ADMIN_EMAIL=${KOMODO_ADMIN_EMAIL}
ADMIN_GIVENNAME=${KOMODO_ADMIN_GIVENNAME}
ADMIN_LASTNAME=${KOMODO_ADMIN_LASTNAME}
ADMIN_PASSWORD=${KOMODO_ADMIN_PASSWORD}



# Validate that all required environment variables are set
if [ -z "$ADMIN_USERNAME" ] || [ -z "$ADMIN_EMAIL" ] || [ -z "$ADMIN_GIVENNAME" ] || [ -z "$ADMIN_LASTNAME" ] || [ -z "$ADMIN_PASSWORD" ]; then
    echo "Error: One or more required environment variables are missing."
    echo "Please provide the following environment variables:"
    echo "  - KOMODO_ADMIN_USERNAME"
    echo "  - KOMODO_ADMIN_EMAIL"
    echo "  - KOMODO_ADMIN_GIVENNAME"
    echo "  - KOMODO_ADMIN_LASTNAME"
    echo "  - KOMODO_ADMIN_PASSWORD"
    exit 1
fi

# Generate a bcrypt-hashed password
HASHED_PASSWORD=$(htpasswd -bnBC 10 "" "$ADMIN_PASSWORD" | tr -d ':\n')

# Insert the admin user into the database and fetch the user ID
ADMIN_USER_ID=$(psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -Atq <<-EOSQL
    INSERT INTO "user" (id, email, givenName, lastName, userName, password, confirmed)
    VALUES (
        uuid_generate_v4(), -- Generate a UUID for the id
        '$ADMIN_EMAIL',     -- Use the ADMIN_EMAIL environment variable
        '$ADMIN_GIVENNAME', -- Use the ADMIN_GIVENNAME environment variable
        '$ADMIN_LASTNAME',  -- Use the ADMIN_LASTNAME environment variable
        '$ADMIN_USERNAME',  -- Use the ADMIN_USERNAME environment variable
        '$HASHED_PASSWORD', -- Use the hashed password
        TRUE                -- Mark the user as confirmed
    )
    ON CONFLICT (email) DO NOTHING
    RETURNING id;
EOSQL
)

# Check if the admin user was created successfully
if [ -z "$ADMIN_USER_ID" ]; then
    echo "Admin user already exists or failed to create."
    exit 1
fi

echo "Admin user created with ID: $ADMIN_USER_ID"

# Fetch all role IDs from the role table
ROLE_IDS=$(psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -Atq -c "SELECT id FROM role;")

# Check if roles exist
if [ -z "$ROLE_IDS" ]; then
    echo "Error: No roles found in the 'role' table."
    exit 1
fi

# Assign all roles to the admin user
echo "$ROLE_IDS" | while read -r ROLE_ID; do
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        INSERT INTO "user_role" (user_id, role_id)
        VALUES ('$ADMIN_USER_ID', '$ROLE_ID')
        ON CONFLICT DO NOTHING;
EOSQL
    echo "Assigned role with ID $ROLE_ID to admin user."
done

echo "All roles assigned to the admin user."
