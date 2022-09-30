UPDATE d
SET d.utfall_id = '9'
FROM klage.delbehandling d,
     klage.behandling b
WHERE d.utfall_id = '6'
  AND b.id = d.behandling_id
  AND b.behandling_type = 'anke';

UPDATE d
SET d.utfall_id = '10'
FROM klage.delbehandling d,
     klage.behandling b
WHERE d.utfall_id = '8'
  AND b.id = d.behandling_id
  AND b.behandling_type = 'anke';