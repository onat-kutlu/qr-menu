-- Align the audit log table with AuditLog.java and preserve existing actor ids.

ALTER TABLE audit_logs RENAME COLUMN performed_by TO user_id;
ALTER TABLE audit_logs RENAME COLUMN reason TO error_message;

ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS user_email VARCHAR(255);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS description VARCHAR(500);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS details TEXT;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS user_agent TEXT;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS raw_request TEXT;

CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);