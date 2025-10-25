-- V8__epayco_transactions.sql

-- Detalle de transacciones ePayco (una fila por intento: aprobada, rechazada o pendiente)
-- Uso BIGSERIAL para ser consistente con V7 (epayco_customers / epayco_cards).
CREATE TABLE IF NOT EXISTS epayco_transactions (
  id             BIGSERIAL PRIMARY KEY,
  user_id        UUID REFERENCES users(id) ON DELETE SET NULL,

  ref_payco      BIGINT,           -- referencia epayco (útil para consultas/trazas)
  invoice        VARCHAR(128),
  description    VARCHAR(500),

  amount         NUMERIC(18,2),
  currency       VARCHAR(3),
  tax            NUMERIC(18,2),
  ico            NUMERIC(18,2),
  base_tax       NUMERIC(18,2),

  bank           VARCHAR(128),
  status         VARCHAR(32),
  response       VARCHAR(255),
  receipt        VARCHAR(64),
  txn_date       TIMESTAMPTZ,

  franchise      VARCHAR(20),
  code_response  INTEGER,
  code_error     VARCHAR(20),
  ip             VARCHAR(64),
  test_mode      BOOLEAN,

  doc_type       VARCHAR(4),
  doc_number     VARCHAR(20),
  first_names    VARCHAR(100),
  last_names     VARCHAR(100),
  email          VARCHAR(255),
  city           VARCHAR(100),
  address        VARCHAR(255),
  country_iso2   VARCHAR(2),

  extras         JSONB,      -- bloque "extras" que devuelve ePayco
  raw_payload    JSONB,      -- respuesta cruda completa

  created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_epayco_tx_user    ON epayco_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_epayco_tx_ref     ON epayco_transactions(ref_payco);
CREATE INDEX IF NOT EXISTS idx_epayco_tx_status  ON epayco_transactions(status);

-- (Opcional) enlazar payments a la transacción de ePayco:
-- Solo si te sirve para navegar payments -> detalle ePayco.
ALTER TABLE payments
  ADD COLUMN IF NOT EXISTS epayco_tx_id BIGINT REFERENCES epayco_transactions(id);

-- (Opcional) si no lo añadiste en V7 y lo quieres en payments:
-- ALTER TABLE payments ADD COLUMN IF NOT EXISTS ref_payco BIGINT;
