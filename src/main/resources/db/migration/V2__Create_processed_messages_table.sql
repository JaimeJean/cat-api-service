-- V2__Create_processed_messages_table.sql

CREATE TABLE processed_messages (
    message_id VARCHAR(255) PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_processed_messages_processed_at ON processed_messages (processed_at);