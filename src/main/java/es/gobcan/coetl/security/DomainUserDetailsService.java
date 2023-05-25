package es.gobcan.coetl.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.repository.UsuarioRepository;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final UsuarioRepository userRepository;

    public DomainUserDetailsService(UsuarioRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.debug("Authenticating {}", login);
        String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);
        Optional<Usuario> userFromDatabase = userRepository.findOneByLoginAndDeletionDateIsNull(lowercaseLogin);
        if (!userFromDatabase.isPresent()) {
            return new User(lowercaseLogin, "", new ArrayList<>());
        }

        return userFromDatabase.map(user -> {
            if (user.getDeletionDate() != null) {
                throw new UserNotActivatedException("Usuario " + lowercaseLogin + " no estaba activado");
            }
            List<GrantedAuthority> permisos = new ArrayList<>(); 
            user.getUsuarioRolOrganismo().iterator().forEachRemaining(rol -> permisos.add(new SimpleGrantedAuthority(rol.getRol().getName())));
            return new User(lowercaseLogin, "", permisos);
        }).orElseThrow(() -> new UsernameNotFoundException("Usuario " + lowercaseLogin + " no encontrado en la " + "database"));
    }
}
