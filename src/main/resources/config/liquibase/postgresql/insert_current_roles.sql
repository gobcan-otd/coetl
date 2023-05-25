INSERT INTO tb_roles (id, name)
SELECT nextval('tb_roles_id_seq'), rol
FROM usuario_rol
group by rol
