ALTER TABLE klage.behandling
    ADD COLUMN satt_paa_vent_expires   DATE,
    ADD COLUMN satt_paa_vent_reason    TEXT,
    ALTER COLUMN satt_paa_vent TYPE DATE USING satt_paa_vent::date;

UPDATE klage.behandling
SET satt_paa_vent_reason = 'Satt på vent', satt_paa_vent_expires = (satt_paa_vent + interval '4 weeks')
WHERE satt_paa_vent IS NOT NULL;

