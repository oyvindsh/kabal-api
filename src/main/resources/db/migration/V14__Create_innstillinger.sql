CREATE TABLE klage.innstillinger
(
    saksbehandlerident TEXT PRIMARY KEY,
    hjemler            TEXT      NOT NULL,
    temaer             TEXT      NOT NULL,
    typer              TEXT      NOT NULL,
    tidspunkt          TIMESTAMP NOT NULL
);