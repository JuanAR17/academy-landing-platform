CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE IF NOT EXISTS leads(
  id          BIGSERIAL PRIMARY KEY,
  ts_utc      TIMESTAMPTZ NOT NULL,
  email       VARCHAR(320) NOT NULL,
  name        VARCHAR(255) NOT NULL,
  courses_json TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS users(
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email        CITEXT UNIQUE NOT NULL,
  username     VARCHAR(50) UNIQUE,
  password_hash TEXT NOT NULL,
  nombre       VARCHAR(255),
  apellido     VARCHAR(255),
  telefono     VARCHAR(50),
  nacionalidad VARCHAR(100),
  address_id   BIGINT,
  donde_nos_viste VARCHAR(255),
  is_admin     BOOLEAN DEFAULT FALSE,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS sessions(
  id                 UUID PRIMARY KEY,
  user_id            UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  refresh_token_hash BYTEA UNIQUE NOT NULL,
  created_at         TIMESTAMPTZ NOT NULL,
  last_used_at       TIMESTAMPTZ,
  expires_at         TIMESTAMPTZ NOT NULL,
  is_revoked         BOOLEAN NOT NULL DEFAULT FALSE,
  ip                 INET,
  user_agent         TEXT
);

CREATE INDEX IF NOT EXISTS idx_sessions_active_exp
  ON sessions(expires_at)
  WHERE is_revoked = false;

-- Crear índice para búsqueda por username
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Crear tabla addresses
CREATE TABLE IF NOT EXISTS addresses (
    id BIGSERIAL PRIMARY KEY,
    direccion TEXT NOT NULL,
    ciudad VARCHAR(255),
    departamento VARCHAR(255),
    pais VARCHAR(255)
);

-- Agregar foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_users_address 
  FOREIGN KEY (address_id) REFERENCES addresses(id);

-- Migrar datos existentes si la columna direccion existe (para upgrades)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'users' AND column_name = 'direccion') THEN
        
        -- Insertar direcciones existentes en tabla addresses
        INSERT INTO addresses (direccion)
        SELECT DISTINCT direccion
        FROM users
        WHERE direccion IS NOT NULL AND direccion != '';
        
        -- Actualizar users con address_id
        UPDATE users
        SET address_id = a.id
        FROM addresses a
        WHERE users.direccion = a.direccion;
        
        -- Eliminar columna antigua
        ALTER TABLE users DROP COLUMN direccion;
    END IF;
END $$;