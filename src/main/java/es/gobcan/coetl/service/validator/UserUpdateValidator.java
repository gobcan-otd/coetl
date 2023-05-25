package es.gobcan.coetl.service.validator;

import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.errors.CustomParameterizedExceptionBuilder;
import es.gobcan.coetl.errors.ErrorConstants;
import es.gobcan.coetl.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

@Component
public class UserUpdateValidator extends AbstractValidator<Usuario> {

    private final UsuarioRepository usuarioRepository;

    public UserUpdateValidator(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void validate(Usuario entity) {
        checkEmailUser(entity);
    }

    private void checkEmailUser(Usuario entity) {
        if(usuarioRepository.existsUsuarioByEmailAndIdIsNot(entity.getEmail().toLowerCase(), entity.getId())){
            throw new CustomParameterizedExceptionBuilder().message("User exist").code(ErrorConstants.USUARIO_EXISTE).build();
        }
    }

}
