WITH duplicate_live AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY restaurant_id, language
               ORDER BY updated_at DESC NULLS LAST, id DESC
           ) AS row_number
    FROM menu_versions
    WHERE is_live = TRUE
)
UPDATE menu_versions AS menu
SET status = 'ARCHIVED',
    is_live = FALSE,
    updated_at = CURRENT_TIMESTAMP
FROM duplicate_live
WHERE menu.id = duplicate_live.id
  AND duplicate_live.row_number > 1;

CREATE UNIQUE INDEX IF NOT EXISTS uk_menu_versions_single_live_language
    ON menu_versions (restaurant_id, language)
    WHERE is_live = TRUE;