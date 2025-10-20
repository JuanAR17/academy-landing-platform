-- Hacer que instructor_id sea opcional para cursos en borrador
ALTER TABLE courses ALTER COLUMN instructor_id DROP NOT NULL;