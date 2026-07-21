ALTER TABLE users
    ADD verified BOOLEAN;

UPDATE users SET verified = FALSE WHERE verified IS NULL;

ALTER TABLE users
    ALTER COLUMN verified SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN client DROP NOT NULL;