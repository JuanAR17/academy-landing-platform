-- 00_extensions.sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;  -- gen_random_uuid(), digest(), etc.
CREATE EXTENSION IF NOT EXISTS citext;    -- case-insensitive text (great for emails)

-- 01_core.sql
-- USERS
CREATE TABLE IF NOT EXISTS users (
  id            uuid    PRIMARY KEY DEFAULT gen_random_uuid(),
  email         citext  UNIQUE NOT NULL,  -- case-insensitive uniqueness
  -- Store the full PHC string for Argon2/bcrypt (e.g., "$argon2id$v=19$m=..." or "$2b$...")
  password_hash text    NOT NULL,
  created_at    timestamptz NOT NULL DEFAULT now()
);

-- SESSIONS (refresh-token store)
CREATE TABLE IF NOT EXISTS sessions_user (
  id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id            uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  -- Store the *hash of the refresh token* as raw bytes (sha256 -> 32 bytes)
  refresh_token_hash bytea NOT NULL UNIQUE,
  created_at         timestamptz NOT NULL DEFAULT now(),
  last_used_at       timestamptz,
  expires_at         timestamptz NOT NULL,
  is_revoked         boolean NOT NULL DEFAULT false,
  -- optional but handy for device management / forensics
  ip                 inet,
  user_agent         text,
  -- sanity check: session expiry must be in the future relative to creation
  CONSTRAINT sessions_expiry_future CHECK (expires_at > created_at)
);

-- Useful indexes
CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON sessions_user(user_id);

-- For frequent queries: "is this session active right now?"
CREATE INDEX IF NOT EXISTS idx_sessions_active_exp
  ON sessions_user (expires_at)
  WHERE is_revoked = false;

-- (Optional) A broader cleanup index if you’ll batch-delete expired rows
CREATE INDEX IF NOT EXISTS idx_sessions_expires_at ON sessions_user(expires_at);
