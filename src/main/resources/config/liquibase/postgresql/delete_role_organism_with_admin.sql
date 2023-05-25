DELETE FROM tb_usuario_rol_organismo 
WHERE id_rol IN (SELECT id 
				  FROM tb_roles 
				  WHERE name = 'ADMIN');