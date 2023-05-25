INSERT INTO tmp_etl_organismo (etl_id, id_organismo)
select e.id, o.id 
FROM tb_etls e
inner join tb_organismo o on o."name" = e.organization_in_charge 