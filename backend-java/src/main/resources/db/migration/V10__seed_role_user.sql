INSERT INTO roles (name, description)
SELECT 'USER', 'Rol base para usuarios creados desde checkout'
WHERE NOT EXISTS (
  SELECT 1 FROM roles WHERE lower(name) = 'user'
);
