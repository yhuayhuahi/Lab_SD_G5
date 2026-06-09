-- Arequipa DB initialization
CREATE TABLE IF NOT EXISTS accounts (
  id SERIAL PRIMARY KEY,
  owner VARCHAR(100) NOT NULL,
  balance BIGINT NOT NULL
);

INSERT INTO accounts (owner, balance) VALUES ('Cuenta_Arequipa', 100000) ON CONFLICT DO NOTHING;
