ALTER TABLE klage.mottak_hjemmel
    ALTER COLUMN lov DROP NOT NULL;

ALTER TABLE klage.mottak_hjemmel
    ADD COLUMN hjemmel_id TEXT;

--migrere det som finnes i prod fra K9
update klage.mottak_hjemmel set hjemmel_id = '1000.009' where kapittel = 9 and paragraf is null;
update klage.mottak_hjemmel set hjemmel_id = '1000.009.002' where kapittel = 9 and paragraf = 2;
update klage.mottak_hjemmel set hjemmel_id = '1000.009.003' where kapittel = 9 and paragraf = 3;
update klage.mottak_hjemmel set hjemmel_id = '1000.009.005' where kapittel = 9 and paragraf = 5;
update klage.mottak_hjemmel set hjemmel_id = '1000.009.006' where kapittel = 9 and paragraf = 6;
update klage.mottak_hjemmel set hjemmel_id = '1000.009.008' where kapittel = 9 and paragraf = 8;
update klage.mottak_hjemmel set hjemmel_id = '1000.009.009' where kapittel = 9 and paragraf = 9;
update klage.mottak_hjemmel set hjemmel_id = '1000.009.010' where kapittel = 9 and paragraf = 10;
update klage.mottak_hjemmel set hjemmel_id = '1000.009.011' where kapittel = 9 and paragraf = 11;
update klage.mottak_hjemmel set hjemmel_id = '1000.009.013' where kapittel = 9 and paragraf = 13;
update klage.mottak_hjemmel set hjemmel_id = '1000.009.014' where kapittel = 9 and paragraf = 14;
update klage.mottak_hjemmel set hjemmel_id = '1000.009.015' where kapittel = 9 and paragraf = 15;

update klage.mottak_hjemmel set hjemmel_id = '1000.022.013' where kapittel = 22 and paragraf = 13;
update klage.mottak_hjemmel set hjemmel_id = '1000.022.015' where kapittel = 22 and paragraf = 15;

update klage.mottak_hjemmel set hjemmel_id = '1002' where lov = 'UKJENT' and kapittel is null and paragraf is null;

--migrere til noe OK i dev
update klage.mottak_hjemmel set hjemmel_id = '1000.008.004' where kapittel = 8;

--leftovers
update klage.mottak_hjemmel set hjemmel_id = '1002' where hjemmel_id is null;

ALTER TABLE klage.mottak_hjemmel
    ALTER COLUMN hjemmel_id SET NOT NULL;