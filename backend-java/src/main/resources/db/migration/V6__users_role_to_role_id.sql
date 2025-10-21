-- V6__users_role_to_role_id.sql
-- Normaliza 'users.role' -> 'users.role_id' (FK a roles.id). Idempotente.

-- 1) Asegura columna role_id
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema='public' AND table_name='users' AND column_name='role_id'
  ) THEN
    ALTER TABLE users ADD COLUMN role_id BIGINT;
  END IF;
END $$;

-- 2) Si existe la columna antigua 'role', migra sus valores a role_id
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema='public' AND table_name='users' AND column_name='role'
  ) THEN
    -- Primero intenta mapear por nombre (roles.name = 'SUPER_ADMIN'/'ADMIN'/...)
    UPDATE users u
    SET role_id = r.id
    FROM roles r
    WHERE u.role_id IS NULL
      AND UPPER(u.role::text) = r.name;

    -- (Opcional) Si alguien guardó números en 'role', mapea por id de forma segura
    UPDATE users u
    SET role_id = r.id
    FROM roles r
    WHERE u.role_id IS NULL
      AND (u.role::text ~ '^[0-9]+$')
      AND (u.role::bigint) = r.id;

    -- Fallback: cualquier fila que siga NULL, pásala a STUDENT
    UPDATE users
      SET role_id = (SELECT id FROM roles WHERE name = 'STUDENT' LIMIT 1)
    WHERE role_id IS NULL;

    -- Quita la columna vieja
    ALTER TABLE users DROP COLUMN IF EXISTS role;
  END IF;
END $$;

-- 3) Crea la FK si no existe
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conrelid = 'public.users'::regclass
      AND contype  = 'f'
      AND conname  = 'users_role_id_fkey'
  ) THEN
    ALTER TABLE users
      ADD CONSTRAINT users_role_id_fkey
      FOREIGN KEY (role_id) REFERENCES roles(id);
  END IF;
END $$;

-- 4) NOT NULL sólo si ya no quedan nulos
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM users WHERE role_id IS NULL) THEN
    ALTER TABLE users ALTER COLUMN role_id SET NOT NULL;
  END IF;
END $$;

-- 5) Índice útil
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);

