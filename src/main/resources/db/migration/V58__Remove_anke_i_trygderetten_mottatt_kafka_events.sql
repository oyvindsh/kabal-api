DELETE
FROM klage.kafka_event
WHERE type = 'STATS_DVH'
  AND json_payload LIKE '%Anke i trygderetten%';
