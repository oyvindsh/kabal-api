UPDATE klage.behandling
SET dato_behandling_tildelt = NULL
WHERE dato_behandling_tildelt IS NOT NULL
AND tildelt_saksbehandlerident IS NULL;