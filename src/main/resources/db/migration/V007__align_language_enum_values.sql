-- Keep PostgreSQL language enum labels aligned with MenuVersion.Language names.
ALTER TYPE language RENAME VALUE 'tr' TO 'TR';
ALTER TYPE language RENAME VALUE 'en' TO 'EN';
ALTER TYPE language RENAME VALUE 'de' TO 'DE';
ALTER TYPE language RENAME VALUE 'fr' TO 'FR';