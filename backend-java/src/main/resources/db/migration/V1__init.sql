CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE IF NOT EXISTS leads(
  id          BIGSERIAL PRIMARY KEY,
  ts_utc      TIMESTAMPTZ NOT NULL,
  email       VARCHAR(320) NOT NULL,
  name        VARCHAR(255) NOT NULL,
  courses_json TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS users(
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email        CITEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS sessions(
  id                 UUID PRIMARY KEY,
  user_id            UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  refresh_token_hash BYTEA UNIQUE NOT NULL,
  created_at         TIMESTAMPTZ NOT NULL,
  last_used_at       TIMESTAMPTZ,
  expires_at         TIMESTAMPTZ NOT NULL,
  is_revoked         BOOLEAN NOT NULL DEFAULT FALSE,
  ip                 INET,
  user_agent         TEXT
);

CREATE INDEX IF NOT EXISTS idx_sessions_active_exp
  ON sessions(expires_at)
  WHERE is_revoked = false;
