-- V7__epayco_tokenization.sql

-- Cliente ePayco por usuario
CREATE TABLE IF NOT EXISTS epayco_customers (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  customer_id VARCHAR(40) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_epayco_customers_user ON epayco_customers(user_id);

-- Tarjetas tokenizadas
CREATE TABLE IF NOT EXISTS epayco_cards (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  card_token_id VARCHAR(40) NOT NULL UNIQUE,
  brand VARCHAR(20),
  last4 VARCHAR(4),
  exp_year VARCHAR(4),
  exp_month VARCHAR(2),
  holder_name VARCHAR(150),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_epayco_cards_user ON epayco_cards(user_id);

-- Ampliar payments para guardar metadatos del gateway y payload crudo
ALTER TABLE payments
  ADD COLUMN IF NOT EXISTS gateway VARCHAR(20) DEFAULT 'EPAYCO',
  ADD COLUMN IF NOT EXISTS auth_code VARCHAR(10),        -- <--- antes usabas "authorization" (reservada)
  ADD COLUMN IF NOT EXISTS response_code VARCHAR(10),
  ADD COLUMN IF NOT EXISTS raw JSONB;
