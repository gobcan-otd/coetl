package es.gobcan.coetl.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

import es.gobcan.coetl.config.AuditConstants;
import es.gobcan.coetl.config.Constants;
import es.gobcan.coetl.config.audit.AuditEventPublisher;
import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.domain.UsuarioRolOrganismo;
import es.gobcan.coetl.errors.ErrorConstants;
import es.gobcan.coetl.repository.UsuarioRepository;
import es.gobcan.coetl.service.MailService;
import es.gobcan.coetl.service.UsuarioRolOrganismoService;
import es.gobcan.coetl.service.UsuarioService;
import es.gobcan.coetl.web.rest.dto.UsuarioDTO;
import es.gobcan.coetl.web.rest.dto.UsuarioRolOrganismoDTO;
import es.gobcan.coetl.web.rest.mapper.UsuarioMapper;
import es.gobcan.coetl.web.rest.util.HeaderUtil;
import es.gobcan.coetl.web.rest.util.PaginationUtil;
import es.gobcan.coetl.web.rest.vm.ManagedUserVM;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api")
public class UsuarioResource extends AbstractResource {

    private final Logger log = LoggerFactory.getLogger(UsuarioResource.class);

    private static final String ENTITY_NAME = "userManagement";

    private final UsuarioRepository userRepository;

    private final MailService mailService;

    private final UsuarioService usuarioService;

    private UsuarioMapper usuarioMapper;

    private AuditEventPublisher auditPublisher;

    private final UsuarioRolOrganismoService usuarioRolOrganismoService;

    public UsuarioResource(UsuarioRepository userRepository, MailService mailService, UsuarioService userService, UsuarioMapper userMapper,
            AuditEventPublisher auditPublisher, UsuarioRolOrganismoService usuarioRolOrganismoService) {

        this.userRepository = userRepository;
        this.mailService = mailService;
        this.usuarioService = userService;
        this.usuarioMapper = userMapper;
        this.auditPublisher = auditPublisher;
        this.usuarioRolOrganismoService = usuarioRolOrganismoService;
    }

