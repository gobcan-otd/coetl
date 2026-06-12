UPDATE usuario SET all_etl_access = true WHERE id IN 
	(SELECT distinct id_usuario 
	 FROM tb_usuario_rol_organismo 
	 WHERE id_rol IN (SELECT id 
	 				  FROM tb_roles 
	 				  WHERE name in ('TECNICO', 'LECTOR')));

