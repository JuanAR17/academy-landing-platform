-- Agregar columnas nuevas a la tabla users
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'STUDENT' NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE NOT NULL;

CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

CREATE TABLE IF NOT EXISTS courses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title VARCHAR(200) NOT NULL,
  description TEXT,
  short_description VARCHAR(500),
  thumbnail_url VARCHAR(500),
  video_preview_url VARCHAR(500),
  teacher_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  price DECIMAL(10, 2) NOT NULL,
  duration_hours INTEGER,
  difficulty_level VARCHAR(50),
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  max_students INTEGER,
  current_students INTEGER NOT NULL DEFAULT 0,
  category VARCHAR(100) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ,
  published_at TIMESTAMPTZ
);

-- Índices para cursos
CREATE INDEX IF NOT EXISTS idx_courses_teacher ON courses(teacher_id);
CREATE INDEX IF NOT EXISTS idx_courses_status ON courses(status);
CREATE INDEX IF NOT EXISTS idx_courses_category ON courses(category);
CREATE INDEX IF NOT EXISTS idx_courses_created_at ON courses(created_at);

CREATE TABLE IF NOT EXISTS course_tags (
  course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  tag VARCHAR(100) NOT NULL,
  PRIMARY KEY (course_id, tag)
);

CREATE INDEX IF NOT EXISTS idx_course_tags_tag ON course_tags(tag);

CREATE TABLE IF NOT EXISTS course_requirements (
  course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  requirement TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS course_learning_outcomes (
  course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  outcome TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS enrollments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  enrolled_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  completed_at TIMESTAMPTZ,
  progress_percentage INTEGER NOT NULL DEFAULT 0,
  amount_paid DECIMAL(10, 2),
  notes TEXT,
  certificate_issued BOOLEAN NOT NULL DEFAULT FALSE,
  certificate_url VARCHAR(500),
  updated_at TIMESTAMPTZ,
  CONSTRAINT unique_student_course UNIQUE (student_id, course_id)
);

CREATE INDEX IF NOT EXISTS idx_enrollments_student ON enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course ON enrollments(course_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_status ON enrollments(status);
CREATE INDEX IF NOT EXISTS idx_enrollments_enrolled_at ON enrollments(enrolled_at);

CREATE TABLE IF NOT EXISTS transactions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  course_id UUID REFERENCES courses(id) ON DELETE SET NULL,
  enrollment_id UUID REFERENCES enrollments(id) ON DELETE SET NULL,
  transaction_reference VARCHAR(100) UNIQUE NOT NULL,
  external_transaction_id VARCHAR(200),
  amount DECIMAL(10, 2) NOT NULL,
  currency VARCHAR(3) NOT NULL DEFAULT 'USD',
  payment_method VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  payment_gateway VARCHAR(50),
  description TEXT,
  error_message TEXT,
  metadata JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ,
  completed_at TIMESTAMPTZ,
  refunded_at TIMESTAMPTZ,
  refund_reason TEXT
);

CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_course ON transactions(course_id);
CREATE INDEX IF NOT EXISTS idx_transactions_enrollment ON transactions(enrollment_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_reference ON transactions(transaction_reference);
CREATE INDEX IF NOT EXISTS idx_transactions_external_id ON transactions(external_transaction_id);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);

CREATE TABLE IF NOT EXISTS system_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  log_level VARCHAR(20) NOT NULL,
  module VARCHAR(100) NOT NULL,
  action VARCHAR(200) NOT NULL,
  message TEXT,
  user_id UUID REFERENCES users(id) ON DELETE SET NULL,
  ip_address VARCHAR(45),
  user_agent TEXT,
  request_path VARCHAR(500),
  request_method VARCHAR(10),
  status_code INTEGER,
  duration_ms BIGINT,
  stack_trace TEXT,
  additional_data JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_system_logs_level ON system_logs(log_level);
CREATE INDEX IF NOT EXISTS idx_system_logs_module ON system_logs(module);
CREATE INDEX IF NOT EXISTS idx_system_logs_user ON system_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_system_logs_created_at ON system_logs(created_at);

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_courses_updated_at BEFORE UPDATE ON courses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_enrollments_updated_at BEFORE UPDATE ON enrollments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
ALTER TABLE system_logs ALTER COLUMN additional_data TYPE TEXT;