    @SuppressWarnings("rawtypes")
    @PostMapping("/usuarios")
    @Timed
    @PreAuthorize("@secChecker.puedeCrearUsuario(authentication)")
    public ResponseEntity createUser(@Valid @RequestBody ManagedUserVM managedUserVM) throws URISyntaxException {
        log.debug("REST Petición para guardar User : {}", managedUserVM);
        if (managedUserVM.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, ErrorConstants.ID_EXISTE, "Un usuario no puede tener ID")).body(null);
        }  else {
            Usuario newUser = usuarioService.createUsuario(usuarioMapper.userDTOToUser(managedUserVM));
            usuarioService.setPermission(managedUserVM, newUser.getId());
            List<UsuarioRolOrganismo> permisos = this.usuarioRolOrganismoService.create(managedUserVM.getUsuarioRolOrganismo());
            newUser.setUsuarioRolOrganismo(permisos);
            mailService.sendCreationEmail(newUser);
            auditPublisher.publish(AuditConstants.USUARIO_CREACION, managedUserVM.getLogin());
            return ResponseEntity.created(new URI("/api/usuarios/" + newUser.getLogin())).headers(HeaderUtil.createAlert("userManagement.created", newUser.getLogin())).body(newUser);
        }
    }

    @PutMapping("/usuarios")
    @Timed
    @PreAuthorize("@secChecker.puedeModificarUsuario(authentication, #managedUserVM?.login)")
    public ResponseEntity<UsuarioDTO> updateUser(@Valid @RequestBody ManagedUserVM managedUserVM) {
        log.debug("REST petición para actualizar User : {}", managedUserVM);
        Optional<Usuario> existingUser = userRepository.findOneByLogin(managedUserVM.getLogin().toLowerCase());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(managedUserVM.getId()))) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, ErrorConstants.USUARIO_EXISTE, "Login ya en uso")).body(null);
        }
        if (!existingUser.isPresent()) {
            existingUser = Optional.ofNullable(userRepository.findOne(managedUserVM.getId()));
        }

        Usuario usuario = usuarioMapper.updateFromDTO(existingUser.orElse(null), managedUserVM);
        usuario = usuarioService.updateUsuario(usuario);
        Optional<UsuarioDTO> updatedUser = Optional.ofNullable(usuarioMapper.userToUserDTO(usuario));

        auditPublisher.publish(AuditConstants.USUARIO_EDICION, managedUserVM.getLogin());
        return ResponseUtil.wrapOrNotFound(updatedUser, HeaderUtil.createAlert("userManagement.updated", managedUserVM.getLogin()));
    }

    @GetMapping("/usuarios")
    @Timed
    @PreAuthorize("@secChecker.puedeConsultarUsuario(authentication)")
    public ResponseEntity<List<UsuarioDTO>> getAllUsers(@ApiParam Pageable pageable, @ApiParam(defaultValue = "false") Boolean includeDeleted, @ApiParam(required = false) String query) {
        final Page<UsuarioDTO> page = usuarioService.getAllUsuarios(pageable, includeDeleted, query).map(usuarioMapper::userToUserDTO);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/usuarios");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/usuarios/{login:" + Constants.LOGIN_REGEX + "}")
    @Timed
    @PreAuthorize("@secChecker.puedeConsultarUsuario(authentication)")
    public ResponseEntity<UsuarioDTO> getUser(@PathVariable String login, @ApiParam(required = false, defaultValue = "false") Boolean includeDeleted) {
        log.debug("REST petición para obtener  User : {}", login);
        return ResponseUtil.wrapOrNotFound(usuarioService.getUsuarioWithAuthoritiesByLogin(login, includeDeleted).map(usuarioMapper::userToUserDTO));
    }

    @GetMapping("/usuarios/{login:" + Constants.LOGIN_REGEX + "}/permisos")
    @Timed
    @PreAuthorize("@secChecker.puedeConsultarUsuario(authentication)")
    public ResponseEntity<List<UsuarioRolOrganismoDTO>> getPermisos(@PathVariable String login, @ApiParam(required = false, defaultValue = "false") Boolean includeDeleted) {
        log.debug("REST petición para obtener  User : {}", login);
        return ResponseEntity.ok(usuarioService.getPermisosUsuario(login));
    }

    @DeleteMapping("/usuarios/{login:" + Constants.LOGIN_REGEX + "}")
    @Timed
    @PreAuthorize("@secChecker.puedeBorrarUsuario(authentication)")
    public ResponseEntity<Void> deleteUser(@PathVariable String login) {
        log.debug("REST request to delete User: {}", login);
        usuarioService.deleteUsuario(login);
        auditPublisher.publish(AuditConstants.USUARIO_DESACTIVACION, login);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, login)).build();
    }

    @PutMapping("/usuarios/{login}/restore")
    @Timed
    @PreAuthorize("@secChecker.puedeModificarUsuario(authentication, #login)")
    public ResponseEntity<UsuarioDTO> updateUser(@Valid @PathVariable String login) {
        log.debug("REST request to restore User : {}", login);

        Optional<Usuario> deletedUser = userRepository.findOneByLogin(login.toLowerCase());
        if (deletedUser.isPresent()) {
            usuarioService.restore(deletedUser.get());
        }

        Optional<UsuarioDTO> updatedUser = Optional.ofNullable(usuarioMapper.userToUserDTO(deletedUser.orElse(null)));

        auditPublisher.publish(AuditConstants.USUARIO_ACTIVACION, login);
        return ResponseUtil.wrapOrNotFound(updatedUser, HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, login));
    }

    @GetMapping("/autenticar")
    @Timed
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST petición para comprobar si el usuario actual está autenticado");
        return request.getRemoteUser();
    }

    @GetMapping("/usuario")
    @Timed
    public ResponseEntity<UsuarioDTO> getAccount() {
        Usuario databaseUser = usuarioService.getUsuarioWithAuthorities();

        if (databaseUser == null) {
            return new ResponseEntity<>((UsuarioDTO) null, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(usuarioMapper.userToUserDTO(databaseUser), HttpStatus.OK);
        }
    }
}
