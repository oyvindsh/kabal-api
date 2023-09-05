ALTER TABLE klage.behandling
    RENAME COLUMN medunderskriverflyt_id TO medunderskriver_flow_state_id;
ALTER TABLE klage.behandling
    RENAME COLUMN rol_state_id TO rol_flow_state_id;