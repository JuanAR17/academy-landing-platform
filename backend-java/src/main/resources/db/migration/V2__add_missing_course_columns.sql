-- V2__add_missing_course_columns.sql
-- ==================================
-- HAZ ESTE SCRIPT IDEMPOTENTE (que no reviente si ya corrió antes)

-- 1) courses: columnas faltantes
ALTER TABLE courses ADD COLUMN IF NOT EXISTS max_students     INTEGER;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS current_students INTEGER NOT NULL DEFAULT 0;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS published_at     TIMESTAMP;

-- asegurar que price no sea NULL (primero rellena nulos, luego impone NOT NULL)
UPDATE courses SET price = 0 WHERE price IS NULL;
ALTER TABLE courses ALTER COLUMN price SET NOT NULL;

-- 2) addresses: desnormalizar a city/state/country/zip_code
--    Agrega columnas solo si no existen
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS city     VARCHAR(100);
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS state    VARCHAR(100);
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS country  VARCHAR(100);
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS zip_code VARCHAR(20);

-- 3) Migrar datos desde la relación (solo si aún existe city_id)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name   = 'addresses'
      AND column_name  = 'city_id'
  ) THEN
    -- Copia datos donde las nuevas columnas estén vacías
    UPDATE addresses a
    SET city    = COALESCE(a.city, c.name),
        state   = COALESCE(a.state, s.name),
        country = COALESCE(a.country, co.name)
    FROM cities c
    JOIN states s   ON c.state_id = s.id
    JOIN countries co ON s.country_id = co.id
    WHERE a.city_id = c.id;
  END IF;
END $$;

-- 4) Intentar poner NOT NULL (solo si ya no quedan nulos)
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='addresses' AND column_name='city') THEN
    IF NOT EXISTS (SELECT 1 FROM addresses WHERE city IS NULL) THEN
      EXECUTE 'ALTER TABLE addresses ALTER COLUMN city SET NOT NULL';
    END IF;
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='addresses' AND column_name='state') THEN
    IF NOT EXISTS (SELECT 1 FROM addresses WHERE state IS NULL) THEN
      EXECUTE 'ALTER TABLE addresses ALTER COLUMN state SET NOT NULL';
    END IF;
  END IF;

  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='addresses' AND column_name='country') THEN
    IF NOT EXISTS (SELECT 1 FROM addresses WHERE country IS NULL) THEN
      EXECUTE 'ALTER TABLE addresses ALTER COLUMN country SET NOT NULL';
    END IF;
  END IF;
END $$;

-- 5) Quitar la FK a cities si existiera (defensivo) y luego eliminar city_id si existe
DO $$
DECLARE
  fk_name text;
BEGIN
  SELECT conname
  INTO fk_name
  FROM pg_constraint
  WHERE contype = 'f'
    AND conrelid = 'public.addresses'::regclass
    AND EXISTS (
      SELECT 1
      FROM unnest(conkey) WITH ORDINALITY AS cols(attnum, ord)
      WHERE attnum = (SELECT attnum FROM pg_attribute
                      WHERE attrelid = 'public.addresses'::regclass AND attname = 'city_id')
    )
  LIMIT 1;

  IF fk_name IS NOT NULL THEN
    EXECUTE format('ALTER TABLE public.addresses DROP CONSTRAINT %I', fk_name);
  END IF;
END $$;

ALTER TABLE addresses DROP COLUMN IF EXISTS city_id;

-- 6) (Opcional) índices útiles si no existen
CREATE INDEX IF NOT EXISTS idx_courses_status        ON courses(status);
CREATE INDEX IF NOT EXISTS idx_addresses_city        ON addresses(city);
CREATE INDEX IF NOT EXISTS idx_addresses_state       ON addresses(state);
CREATE INDEX IF NOT EXISTS idx_addresses_country     ON addresses(country);

