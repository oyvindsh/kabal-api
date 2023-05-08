DELETE
FROM klage.kafka_event
WHERE type = 'STATS_DVH'
  AND kafka_event.kilde = 'IT01'
