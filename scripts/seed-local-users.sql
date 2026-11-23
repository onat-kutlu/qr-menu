-- Local-only test identities. Passwords are the role names and must not be used outside local development.

INSERT INTO users (
    email, password_hash, first_name, last_name, is_active, email_verified, global_role
) VALUES
    ('SUPER_ADMIN@xyz.com', '$2y$12$nO.hKyO9nMg16de1vJUk2eTbHgbqkvkm39Q3YOZUxKLmX22s8asuW', 'Super', 'Admin', TRUE, TRUE, 'SUPER_ADMIN'),
    ('ADMIN@xyz.com', '$2y$12$e7MSWOrQ78kWujKrQZS.Ze5duaspdltUV6pmZr/zU8VvgvpasbvFa', 'Platform', 'Admin', TRUE, TRUE, 'ADMIN'),
    ('COMPANY_ADMIN@xyz.com', '$2y$12$Tu/bFsGVd2pWCsKxIKuNbecl6aX1Ngcx9BkGmqw3qaK65h94MYG5q', 'Company', 'Owner', TRUE, TRUE, NULL)
ON CONFLICT (email) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    is_active = TRUE,
    email_verified = TRUE,
    deleted_at = NULL,
    global_role = EXCLUDED.global_role,
    updated_at = CURRENT_TIMESTAMP;

WITH super_admin AS (
    SELECT id FROM users WHERE email = 'SUPER_ADMIN@xyz.com'
), company_row AS (
    INSERT INTO companies (name, slug, description, status, subscription_tier, created_by)
    SELECT 'XYZ Test Company', 'xyz-test-company', 'Local development company', 'ACTIVE', 'free', id
    FROM super_admin
    ON CONFLICT (slug) DO UPDATE SET
        status = 'ACTIVE',
        deleted_at = NULL,
        updated_at = CURRENT_TIMESTAMP
    RETURNING id
), company_user AS (
    SELECT id FROM users WHERE email = 'COMPANY_ADMIN@xyz.com'
)
INSERT INTO company_users (company_id, user_id, role, is_active)
SELECT company_row.id, company_user.id, 'COMPANY_ADMIN', TRUE
FROM company_row CROSS JOIN company_user
ON CONFLICT (company_id, user_id) DO UPDATE SET
    role = 'COMPANY_ADMIN',
    is_active = TRUE,
    updated_at = CURRENT_TIMESTAMP;

SELECT u.email, COALESCE(u.global_role::text, cu.role::text) AS role, u.is_active
FROM users u
LEFT JOIN company_users cu ON cu.user_id = u.id AND cu.is_active = TRUE
WHERE u.email IN ('SUPER_ADMIN@xyz.com', 'ADMIN@xyz.com', 'COMPANY_ADMIN@xyz.com')
ORDER BY u.email;
