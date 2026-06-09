-- Inicialización Nodo Trujillo
CREATE TABLE IF NOT EXISTS accounts (
    id SERIAL PRIMARY KEY,
    owner VARCHAR(100) UNIQUE NOT NULL,
    balance NUMERIC(15, 2) NOT NULL CHECK (balance >= 0)
);

INSERT INTO accounts (owner, balance) 
VALUES ('Cuenta_Trujillo', 75000.00)
ON CONFLICT (owner) DO NOTHING;