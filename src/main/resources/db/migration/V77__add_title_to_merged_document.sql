--Create new parent
CREATE TABLE klage.merged_document
(
    id      UUID PRIMARY KEY,
    title   TEXT      NOT NULL,
    created TIMESTAMP NOT NULL
);

DELETE
FROM klage.document_to_merge;

ALTER TABLE klage.document_to_merge
    RENAME COLUMN reference_id to merged_document_id;

ALTER TABLE klage.document_to_merge
    DROP COLUMN created;

ALTER TABLE klage.document_to_merge
    ADD CONSTRAINT fk_merged_document
        FOREIGN KEY (merged_document_id)
            REFERENCES klage.merged_document (id);