-- ============================================================
-- V2__Add_Quiz_Answer_File_Fields.sql
-- Add columns for storing student file attachments in quiz answers
-- ============================================================

ALTER TABLE quiz_answers ADD COLUMN file_url VARCHAR(500);
ALTER TABLE quiz_answers ADD COLUMN file_name VARCHAR(255);
