-- V4__make_instructor_nullable.sql
-- Asegura courses.instructor_id (nullable), FK a users(id) e índice.

-- 1) Si existe teacher_id y NO existe instructor_id -> renombrar
DO $do$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'courses' AND column_name = 'teacher_id'
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'courses' AND column_name = 'instructor_id'
  ) THEN
    EXECUTE 'ALTER TABLE public.courses RENAME COLUMN teacher_id TO instructor_id';
  END IF;
END
$do$;

-- 2) Crear instructor_id si no existe
DO $do$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'courses' AND column_name = 'instructor_id'
  ) THEN
    EXECUTE 'ALTER TABLE public.courses ADD COLUMN instructor_id BIGINT';
  END IF;
END
$do$;

-- 3) Asegurar que sea NULLABLE (si hoy es NOT NULL)
DO $do$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'courses'
      AND column_name = 'instructor_id' AND is_nullable = 'NO'
  ) THEN
    EXECUTE 'ALTER TABLE public.courses ALTER COLUMN instructor_id DROP NOT NULL';
  END IF;
END
$do$;

-- 4) Agregar FK si no existe
DO $do$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu
      ON tc.constraint_name = kcu.constraint_name
     AND tc.constraint_schema = kcu.constraint_schema
    WHERE tc.table_schema = 'public'
      AND tc.table_name = 'courses'
      AND tc.constraint_type = 'FOREIGN KEY'
      AND kcu.column_name = 'instructor_id'
  ) THEN
    EXECUTE 'ALTER TABLE public.courses
             ADD CONSTRAINT fk_courses_instructor
             FOREIGN KEY (instructor_id)
             REFERENCES public.users(id)
             ON DELETE SET NULL';
  END IF;
END
$do$;

-- 5) Índice para consultas por instructor
CREATE INDEX IF NOT EXISTS idx_courses_instructor_id
  ON public.courses (instructor_id);

