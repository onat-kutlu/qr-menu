-- AuditLog.java intentionally uses Integer for its primary key.
-- Keep this table consistent with the entity while other domain ids use Long.

ALTER TABLE audit_logs ALTER COLUMN id TYPE INTEGER;