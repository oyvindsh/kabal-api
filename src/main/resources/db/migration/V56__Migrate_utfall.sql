UPDATE klage.delbehandling d
SET utfall_id = '9'
FROM klage.behandling b
WHERE d.utfall_id = '6'
  AND b.id = d.behandling_id
  AND b.behandling_type = 'anke';

UPDATE klage.delbehandling d
SET utfall_id = '10'
FROM klage.behandling b
WHERE d.utfall_id = '8'
  AND b.id = d.behandling_id
  AND b.behandling_type = 'anke';