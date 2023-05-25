package es.gobcan.coetl.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import es.gobcan.coetl.assemblers.UsuarioRolOrganismoAssembler;
import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.domain.UsuarioRolOrganismo;
import es.gobcan.coetl.errors.CustomParameterizedExceptionBuilder;
import es.gobcan.coetl.errors.ErrorConstants;
import es.gobcan.coetl.repository.UsuarioRepository;
import es.gobcan.coetl.security.SecurityUtils;
import es.gobcan.coetl.service.UsuarioService;
import es.gobcan.coetl.service.validator.UserCreatorValidator;
import es.gobcan.coetl.service.validator.UserUpdateValidator;
import es.gobcan.coetl.service.validator.UserValidator;
import es.gobcan.coetl.web.rest.dto.UsuarioRolOrganismoDTO;
import es.gobcan.coetl.web.rest.util.QueryUtil;
import es.gobcan.coetl.web.rest.vm.ManagedUserVM;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final UserCreatorValidator userCreatorValidator;
    private final UserUpdateValidator userUpdateValidator;
    private UserValidator userValidator;

    private QueryUtil queryUtil;

    public UsuarioServiceImpl(UsuarioRepository userRepository, QueryUtil queryUtil, UserCreatorValidator userValidator, 
            UserUpdateValidator userUpdateValidator) {
        this.usuarioRepository = userRepository;
        this.queryUtil = queryUtil;
        this.userCreatorValidator = userValidator;
        this.userUpdateValidator = userUpdateValidator;
    }

    public Usuario createUsuario(@NotNull Usuario user) {
        userValidator = new UserValidator();
        userCreatorValidator.validate(user);
        userValidator.validatIfUserHasAtLeastOneRolOrganismoOrIsAdmin(user);
        userValidator.validatIfRolOrganismoIsJustOnce(user.getUsuarioRolOrganismo());
        Usuario newUser = usuarioRepository.saveAndFlush(user);
        log.debug("Creada informaicón para el usuario: {}", newUser);
        return newUser;
    }

    public void updateUsuario(String firstName, String apellido1, String apellido2, String email) {
        usuarioRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).ifPresent(user -> {
            user.setNombre(firstName);
            user.setApellido1(apellido1);
            user.setApellido2(apellido2);
            user.setEmail(email);
            log.debug("Cambiado información para el usuario: {}", user);
        });
    }

    @Transactional
    public Usuario updateUsuario(Usuario user) {
        userValidator = new UserValidator();
        userUpdateValidator.validate(user);
        userValidator.validatIfUserHasAtLeastOneRolOrganismoOrIsAdmin(user);
        userValidator.validatIfRolOrganismoIsJustOnce(user.getUsuarioRolOrganismo());
        for (UsuarioRolOrganismo r : user.getUsuarioRolOrganismo()) {
            r.setIdRol(r.getRol().getId());
            r.setIdOrganismo(r.getOrganismo().getId());
            r.setIdUsuario(user.getId());
        }
        return usuarioRepository.saveAndFlush(user);
    }

    public void deleteUsuario(String login) {
        usuarioRepository.findOneByLoginAndDeletionDateIsNull(login).ifPresent(user -> {
            user.setDeletionDate(Instant.now());
            user.setDeletedBy(SecurityUtils.getCurrentUserLogin());
            usuarioRepository.saveAndFlush(user);
            log.debug("Eliminado Usuario: {}", user);
        });
    }

    public void restore(Usuario usuario) {
        if (usuario == null) {
            final String message = "Usuario no válido";
            final String code = ErrorConstants.USUARIO_NO_VALIDO;
            throw new CustomParameterizedExceptionBuilder().message(message).code(code).build();
        }
        usuario.setDeletionDate(null);
        usuario.setDeletedBy(null);
        usuarioRepository.saveAndFlush(usuario);
        log.debug("Restaurado usuario: {}", usuario);
    }

    public Page<Usuario> getAllUsuarios(Pageable pageable, Boolean includeDeleted, String query) {
        DetachedCriteria criteria = buildUsuarioCriteria(pageable, includeDeleted, query);
        return usuarioRepository.findAll(criteria, pageable);
    }

    private DetachedCriteria buildUsuarioCriteria(Pageable pageable, Boolean includeDeleted, String query) {
        StringBuilder queryBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(query)) {
            queryBuilder.append(query);
        }
        String finalQuery = getFinalQuery(includeDeleted, queryBuilder);
        return queryUtil.queryToUserCriteria(pageable, finalQuery);
    }

    private String getFinalQuery(Boolean includeDeleted, StringBuilder queryBuilder) {
        String finalQuery = queryBuilder.toString();
        if (BooleanUtils.isTrue(includeDeleted)) {
            finalQuery = queryUtil.queryIncludingDeleted(finalQuery);
        }
        return finalQuery;
    }

    public Optional<Usuario> getUsuarioWithAuthoritiesByLogin(String login, Boolean includeDeleted) {
        if (BooleanUtils.isTrue(includeDeleted)) {
            return usuarioRepository.findOneByLogin(login);
        } else {
            return usuarioRepository.findOneByLoginAndDeletionDateIsNull(login);
        }
    }

    public Usuario getUsuarioWithAuthorities(Long id) {
        return usuarioRepository.findOneWithRolesByIdAndDeletionDateIsNull(id);
    }

    public Usuario getUsuarioWithAuthorities() {
        Usuario returnValue = usuarioRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).orElse(new Usuario());
        if (returnValue.isDeleted()) {
            returnValue.setUsuarioRolOrganismo(new ArrayList<>());
        }

        return returnValue;
    }

    @Override
    public String[] getNotRepeatEmailsUsuarioAdmin() {
        List<Usuario> usuarios = usuarioRepository.findByIsAdminTrue();
        Set<String> usuarioEmails = usuarios.stream()
                .filter(user -> user.getIsAdmin()).map(usu -> usu.getEmail())
                .collect(Collectors.toSet());

        String[] emails = usuarioEmails.toArray(new String[usuarioEmails.size()]);
        return emails;
    }

    @Override
    public List<UsuarioRolOrganismoDTO> getPermisosUsuario(String login) {
        Optional<Usuario> user = usuarioRepository.findOneByLogin(login);
        return user.isPresent() ?
         UsuarioRolOrganismoAssembler.usuarioRolOrganismoToDTO(user.get().getUsuarioRolOrganismo()) : null;
    }

    @Override
    public void setPermission(ManagedUserVM managedUserVM, Long userId) {
        for (UsuarioRolOrganismo r : managedUserVM.getUsuarioRolOrganismo()) {
            r.setIdRol(r.getRol().getId());
            r.setIdOrganismo(r.getOrganismo().getId());
            r.setIdUsuario(userId);
        }
    }

}
