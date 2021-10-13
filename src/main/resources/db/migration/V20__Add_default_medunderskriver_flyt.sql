UPDATE klage.klagebehandling
SET medunderskriverflyt_id = 2 WHERE medunderskriverident IS NOT NULL;

UPDATE klage.klagebehandling
SET medunderskriverflyt_id = 1 WHERE medunderskriverflyt_id IS NULL;

ALTER TABLE klage.klagebehandling
    ALTER COLUMN medunderskriverflyt_id SET NOT NULL;