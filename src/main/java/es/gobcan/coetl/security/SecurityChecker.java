package es.gobcan.coetl.security;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Roles;
import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.domain.UsuarioRolOrganismo;
import es.gobcan.coetl.domain.enumeration.Rol;
import es.gobcan.coetl.repository.UsuarioRepository;
import es.gobcan.coetl.repository.UsuarioRolOrganismoRepository;
import es.gobcan.coetl.service.EtlService;

@Component("secChecker")
public class SecurityChecker {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioRolOrganismoRepository usuarioRolOrganismoRepository;

    @Autowired
    private EtlService etlService;

    public boolean puedeConsultarAuditoria(Authentication authentication) {
        return this.isAdmin(authentication);
    }

    public boolean puedeConsultarLogs(Authentication authentication) {
        return this.isAdmin(authentication);
    }

    public boolean puedeModificarLogs(Authentication authentication) {
        return this.isAdmin(authentication);
    }

    public boolean puedeConsultarUsuario(Authentication authentication) {
        return this.isAdmin(authentication);
    }

    public boolean puedeCrearUsuario(Authentication authentication) {
        return this.isAdmin(authentication);
    }

    public boolean isManageGlobalParameters(Authentication authentication) {
        return this.isAdmin(authentication);
    }

    public boolean puedeModificarUsuario(Authentication authentication, String login) {
        String userLogin = SecurityUtils.getCurrentUserLogin();
        return this.isAdmin(authentication) || (StringUtils.isNotBlank(userLogin) && StringUtils.isNotBlank(login) && userLogin.equals(login));
    }

    public boolean puedeBorrarUsuario(Authentication authentication) {
        return this.isAdmin(authentication);
    }

    public boolean canReadFile(Authentication authentication) {
        return this.isAdmin(authentication) || this.isTecnico(authentication) || this.isLector(authentication);
    }

    public boolean canManageFile(Authentication authentication) {
        return this.isAdmin(authentication) || this.isTecnico(authentication);
    }

    public boolean puedeConsultarMetrica(Authentication authentication) {
        return this.isAdmin(authentication);
    }

    public boolean puedeConsultarSalud(Authentication authentication) {
        return this.isAdmin(authentication);
    }

    public boolean puedeGestionarSalud(Authentication authentication) {
        return this.isAdmin(authentication);
    }

    public boolean puedeConsultarConfig(Authentication authentication) {
        return this.isAdmin(authentication);
    }

    public boolean puedeConsultarApi(Authentication authentication) {
        return this.isAdmin(authentication);
    }

    public boolean canReadEtl(Authentication authentication) {
        return this.isAdmin(authentication) || this.isTecnico(authentication) || this.isLector(authentication);
    }

    public boolean canCreateEtl(Authentication authentication) {
        return this.isAdmin(authentication) || this.isTecnico(authentication);
    }
    
    public boolean canManageOrganismo(Authentication authentication) {
        return this.isAdmin(authentication);
    }

    public boolean canSeeAllEtls(Authentication authentication) {
        return this.isAdmin(authentication);
    }

    private boolean isAdmin(Authentication authentication) {
    	String login = SecurityUtils.getCurrentUserLogin();
        Optional<Usuario> user = usuarioRepository.findOneByLogin(login);
        return user.isPresent() && user.get().getIsAdmin();
    }

    private boolean isTecnico(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(Rol.TECNICO.name()));
    }

    private boolean isLector(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(Rol.LECTOR.name()));
    }

    public boolean isTecnico(String rol) {
        return rol.equals(Rol.TECNICO.name());
    }

    public boolean canManageEtl(Authentication authentication, Long idEtl) {

        if (canSeeAllEtls(authentication)) {
            return true;
        }

        if (!this.isTecnico(authentication)) {
            return false;
        }

        Etl currentEtl = etlService.findOne(idEtl);
        if (currentEtl == null) {
            return false;
        }
        
        Long idOrganismoSelectedEtl = currentEtl.getOrganizationInCharge().getId();
        String login = SecurityUtils.getCurrentUserLogin();
        Optional<Usuario> user = usuarioRepository.findOneByLogin(login);

        if (!user.isPresent()) {
            return false;
        }

        List<UsuarioRolOrganismo> organismosUsuario = usuarioRolOrganismoRepository.findByIdUsuario(user.get().getId());
        List<Roles> rolesUsuario = organismosUsuario.stream().map(r -> r.getRol()).collect(Collectors.toList());

        for (int i = 0; i < rolesUsuario.size(); i++) {
            if (isTecnico(rolesUsuario.get(i).getName()) &&
                    organismosUsuario.get(i).getRol().getId().equals(rolesUsuario.get(i).getId()) &&
                    organismosUsuario.get(i).getOrganismo().getId().equals(idOrganismoSelectedEtl)) {
                return true;
            }
        }
        return false;

    }

}
