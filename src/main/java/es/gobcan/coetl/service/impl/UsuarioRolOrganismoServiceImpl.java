package es.gobcan.coetl.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import es.gobcan.coetl.domain.UsuarioRolOrganismo;
import es.gobcan.coetl.domain.enumeration.Rol;
import es.gobcan.coetl.repository.UsuarioRolOrganismoRepository;
import es.gobcan.coetl.service.UsuarioRolOrganismoService;

@Service
public class UsuarioRolOrganismoServiceImpl implements UsuarioRolOrganismoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsuarioRolOrganismoServiceImpl.class);
    private final UsuarioRolOrganismoRepository usuarioRolOrganismoRepository;
    
    public UsuarioRolOrganismoServiceImpl(UsuarioRolOrganismoRepository usuarioRolOrganismoRepository) {
        this.usuarioRolOrganismoRepository = usuarioRolOrganismoRepository;
    }

    @Override
    public boolean hasOrganismosOnlyLector(List<UsuarioRolOrganismo> organismosUsuario) {
        return organismosUsuario.stream().filter(obj -> Arrays.asList(Rol.TECNICO.name()).stream()
                .anyMatch(rol -> rol.equals(obj.getRol().getName())))
                .map(org -> org.getOrganismo()).collect(Collectors.toList()).isEmpty();
    }

    @Override
    @Transactional
    public List<UsuarioRolOrganismo> editPermisos(List<UsuarioRolOrganismo> permisosNuevos, List<UsuarioRolOrganismo> permisosActuales) {
        delete(permisosActuales);
        return create(permisosNuevos);
    }

    @Override
    @Transactional
    public void delete(List<UsuarioRolOrganismo> permisos) {
        usuarioRolOrganismoRepository.delete(permisos);
        usuarioRolOrganismoRepository.flush();
    }

    @Transactional
    public List<UsuarioRolOrganismo> create(List<UsuarioRolOrganismo> permisosNuevos) {
        List<UsuarioRolOrganismo> result = new ArrayList<>();
        for (UsuarioRolOrganismo permiso : permisosNuevos) {
            result.add(usuarioRolOrganismoRepository.save(permiso));
        }
        usuarioRolOrganismoRepository.flush();
        return result;
    }

}
