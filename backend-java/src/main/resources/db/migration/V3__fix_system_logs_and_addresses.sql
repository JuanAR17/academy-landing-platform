-- V3__fix_system_logs_and_addresses.sql
-- Migración para corregir tipos de datos en system_logs, addresses y enrollments

-- 1. Corregir tabla addresses: cambiar de FK a columnas directas
ALTER TABLE addresses DROP CONSTRAINT IF EXISTS addresses_city_id_fkey;
ALTER TABLE addresses DROP COLUMN IF EXISTS city_id;

-- Agregar columnas directas si no existen
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS city VARCHAR(100);
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS state VARCHAR(100);
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS country VARCHAR(100);
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS zip_code VARCHAR(20);

-- 2. Corregir tabla system_logs: cambiar tipos de datos problemáticos

-- Cambiar id de BIGSERIAL a UUID
ALTER TABLE system_logs ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE IF EXISTS system_logs_id_seq CASCADE;
ALTER TABLE system_logs DROP CONSTRAINT IF EXISTS system_logs_pkey;
ALTER TABLE system_logs ALTER COLUMN id TYPE UUID USING gen_random_uuid();
ALTER TABLE system_logs ADD PRIMARY KEY (id);

-- Cambiar ip_address de INET a TEXT (más compatible con Hibernate)
ALTER TABLE system_logs ALTER COLUMN ip_address TYPE TEXT;

-- Recrear índices que puedan haber sido afectados
DROP INDEX IF EXISTS idx_system_logs_created_at;
DROP INDEX IF EXISTS idx_system_logs_user_id;

CREATE INDEX idx_system_logs_created_at ON system_logs(created_at);
CREATE INDEX idx_system_logs_user_id ON system_logs(user_id);

-- 3. Agregar columnas faltantes a la tabla enrollments
ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS amount_paid DECIMAL(10,2);
ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS certificate_issued BOOLEAN DEFAULT FALSE;
ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS certificate_url VARCHAR(500);
ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Actualizar constraint de status para incluir PENDING
ALTER TABLE enrollments DROP CONSTRAINT IF EXISTS enrollments_status_check;
ALTER TABLE enrollments ADD CONSTRAINT enrollments_status_check 
  CHECK (status::text = ANY (ARRAY['PENDING'::character varying, 'ACTIVE'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying]::text[]));

-- Renombrar enrollment_date a enrolled_at si existe (para compatibilidad)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'enrollments' AND column_name = 'enrollment_date') THEN
        -- Copiar datos de enrollment_date a enrolled_at si enrolled_at está vacío
        UPDATE enrollments SET enrolled_at = enrollment_date WHERE enrolled_at IS NULL;
    END IF;
END $$;
