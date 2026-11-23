-- V001__init_tables.sql
-- Initial database schema for QR Menu MVP

-- Create ENUM types
CREATE TYPE user_role AS ENUM ('SUPER_ADMIN', 'ADMIN', 'COMPANY_ADMIN', 'COMPANY_USER');
CREATE TYPE company_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');
CREATE TYPE restaurant_status AS ENUM ('ACTIVE', 'INACTIVE', 'PENDING_APPROVAL');
CREATE TYPE pdf_status AS ENUM ('PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'LIVE', 'ARCHIVED', 'DELETED');
CREATE TYPE language AS ENUM ('tr', 'en', 'de', 'fr');
CREATE TYPE approval_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

-- Users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified_at TIMESTAMP,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Companies table
CREATE TABLE companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    status company_status NOT NULL DEFAULT 'ACTIVE',
    subscription_tier VARCHAR(50) DEFAULT 'free',
    created_by INTEGER NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_companies_slug ON companies(slug);
CREATE INDEX idx_companies_status ON companies(status);

-- Company Users (linking users to companies with roles)
CREATE TABLE company_users (
    id SERIAL PRIMARY KEY,
    company_id INTEGER NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role user_role NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, user_id)
);

CREATE INDEX idx_company_users_company_id ON company_users(company_id);
CREATE INDEX idx_company_users_user_id ON company_users(user_id);

-- Restaurants table
CREATE TABLE restaurants (
    id SERIAL PRIMARY KEY,
    company_id INTEGER NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description TEXT,
    address VARCHAR(500),
    phone VARCHAR(20),
    logo_path VARCHAR(500),
    status restaurant_status NOT NULL DEFAULT 'PENDING_APPROVAL',
    monthly_upload_quota INTEGER NOT NULL DEFAULT 20,
    monthly_uploads_used INTEGER NOT NULL DEFAULT 0,
    created_by INTEGER NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    UNIQUE(company_id, slug)
);

CREATE INDEX idx_restaurants_company_id ON restaurants(company_id);
CREATE INDEX idx_restaurants_slug ON restaurants(slug);
CREATE INDEX idx_restaurants_status ON restaurants(status);

-- Restaurant Users (linking users to specific restaurants)
CREATE TABLE restaurant_users (
    id SERIAL PRIMARY KEY,
    restaurant_id INTEGER NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    permissions VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(restaurant_id, user_id)
);

CREATE INDEX idx_restaurant_users_restaurant_id ON restaurant_users(restaurant_id);
CREATE INDEX idx_restaurant_users_user_id ON restaurant_users(user_id);

-- Restaurant Approval Requests
CREATE TABLE restaurant_approval_requests (
    id SERIAL PRIMARY KEY,
    restaurant_id INTEGER NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    company_id INTEGER NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    requested_by INTEGER NOT NULL REFERENCES users(id),
    status approval_status NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    reviewed_by INTEGER REFERENCES users(id),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_restaurant_approval_requests_restaurant_id ON restaurant_approval_requests(restaurant_id);
CREATE INDEX idx_restaurant_approval_requests_status ON restaurant_approval_requests(status);

-- Slug History (for tracking slug changes and redirects)
CREATE TABLE slug_history (
    id SERIAL PRIMARY KEY,
    restaurant_id INTEGER NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    old_slug VARCHAR(100),
    new_slug VARCHAR(100) NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_slug_history_restaurant_id ON slug_history(restaurant_id);
CREATE INDEX idx_slug_history_new_slug ON slug_history(new_slug);

-- Slug Change Requests
CREATE TABLE slug_change_requests (
    id SERIAL PRIMARY KEY,
    restaurant_id INTEGER NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    current_slug VARCHAR(100) NOT NULL,
    requested_slug VARCHAR(100) NOT NULL,
    requested_by INTEGER NOT NULL REFERENCES users(id),
    status approval_status NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    reviewed_by INTEGER REFERENCES users(id),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_slug_change_requests_restaurant_id ON slug_change_requests(restaurant_id);
CREATE INDEX idx_slug_change_requests_status ON slug_change_requests(status);

-- Menu Versions
CREATE TABLE menu_versions (
    id SERIAL PRIMARY KEY,
    restaurant_id INTEGER NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    language language NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT,
    file_hash VARCHAR(64) NOT NULL,
    page_count INTEGER,
    status pdf_status NOT NULL DEFAULT 'PENDING_APPROVAL',
    version_number INTEGER NOT NULL,
    is_live BOOLEAN NOT NULL DEFAULT FALSE,
    created_by INTEGER NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    UNIQUE(restaurant_id, language, version_number)
);

CREATE INDEX idx_menu_versions_restaurant_id ON menu_versions(restaurant_id);
CREATE INDEX idx_menu_versions_language ON menu_versions(language);
CREATE INDEX idx_menu_versions_status ON menu_versions(status);
CREATE INDEX idx_menu_versions_is_live ON menu_versions(is_live);

-- Menu Upload Limits
CREATE TABLE menu_upload_limits (
    id SERIAL PRIMARY KEY,
    restaurant_id INTEGER NOT NULL UNIQUE REFERENCES restaurants(id) ON DELETE CASCADE,
    monthly_quota INTEGER NOT NULL DEFAULT 20,
    uploads_used_this_month INTEGER NOT NULL DEFAULT 0,
    reset_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_menu_upload_limits_restaurant_id ON menu_upload_limits(restaurant_id);

-- PDF Rejection Reasons
CREATE TABLE pdf_rejection_reasons (
    id SERIAL PRIMARY KEY,
    menu_version_id INTEGER NOT NULL REFERENCES menu_versions(id) ON DELETE CASCADE,
    category VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    rejected_by INTEGER NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pdf_rejection_reasons_menu_version_id ON pdf_rejection_reasons(menu_version_id);

-- Analytics Events
CREATE TABLE analytics_events (
    id SERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    restaurant_id INTEGER REFERENCES restaurants(id) ON DELETE SET NULL,
    company_id INTEGER REFERENCES companies(id) ON DELETE SET NULL,
    language language,
    visit_id VARCHAR(100),
    ip_hash VARCHAR(64),
    user_agent TEXT,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_analytics_events_restaurant_id ON analytics_events(restaurant_id);
CREATE INDEX idx_analytics_events_event_type ON analytics_events(event_type);
CREATE INDEX idx_analytics_events_created_at ON analytics_events(created_at);

-- Audit Logs
CREATE TABLE audit_logs (
    id SERIAL PRIMARY KEY,
    entity_type VARCHAR(100) NOT NULL,
    entity_id INTEGER NOT NULL,
    action VARCHAR(50) NOT NULL,
    performed_by INTEGER REFERENCES users(id),
    old_values JSONB,
    new_values JSONB,
    reason TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_performed_by ON audit_logs(performed_by);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- Login Sessions (for JWT token tracking)
CREATE TABLE login_sessions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_jti VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(45),
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_login_sessions_user_id ON login_sessions(user_id);
CREATE INDEX idx_login_sessions_token_jti ON login_sessions(token_jti);
CREATE INDEX idx_login_sessions_expires_at ON login_sessions(expires_at);

-- Password Reset Tokens
CREATE TABLE password_reset_tokens (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);

-- Email Verification Codes
CREATE TABLE email_verification_codes (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_verification_codes_user_id ON email_verification_codes(user_id);
CREATE INDEX idx_email_verification_codes_code ON email_verification_codes(code);
