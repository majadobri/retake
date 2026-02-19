-- postal_code-feltet i Address-entiteten er String i Java, men BIGINT i DB.
-- Fikser dette ved å endre kolonnetypen til VARCHAR.
ALTER TABLE address ALTER COLUMN postal_code TYPE VARCHAR(20) USING postal_code::text;
