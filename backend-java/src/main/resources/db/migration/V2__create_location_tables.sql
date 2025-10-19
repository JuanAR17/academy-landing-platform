
CREATE TABLE IF NOT EXISTS countries (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    phone_code VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS states (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    country_id BIGINT NOT NULL REFERENCES countries(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    state_id BIGINT NOT NULL REFERENCES states(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_states_country_id ON states(country_id);
CREATE INDEX IF NOT EXISTS idx_cities_state_id ON cities(state_id);

