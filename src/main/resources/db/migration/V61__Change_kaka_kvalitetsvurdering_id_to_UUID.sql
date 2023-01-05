ALTER TABLE klage.behandling
    ALTER COLUMN kaka_kvalitetsvurdering_id TYPE UUID USING kaka_kvalitetsvurdering_id::UUID;