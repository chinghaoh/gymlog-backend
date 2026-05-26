ALTER TABLE users ALTER COLUMN is_demo SET DEFAULT false;
UPDATE users SET is_demo = false WHERE is_demo IS NULL;