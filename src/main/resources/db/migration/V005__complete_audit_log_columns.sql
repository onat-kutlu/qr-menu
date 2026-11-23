-- Complete the audit log columns required by AuditLog.java.

ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS status VARCHAR(50);
