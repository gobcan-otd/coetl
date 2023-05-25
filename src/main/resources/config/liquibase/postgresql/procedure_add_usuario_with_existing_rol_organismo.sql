CREATE OR REPLACE FUNCTION add_usuario_with_existing_rol(login usuario.login%TYPE, nombre usuario.nombre%TYPE, apellido1 usuario.apellido1%TYPE, 
                                                         apellido2 usuario.apellido2%TYPE, email usuario.email%TYPE, nameRol tb_roles.name%TYPE,
                                                         organismo tb_organismo.name%TYPE) 
    RETURNS void AS $$
    DECLARE
        id_rol_sel bigint := 0;
        id_organismo bigint := 0;
    BEGIN
        INSERT INTO usuario(id, opt_lock, login, nombre, apellido1, apellido2, email, deletion_date, created_by, created_date, last_modified_by, last_modified_date)
            VALUES(nextval('usuario_id_seq'), 0, login, nombre, apellido1, apellido2, email, NULL, 'system', now(), 'system', NULL);

        select id into id_rol_sel from tb_roles where name = nameRol;
        select id into id_organismo from tb_organismo where name = organismo;
        INSERT INTO tb_usuario_rol_organismo(id, id_usuario, id_rol, id_organismo)
            VALUES(nextval('usuario_rol_organismo_id_seq'), currval('usuario_id_seq'), id_rol_sel, id_organismo);
    
        raise notice 'Done!';
    
    EXCEPTION
        WHEN unique_violation THEN
            raise notice 'Se ha violado una constraint única. ERROR -> %', SQLERRM;
        WHEN OTHERS THEN
            raise notice 'Algo ha ocurrido mal. ERROR -> %', SQLERRM;
    END;
    $$ LANGUAGE plpgsql;
    