-- V6__users_role_to_role_id.sql
-- Asegurar extensiones útiles (no falla si ya están)
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- 1) Crear tabla roles si no existe
CREATE TABLE IF NOT EXISTS roles (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  description TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 2) Semilla de roles (no duplica gracias a ON CONFLICT)
INSERT INTO roles (name, description) VALUES
  ('SUPER_ADMIN', 'Super administrador con permisos totales'),
  ('ADMIN',       'Administrador del sistema'),
  ('TEACHER',     'Profesor'),
  ('STUDENT',     'Estudiante')
ON CONFLICT (name) DO NOTHING;

-- 3) Añadir columna role_id en users si no existe
ALTER TABLE users ADD COLUMN IF NOT EXISTS role_id BIGINT;

-- 4) Backfill: si existe columna legacy "role", mapear a role_id
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'users' AND column_name = 'role'
  ) THEN
    UPDATE users u
    SET role_id = r.id
    FROM roles r
    WHERE u.role_id IS NULL
      AND CAST(u.role AS TEXT) = r.name;  -- por si era enum o varchar

    -- Como fallback, asignar STUDENT a los que queden sin rol
    UPDATE users u
    SET role_id = r.id
    FROM roles r
    WHERE u.role_id IS NULL AND r.name = 'STUDENT';
  ELSE
    -- Si no hay columna "role" y role_id está NULL, también asegurar STUDENT
    UPDATE users u
    SET role_id = r.id
    FROM roles r
    WHERE u.role_id IS NULL AND r.name = 'STUDENT';
  END IF;
END$$;

-- 5) Añadir la FK si no existe
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'fk_users_role_id_roles'
  ) THEN
    ALTER TABLE users
      ADD CONSTRAINT fk_users_role_id_roles
      FOREIGN KEY (role_id) REFERENCES roles(id);
  END IF;
END$$;

-- 6) Índice a role_id (si no existe)
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);

-- 7) (Opcional) Enforzar NOT NULL cuando estés seguro de que todos tienen role_id
-- ALTER TABLE users ALTER COLUMN role_id SET NOT NULL;

-- 8) Borrar la columna antigua "role" si existe
ALTER TABLE users DROP COLUMN IF EXISTS role;

