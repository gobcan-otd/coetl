INSERT INTO tb_organismo (id, name, description)
SELECT nextval('organismo_id_seq'), organization_in_charge, null 
FROM tb_etls
where organization_in_charge not in (select name from tb_organismo to2 group by name)
group by organization_in_charge