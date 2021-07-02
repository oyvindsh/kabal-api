ALTER TABLE klage.mottak
    ADD CONSTRAINT unique_mottak UNIQUE (kildesystem, kilde_referanse);