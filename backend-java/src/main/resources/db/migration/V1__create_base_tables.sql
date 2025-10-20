-- V1__create_base_tables.sql  (versión idempotente)
-- Extensiones necesarias (seguras si ya existen)
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- Asegurar tabla roles (si no existe se crea con default correcto)
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Si la tabla ya existía sin la columna o sin default, lo corregimos:
DO $$
BEGIN
  -- Agregar columna si no existe
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
       WHERE table_name = 'roles' AND column_name = 'created_at'
  ) THEN
    ALTER TABLE roles ADD COLUMN created_at TIMESTAMP;
  END IF;

  -- Poner default (idempotente aunque ya lo tenga)
  EXECUTE 'ALTER TABLE roles ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP';

  -- Rellenar nulos existentes
  EXECUTE 'UPDATE roles SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL';

  -- Asegurar NOT NULL
  EXECUTE 'ALTER TABLE roles ALTER COLUMN created_at SET NOT NULL';
END $$;

-- Índice/único por si la tabla vieja no lo tenía
CREATE UNIQUE INDEX IF NOT EXISTS uk_roles_name ON roles(name);

-- Seed de roles (no falla si ya están)
INSERT INTO roles (name, description) VALUES
('SUPER_ADMIN', 'Super administrador con permisos totales'),
('ADMIN', 'Administrador del sistema'),
('TEACHER', 'Profesor'),
('STUDENT', 'Estudiante')
ON CONFLICT (name) DO NOTHING;

-- COUNTRIES
CREATE TABLE IF NOT EXISTS countries (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    phone_code VARCHAR(10),
    emoji VARCHAR(10),
    nationality VARCHAR(100),
    numeric_code VARCHAR(10),
    currency VARCHAR(10),
    currency_name VARCHAR(100),
    currency_symbol VARCHAR(10)
);

-- STATES
CREATE TABLE IF NOT EXISTS states (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    country_id BIGINT NOT NULL REFERENCES countries(id)
);

-- CITIES
CREATE TABLE IF NOT EXISTS cities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    state_id BIGINT NOT NULL REFERENCES states(id)
);

-- ADDRESSES
CREATE TABLE IF NOT EXISTS addresses (
    id BIGSERIAL PRIMARY KEY,
    address TEXT NOT NULL,
    city_id BIGINT NOT NULL REFERENCES cities(id)
);

-- USERS
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    phone VARCHAR(20),
    nationality VARCHAR(100),
    address_id BIGINT REFERENCES addresses(id),
    how_did_you_find_us TEXT,
    role_id BIGINT NOT NULL REFERENCES roles(id),
    bio TEXT,
    profile_image_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- SESSIONS (asegura unicidad del hash si tu código lo asume)
CREATE TABLE IF NOT EXISTS sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash BYTEA NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    user_agent TEXT,
    ip INET
);

-- COURSES
CREATE TABLE IF NOT EXISTS courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    short_description VARCHAR(500),
    thumbnail_url VARCHAR(500),
    video_preview_url VARCHAR(500),
    price DECIMAL(10,2) NOT NULL,
    duration_hours INTEGER,
    difficulty_level VARCHAR(20) CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    max_students INTEGER,
    current_students INTEGER NOT NULL DEFAULT 0,
    category VARCHAR(100),
    tags TEXT[],
    requirements TEXT[],
    learning_outcomes TEXT[],
    instructor_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP
);

-- ENROLLMENTS
CREATE TABLE IF NOT EXISTS enrollments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES users(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    enrollment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
    progress_percentage DECIMAL(5,2) DEFAULT 0,
    completed_at TIMESTAMP,
    UNIQUE(student_id, course_id)
);

-- LEADS
CREATE TABLE IF NOT EXISTS leads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    message TEXT,
    course_interest VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW' CHECK (status IN ('NEW', 'CONTACTED', 'CONVERTED', 'CANCELLED'))
);

-- TRANSACTIONS
CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    enrollment_id UUID REFERENCES enrollments(id),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    payment_method VARCHAR(50),
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    external_transaction_id VARCHAR(255),
    notes TEXT
);

-- SYSTEM_LOGS
CREATE TABLE IF NOT EXISTS system_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    additional_data JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    duration_ms BIGINT,
    ip_address INET,
    log_level VARCHAR(20) NOT NULL,
    message TEXT,
    module VARCHAR(100),
    request_method VARCHAR(10),
    request_path VARCHAR(500),
    stack_trace TEXT,
    status_code INTEGER,
    user_id UUID REFERENCES users(id),
    user_agent TEXT
);

-- === Normalizar columnas 'status' para que existan con defaults/constraints ===

