-- Normaliza a minúsculas por si hay datos mezclados
UPDATE users SET email = lower(email) WHERE email <> lower(email);

-- (Opcional) detecta posibles duplicados que romperían el índice único
-- Revísalo antes de continuar si devuelve filas.
-- SELECT lower(email) AS email_lc, COUNT(*) 
-- FROM users GROUP BY lower(email) HAVING COUNT(*) > 1;

-- Elimina de forma segura la restricción UNIQUE actual sobre email (si existe)
DO $$
DECLARE
  ctext text;
BEGIN
  SELECT conname INTO ctext
  FROM pg_constraint
  WHERE conrelid = 'public.users'::regclass
    AND contype = 'u'
    AND conkey = ARRAY[
      (SELECT attnum FROM pg_attribute 
       WHERE attrelid = 'public.users'::regclass AND attname = 'email')
    ]
  LIMIT 1;

  IF ctext IS NOT NULL THEN
    EXECUTE format('ALTER TABLE public.users DROP CONSTRAINT %I', ctext);
  END IF;
END $$;

-- Crea unicidad case-insensitive
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email_ci ON users (lower(email));

-- (Opcional) vuelve a crear un índice normal no-único por email si lo quieres para búsquedas
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
