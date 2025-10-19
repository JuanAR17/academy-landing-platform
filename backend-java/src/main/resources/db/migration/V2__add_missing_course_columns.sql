
-- Agregar columnas faltantes a la tabla courses

ALTER TABLE courses ADD COLUMN IF NOT EXISTS max_students INTEGER;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS current_students INTEGER NOT NULL DEFAULT 0;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS published_at TIMESTAMP;

-- Actualizar la columna price para que sea NOT NULL
ALTER TABLE courses ALTER COLUMN price SET NOT NULL;

-- Modificar la tabla addresses para tener city, state, country y zip_code directamente

-- Primero, agregar las nuevas columnas
ALTER TABLE addresses 
ADD COLUMN city VARCHAR(100),
ADD COLUMN state VARCHAR(100),
ADD COLUMN country VARCHAR(100),
ADD COLUMN zip_code VARCHAR(20);

-- Migrar datos existentes (copiar nombre de ciudad desde la relación)
UPDATE addresses a
SET city = c.name,
    state = s.name,
    country = co.name
FROM cities c
JOIN states s ON c.state_id = s.id
JOIN countries co ON s.country_id = co.id
WHERE a.city_id = c.id;

-- Ahora hacer las columnas NOT NULL (después de migrar los datos)
ALTER TABLE addresses
ALTER COLUMN city SET NOT NULL,
ALTER COLUMN state SET NOT NULL,
ALTER COLUMN country SET NOT NULL;

-- Eliminar la columna city_id (ya no la necesitamos)
ALTER TABLE addresses DROP COLUMN city_id;
