UPDATE klage.behandling
SET rol_flow_state_id = '1'
WHERE rol_flow_state_id is null;

ALTER TABLE klage.behandling
    ALTER COLUMN rol_flow_state_id SET NOT NULL;