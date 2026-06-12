package es.gobcan.coetl.service.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.domain.UsuarioRolOrganismo;
import es.gobcan.coetl.errors.CustomParameterizedExceptionBuilder;
import es.gobcan.coetl.errors.ErrorConstants;
import es.gobcan.coetl.repository.UsuarioRepository;

@Component
public class UserValidator extends AbstractValidator<Usuario> {

    private static final String ERROR_USER_HAS_ROL_AND_ORGANISM = "Un usuario debe tener al menos 1 rol y organismo";
    private static final String ERROR_USER_ADMIN_HAS_ROL_AND_ORGANISM = "Un usuario administrador no se asocia a ningún organismo";
    private static final String ERROR_ONE_ROL_IS_IN_ONLY_ORGANISMO = "Un usuario no puede tener configurado un mismo organismo en más de un rol";
    private static final String ERROR_USER_RESTRICTED_ACCESS_ETL_NO_ETL_SELECTED = "El usuario no tiene permisos para acceder a todas las ETL, debe seleccionar al menos una ETL a la que puede acceder";
    private static final String ERROR_EMAIL_USER_ALREADY_EXIST = "El correo electrónico introducido se encuentra utilizado por otro usuario.";
    private static final String ERROR_USER_LOGIN_ALREADY_EXISTS = "El \"nombre de usuario\" introducido ya está siendo usado";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void validate(Usuario entity) {
        checkEmailUser(entity);
        checkUsernameLogin(entity);
        validateIfUserHasEtlSelected(entity.getAllEtlAccess(), entity.getEtls());
        validatIfUserHasAtLeastOneRolOrganismoOrIsAdmin(entity);
        validatIfRolOrganismoIsJustOnce(entity);
    }

    private void checkUsernameLogin(Usuario entity) {
        Optional<Usuario> existingUser = usuarioRepository.findOneByLogin(entity.getLogin().toLowerCase());
        if (entity.getId() != null && existingUser.isPresent() && (!existingUser.get().getId().equals(entity.getId()))) {
            throw new CustomParameterizedExceptionBuilder().message(ERROR_USER_LOGIN_ALREADY_EXISTS).code(ErrorConstants.USER_EXIST).build();
        }
        if (entity.getId() == null && existingUser.isPresent()) {
            throw new CustomParameterizedExceptionBuilder().message(ERROR_USER_LOGIN_ALREADY_EXISTS).code(ErrorConstants.USER_EXIST).build();
        }
    }

    private void checkEmailUser(Usuario entity) {
        if (entity.getId() == null && usuarioRepository.existsUsuarioByEmail(entity.getEmail().toLowerCase())) {
            throw new CustomParameterizedExceptionBuilder().message(ERROR_EMAIL_USER_ALREADY_EXIST).code(ErrorConstants.EMAIL_EXIST).build();
        }
        if (entity.getId() != null && usuarioRepository.existsUsuarioByEmailAndIdIsNot(entity.getEmail().toLowerCase(), entity.getId())) {
            throw new CustomParameterizedExceptionBuilder().message(ERROR_EMAIL_USER_ALREADY_EXIST).code(ErrorConstants.EMAIL_EXIST).build();
        }
    }

    public void validatIfUserHasAtLeastOneRolOrganismoOrIsAdmin(Usuario usuario) {
        if (usuario.getIsAdmin() && !usuario.getUsuarioRolOrganismo().isEmpty()) {
            throw new CustomParameterizedExceptionBuilder().message(ERROR_USER_ADMIN_HAS_ROL_AND_ORGANISM).code(ErrorConstants.USER_ADMIN_ROL_ORGANISMOS_NOT_EMPTY).build();
        } else if (!usuario.getIsAdmin() && usuario.getUsuarioRolOrganismo().isEmpty()) {
            throw new CustomParameterizedExceptionBuilder().message(ERROR_USER_HAS_ROL_AND_ORGANISM).code(ErrorConstants.USER_ROL_ORGANISMOS_NOT_EMPTY).build();
        }
    }

    public void validatIfRolOrganismoIsJustOnce(Usuario entity) {
        List<UsuarioRolOrganismo> aux = new ArrayList<>();
        if (!entity.getIsAdmin() && !entity.getUsuarioRolOrganismo().isEmpty()) {
            for (UsuarioRolOrganismo permiso : entity.getUsuarioRolOrganismo()) {
                if (aux.stream().map(r -> r.getOrganismo()).anyMatch(e -> e.getName().equals(permiso.getOrganismo().getName()))) {
                    throw new CustomParameterizedExceptionBuilder().message(ERROR_ONE_ROL_IS_IN_ONLY_ORGANISMO).code(ErrorConstants.USER_ROL_ORGANISMOS_JUST_ONCE).build();
                }
                aux.add(permiso);
            }
        }
    }

    public void validateIfUserHasEtlSelected(Boolean allEtlAccess, List<Etl> etls) {
        if (!allEtlAccess && etls.isEmpty()) {
            throw new CustomParameterizedExceptionBuilder().message(ERROR_USER_RESTRICTED_ACCESS_ETL_NO_ETL_SELECTED)
                    .code(ErrorConstants.USER_RESTRICTED_ACCESS_ETL_NO_ETL_SELECTED).build();
        }
    }

}
