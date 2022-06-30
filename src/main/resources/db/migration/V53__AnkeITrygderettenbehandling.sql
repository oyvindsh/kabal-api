ALTER TABLE klage.behandling
    ADD COLUMN sendt_til_trygderetten TIMESTAMP,
    ADD COLUMN kjennelse_mottatt      TIMESTAMP;