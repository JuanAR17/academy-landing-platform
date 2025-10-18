-- Agregar columnas de perfil de usuario y nombre de usuario
ALTER TABLE users
  ADD COLUMN username VARCHAR(50) UNIQUE,
  ADD COLUMN nombre VARCHAR(255) NOT NULL,
  ADD COLUMN apellido VARCHAR(255),
  ADD COLUMN telefono VARCHAR(50),
  ADD COLUMN nacionalidad VARCHAR(100),
  ADD COLUMN direccion TEXT,
  ADD COLUMN donde_nos_viste VARCHAR(255),
  ADD COLUMN is_admin BOOLEAN DEFAULT FALSE;

-- Crear índice para búsqueda por username
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
