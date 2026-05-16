-- Migration: Add plain_password column for development debugging
-- This column stores the plain text password for development purposes only
-- ⚠️ WARNING: This is for development/testing only - DO NOT use in production!

USE eduapp_db;

-- Add plain_password column if it doesn't exist
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS plain_password VARCHAR(255) NULL COMMENT 'Development only - plain text password for debugging';

-- Add index for easier querying during development
CREATE INDEX idx_plain_password ON users(plain_password);

-- Optional: Update existing users with their plain text passwords if you know them
-- Uncomment and modify if needed:
-- UPDATE users SET plain_password = 'password123' WHERE email = 'test@example.com';
