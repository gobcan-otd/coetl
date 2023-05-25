package es.gobcan.coetl.service;

import java.util.List;

import es.gobcan.coetl.domain.UsuarioRolOrganismo;

public interface UsuarioRolOrganismoService {

    public boolean hasOrganismosOnlyLector(List<UsuarioRolOrganismo> organismosUsuario);
    public List<UsuarioRolOrganismo> editPermisos(List<UsuarioRolOrganismo> permisosNuevos, List<UsuarioRolOrganismo> permisosActuales);
    public void delete(List<UsuarioRolOrganismo> permisos);
    public List<UsuarioRolOrganismo> create(List<UsuarioRolOrganismo> permisosNuevos);

}
