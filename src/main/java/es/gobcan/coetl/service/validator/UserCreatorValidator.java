package es.gobcan.coetl.service.validator;

import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.errors.CustomParameterizedExceptionBuilder;
import es.gobcan.coetl.errors.ErrorConstants;
import es.gobcan.coetl.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

@Component
public class UserCreatorValidator extends AbstractValidator<Usuario> {

    private final UsuarioRepository usuarioRepository;

    public UserCreatorValidator(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void validate(Usuario entity) {
        checkUsernameLogin(entity);
        checkEmailUser(entity);
    }

    private void checkUsernameLogin(Usuario entity){
        if(usuarioRepository.findOneByLogin(entity.getLogin().toLowerCase()).isPresent()){
            throw new CustomParameterizedExceptionBuilder().message("User exist").code(ErrorConstants.USUARIO_EXISTE).build();
        }
    }

    private void checkEmailUser(Usuario entity) {
        if(usuarioRepository.existsUsuarioByEmail(entity.getEmail().toLowerCase())){
            throw new CustomParameterizedExceptionBuilder().message("User exist").code(ErrorConstants.USUARIO_EXISTE).build();
        }
    }

}
