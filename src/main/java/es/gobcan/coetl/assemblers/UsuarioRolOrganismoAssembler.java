package es.gobcan.coetl.assemblers;

import java.util.ArrayList;
import java.util.List;

import es.gobcan.coetl.domain.UsuarioRolOrganismo;
import es.gobcan.coetl.web.rest.dto.UsuarioRolOrganismoDTO;

public class UsuarioRolOrganismoAssembler {
    
    private UsuarioRolOrganismoAssembler() {
        super();
    }

    public static List<UsuarioRolOrganismoDTO> usuarioRolOrganismoToDTO(List<UsuarioRolOrganismo> permisosUsuario) {
        List<UsuarioRolOrganismoDTO> resultado = new ArrayList<>();
        if (permisosUsuario.isEmpty()) {
            return new ArrayList<>();
        }

        for (UsuarioRolOrganismo permiso : permisosUsuario) {
            UsuarioRolOrganismoDTO nuevo = new UsuarioRolOrganismoDTO(permiso.getIdUsuario(), permiso.getRol().getId(), permiso.getIdOrganismo());
            nuevo.setIdUsuario(permiso.getIdUsuario());
            nuevo.setIdRol(permiso.getRol().getId());
            nuevo.setIdOrganismo(permiso.getIdOrganismo());
            resultado.add(nuevo);
        }

        return resultado;
    }

    public static List<UsuarioRolOrganismo> usuarioRolOrganismoDTOToEntity(List<UsuarioRolOrganismoDTO> permisosUsuario) {
        List<UsuarioRolOrganismo> resultado = new ArrayList<>();
        if (permisosUsuario.isEmpty()) {
            return new ArrayList<>();
        }

        for (UsuarioRolOrganismoDTO permiso : permisosUsuario) {
            UsuarioRolOrganismo nuevo = new UsuarioRolOrganismo();
            nuevo.setIdUsuario(permiso.getIdUsuario());
            nuevo.setIdRol(permiso.getIdRol());
            nuevo.setIdOrganismo(permiso.getIdOrganismo());
            resultado.add(nuevo);
        }

        return resultado;
    }

}
