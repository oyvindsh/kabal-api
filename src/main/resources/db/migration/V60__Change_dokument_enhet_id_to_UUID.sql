ALTER TABLE klage.delbehandling
    ALTER COLUMN dokument_enhet_id TYPE UUID USING dokument_enhet_id::UUID;