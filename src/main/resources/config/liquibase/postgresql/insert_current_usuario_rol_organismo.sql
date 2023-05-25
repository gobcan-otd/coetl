INSERT INTO tb_usuario_rol_organismo (id, id_usuario, id_rol, id_organismo)
select nextval('tb_roles_id_seq'), u.id, r.id, o.id  
FROM tb_etls e
inner join tb_organismo o on o.id = e.organization_in_charge 
inner join usuario u on u.login = e.created_by
inner join usuario_rol ur on u.id = ur.usuario_id
inner join tb_roles r on r."name" = ur.rol 
group by u.id, r.id, o.id 