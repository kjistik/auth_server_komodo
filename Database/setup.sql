-- Switch to the correct database
\c komodo;

-- Enable UUID support
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create the user table
CREATE TABLE IF NOT EXISTS "user" (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    givenName VARCHAR(100) NOT NULL,
    lastName VARCHAR(100) NOT NULL,
    userName VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    confirmed BOOLEAN DEFAULT FALSE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modifiedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create the role table
CREATE TABLE IF NOT EXISTS "role" (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    role VARCHAR(20) UNIQUE NOT NULL -- Ensure role names are unique
);

-- Insert default roles if they don't already exist
INSERT INTO "role" (id, role)
VALUES
    (uuid_generate_v4(), 'USER')
    ON CONFLICT (role) DO NOTHING;

INSERT INTO "role" (id, role)
VALUES
    (uuid_generate_v4(), 'SUPPORT')
    ON CONFLICT (role) DO NOTHING;

INSERT INTO "role" (id, role)
VALUES
    (uuid_generate_v4(), 'ADMIN')
    ON CONFLICT (role) DO NOTHING;

INSERT INTO "role" (id, role)
VALUES
    (uuid_generate_v4(), 'OWNER')
    ON CONFLICT (role) DO NOTHING;

-- Create the user_role table to manage the many-to-many relationship
CREATE TABLE IF NOT EXISTS "user_role" (
    user_id UUID REFERENCES "user"(id) ON DELETE CASCADE,
    role_id UUID REFERENCES "role"(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id) -- Composite primary key to ensure uniqueness
);

-- Create an index on the confirmed column for login performance optimization
CREATE INDEX IF NOT EXISTS idx_user_confirmed ON "user"(confirmed);

-- Drop existing triggers if they exist (in case of updates to the schema)
-- This ensures that the trigger definitions can be updated without causing errors.
DROP TRIGGER IF EXISTS enforce_uuid_generation_user ON "user";
DROP TRIGGER IF EXISTS enforce_uuid_generation_role ON "role";
DROP TRIGGER IF EXISTS update_user_modifiedAt ON "user";

-- Create a function to enforce UUID generation for any table
CREATE OR REPLACE FUNCTION enforce_uuid_generation()
RETURNS TRIGGER AS $$
BEGIN
    NEW.id := uuid_generate_v4(); -- Override the id with a new UUID
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create a function to update the modifiedAt column for any table
CREATE OR REPLACE FUNCTION update_modifiedAt()
RETURNS TRIGGER AS $$
BEGIN
    NEW.modifiedAt := CURRENT_TIMESTAMP; -- Update modifiedAt to the current timestamp
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for the user table
CREATE TRIGGER enforce_uuid_generation_user
BEFORE INSERT ON "user"
FOR EACH ROW
EXECUTE FUNCTION enforce_uuid_generation();

CREATE TRIGGER update_user_modifiedAt
BEFORE UPDATE ON "user"
FOR EACH ROW
EXECUTE FUNCTION update_modifiedAt();

-- Create triggers for the role table
CREATE TRIGGER enforce_uuid_generation_role
BEFORE INSERT ON "role"
FOR EACH ROW
EXECUTE FUNCTION enforce_uuid_generation();