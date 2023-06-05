ALTER TABLE klage.behandling
    RENAME COLUMN satt_paa_vent TO satt_paa_vent_from,
    RENAME COLUMN satt_paa_vent_expires TO satt_paa_vent_to;