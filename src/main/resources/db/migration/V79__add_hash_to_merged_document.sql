DELETE FROM klage.document_to_merge;
DELETE FROM klage.merged_document;

ALTER TABLE klage.merged_document
    ADD COLUMN hash TEXT NOT NULL;

ALTER TABLE klage.merged_document
    ADD CONSTRAINT unique_merged_document UNIQUE (hash);