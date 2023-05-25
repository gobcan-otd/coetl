package es.gobcan.coetl.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.web.rest.dto.UsuarioRolOrganismoDTO;
import es.gobcan.coetl.web.rest.vm.ManagedUserVM;

public interface UsuarioService {

    Usuario createUsuario(Usuario user);

    void updateUsuario(String firstName, String apellido1, String apellido2, String email);

    Usuario updateUsuario(Usuario user);

    void deleteUsuario(String login);

    void restore(Usuario usuario);

    Page<Usuario> getAllUsuarios(Pageable pageable, Boolean includeDeleted, String query);

    Optional<Usuario> getUsuarioWithAuthoritiesByLogin(String login, Boolean includeDeleted);

    Usuario getUsuarioWithAuthorities(Long id);

    Usuario getUsuarioWithAuthorities();

    String[] getNotRepeatEmailsUsuarioAdmin();

    List<UsuarioRolOrganismoDTO> getPermisosUsuario(String login);

    public void setPermission(ManagedUserVM managedUserVM, Long userId);

}
