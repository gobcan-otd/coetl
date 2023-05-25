package es.gobcan.coetl.service.validator;

import java.util.ArrayList;
import java.util.List;

import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.domain.UsuarioRolOrganismo;
import es.gobcan.coetl.errors.CustomParameterizedExceptionBuilder;
import es.gobcan.coetl.errors.ErrorConstants;

public class UserValidator {

    private static final String ERROR_USER_HAS_ROL_AND_ORGANISM = "Un usuario debe tener al menos 1 rol y organismo";
    private static final String ERROR_USER_ADMIN_HAS_ROL_AND_ORGANISM = "Un usuario administrador no se asocia a ningún organismo";
    private static final String ERROR_ONE_ROL_IS_IN_ONLY_ORGANISMO = "Un usuario no puede tener configurado un mismo organismo en más de un rol";

    public UserValidator() {
        super();
    }

    public void validatIfUserHasAtLeastOneRolOrganismoOrIsAdmin(Usuario usuario) {
    	if (usuario.getIsAdmin() && !usuario.getUsuarioRolOrganismo().isEmpty()) {
    		throw new CustomParameterizedExceptionBuilder().message(ERROR_USER_ADMIN_HAS_ROL_AND_ORGANISM).code(ErrorConstants.USER_ADMIN_ROL_ORGANISMOS_NOT_EMPTY).build();
    	} else if (!usuario.getIsAdmin() && usuario.getUsuarioRolOrganismo().isEmpty()) {
            throw new CustomParameterizedExceptionBuilder().message(ERROR_USER_HAS_ROL_AND_ORGANISM).code(ErrorConstants.USER_ROL_ORGANISMOS_NOT_EMPTY).build();
        }
    }

    public void validatIfRolOrganismoIsJustOnce(List<UsuarioRolOrganismo> permisos) {
        List<UsuarioRolOrganismo> aux = new ArrayList<>();
        if (!permisos.isEmpty()) {
            for (UsuarioRolOrganismo permiso : permisos) {
                if (aux.stream().map(r -> r.getOrganismo()).anyMatch(e -> e.getName().equals(permiso.getOrganismo().getName()))) {
                    throw new CustomParameterizedExceptionBuilder().message(ERROR_ONE_ROL_IS_IN_ONLY_ORGANISMO).code(ErrorConstants.USER_ROL_ORGANISMOS_JUST_ONCE).build();
                }
                aux.add(permiso);
            }
        }
    }

}