-- COURSES.status: DRAFT/PUBLISHED/ARCHIVED
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='courses') THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_name='courses' AND column_name='status'
    ) THEN
      ALTER TABLE courses ADD COLUMN status VARCHAR(20);
    END IF;

    -- default + valores faltantes
    UPDATE courses SET status = 'DRAFT' WHERE status IS NULL;
    ALTER TABLE courses ALTER COLUMN status SET DEFAULT 'DRAFT';

    -- constraint si no existe
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='courses_status_check') THEN
      ALTER TABLE courses
        ADD CONSTRAINT courses_status_check
        CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED'));
    END IF;

    ALTER TABLE courses ALTER COLUMN status SET NOT NULL;
  END IF;
END $$;

-- ENROLLMENTS.status: ACTIVE/COMPLETED/CANCELLED
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='enrollments') THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_name='enrollments' AND column_name='status'
    ) THEN
      ALTER TABLE enrollments ADD COLUMN status VARCHAR(20);
    END IF;

    UPDATE enrollments SET status = 'ACTIVE' WHERE status IS NULL;
    ALTER TABLE enrollments ALTER COLUMN status SET DEFAULT 'ACTIVE';

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='enrollments_status_check') THEN
      ALTER TABLE enrollments
        ADD CONSTRAINT enrollments_status_check
        CHECK (status IN ('ACTIVE','COMPLETED','CANCELLED'));
    END IF;

    ALTER TABLE enrollments ALTER COLUMN status SET NOT NULL;
  END IF;
END $$;

-- LEADS.status: NEW/CONTACTED/CONVERTED/CANCELLED
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='leads') THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_name='leads' AND column_name='status'
    ) THEN
      ALTER TABLE leads ADD COLUMN status VARCHAR(20);
    END IF;

    UPDATE leads SET status = 'NEW' WHERE status IS NULL;
    ALTER TABLE leads ALTER COLUMN status SET DEFAULT 'NEW';

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='leads_status_check') THEN
      ALTER TABLE leads
        ADD CONSTRAINT leads_status_check
        CHECK (status IN ('NEW','CONTACTED','CONVERTED','CANCELLED'));
    END IF;

    ALTER TABLE leads ALTER COLUMN status SET NOT NULL;
  END IF;
END $$;

-- TRANSACTIONS.status: PENDING/COMPLETED/FAILED/REFUNDED
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='transactions') THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_name='transactions' AND column_name='status'
    ) THEN
      ALTER TABLE transactions ADD COLUMN status VARCHAR(20);
    END IF;

    UPDATE transactions SET status = 'PENDING' WHERE status IS NULL;
    ALTER TABLE transactions ALTER COLUMN status SET DEFAULT 'PENDING';

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='transactions_status_check') THEN
      ALTER TABLE transactions
        ADD CONSTRAINT transactions_status_check
        CHECK (status IN ('PENDING','COMPLETED','FAILED','REFUNDED'));
    END IF;

    ALTER TABLE transactions ALTER COLUMN status SET NOT NULL;
  END IF;
END $$;

-- === Índices seguros (solo si existen las tablas) ===
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='users') THEN
    CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
    CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
    CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='sessions') THEN
    CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON sessions(user_id);
    CREATE INDEX IF NOT EXISTS idx_sessions_expires_at ON sessions(expires_at);
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='courses') THEN
    CREATE INDEX IF NOT EXISTS idx_courses_instructor_id ON courses(instructor_id);
    CREATE INDEX IF NOT EXISTS idx_courses_status ON courses(status);
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='enrollments') THEN
    CREATE INDEX IF NOT EXISTS idx_enrollments_student_id ON enrollments(student_id);
    CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON enrollments(course_id);
    CREATE INDEX IF NOT EXISTS idx_enrollments_status ON enrollments(status);
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='leads') THEN
    CREATE INDEX IF NOT EXISTS idx_leads_email ON leads(email);
    CREATE INDEX IF NOT EXISTS idx_leads_status ON leads(status);
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='transactions') THEN
    CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
    CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='system_logs') THEN
    CREATE INDEX IF NOT EXISTS idx_system_logs_created_at ON system_logs(created_at);
    CREATE INDEX IF NOT EXISTS idx_system_logs_user_id ON system_logs(user_id);
  END IF;
END $$;


-- ÍNDICES (todos con IF NOT EXISTS)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_expires_at ON sessions(expires_at);
CREATE INDEX IF NOT EXISTS idx_courses_instructor_id ON courses(instructor_id);
CREATE INDEX IF NOT EXISTS idx_courses_status ON courses(status);
CREATE INDEX IF NOT EXISTS idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON enrollments(course_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_status ON enrollments(status);
CREATE INDEX IF NOT EXISTS idx_leads_email ON leads(email);
CREATE INDEX IF NOT EXISTS idx_leads_status ON leads(status);
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_system_logs_created_at ON system_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_system_logs_user_id ON system_logs(user_id);

