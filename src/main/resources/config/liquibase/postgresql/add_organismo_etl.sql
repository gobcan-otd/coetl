update tb_etls set organization_in_charge = tmp."id_organismo"
from (
	select eo."id_organismo", eo."etl_id"
	from tmp_etl_organismo eo
) as tmp
where id = tmp."etl_id"