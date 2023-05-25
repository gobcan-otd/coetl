package es.gobcan.coetl.web.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

import es.gobcan.coetl.assemblers.UsuarioRolOrganismoAssembler;
import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.domain.UsuarioRolOrganismo;
import es.gobcan.coetl.repository.UsuarioRepository;
import es.gobcan.coetl.repository.UsuarioRolOrganismoRepository;
import es.gobcan.coetl.security.SecurityUtils;
import es.gobcan.coetl.service.UsuarioRolOrganismoService;
import es.gobcan.coetl.web.rest.dto.UsuarioRolOrganismoDTO;
import es.gobcan.coetl.web.rest.util.HeaderUtil;

@RestController
@RequestMapping(UsuarioRolOrganismoResource.BASE_URI)
public class UsuarioRolOrganismoResource extends AbstractResource {

    public static final String BASE_URI = "/api/usuarioRolOrganismo";
    private static final Logger LOG = LoggerFactory.getLogger(UsuarioRolOrganismoResource.class);
    private static final String ENTITY_NAME = "usuarioRolOrganismo";

    @Autowired
    UsuarioRolOrganismoService usuarioRolOrganismoService;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioRolOrganismoRepository usuarioRolOrganismoRespository;

    public UsuarioRolOrganismoResource(UsuarioRolOrganismoService usuarioRolOrganismoService, UsuarioRepository usuarioRepository,
            UsuarioRolOrganismoRepository usuarioRolOrganismoRespository) {
        this.usuarioRolOrganismoService = usuarioRolOrganismoService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolOrganismoRespository = usuarioRolOrganismoRespository;
    }

    @GetMapping("/canCreate")
    @Timed
    @PreAuthorize("@secChecker.canReadEtl(authentication)")
    public ResponseEntity<Boolean> hasOrganismosOnlyLector() {
        LOG.debug("REST Request to find si un usuario solo tiene permisos de lector para todos sus organismos ");

        String login = SecurityUtils.getCurrentUserLogin();
        Optional<Usuario> user = usuarioRepository.findOneByLogin(login);

        if (!user.isPresent()) {
            return ResponseEntity.ok().body(false);
        }

        List<UsuarioRolOrganismo> organismosUsuario = this.usuarioRolOrganismoRespository.findByIdUsuario(user.get().getId());

        return ResponseEntity.ok().body(this.usuarioRolOrganismoService.hasOrganismosOnlyLector(organismosUsuario));
    }

    @PutMapping("/{idUsuario}")
    @Timed
    @PreAuthorize("@secChecker.puedeModificarUsuario(authentication, #managedUserVM?.login)")
    public ResponseEntity<List<UsuarioRolOrganismo>> updateUser(@RequestBody List<UsuarioRolOrganismoDTO> permisos, @PathVariable Long idUsuario) {
        LOG.debug("REST petición para actualizar los permisos del usuario : {}", idUsuario);

        List<UsuarioRolOrganismo> organismosUsuario = usuarioRolOrganismoRespository.findByIdUsuario(idUsuario);
        List<UsuarioRolOrganismo> permisosNuevos = new ArrayList<>();
        permisosNuevos.addAll(UsuarioRolOrganismoAssembler.usuarioRolOrganismoDTOToEntity(permisos));
        List<UsuarioRolOrganismo> resultado = usuarioRolOrganismoService.editPermisos(permisosNuevos, organismosUsuario);

        return ResponseEntity.ok(resultado);
    }

    @DeleteMapping("/{idUsuario}")
    @Timed
    @PreAuthorize("@secChecker.puedeModificarUsuario(authentication, #managedUserVM?.login)")
    public ResponseEntity<List<UsuarioRolOrganismo>> delete(@PathVariable Long idUsuario) {
        LOG.debug("REST petición para actualizar los permisos del usuario : {}", idUsuario);

        List<UsuarioRolOrganismo> organismosUsuario = usuarioRolOrganismoRespository.findByIdUsuario(idUsuario);
        usuarioRolOrganismoService.delete(organismosUsuario);

        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, idUsuario.toString())).build();
    }

}
