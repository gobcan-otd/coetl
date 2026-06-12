package es.gobcan.coetl.web.rest.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.domain.UsuarioRolOrganismo;
import es.gobcan.coetl.repository.EtlRepository;
import es.gobcan.coetl.repository.UsuarioRepository;
import es.gobcan.coetl.repository.UsuarioRolOrganismoRepository;
import es.gobcan.coetl.web.rest.dto.UsuarioDTO;
import es.gobcan.coetl.web.rest.dto.UsuarioRolOrganismoDTO;
import es.gobcan.coetl.web.rest.vm.ManagedUserVM;

@Mapper(componentModel = "spring", uses = { UsuarioRolOrganismoMapper.class })
public abstract class UsuarioMapper implements EntityMapper<UsuarioDTO, Usuario> {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    private EtlRepository etlRepository;

    @Autowired
    private UsuarioRolOrganismoRepository usuarioRolOrganismoRepository;

    @Autowired
    private UsuarioRolOrganismoMapper usuarioRolOrganismoMapper;

    public UsuarioDTO toDto(Usuario user) {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(user.getId());
        usuarioDTO.setLogin(user.getLogin());
        usuarioDTO.setNombre(user.getNombre());
        usuarioDTO.setApellido1(user.getApellido1());
        usuarioDTO.setApellido2(user.getApellido2());
        usuarioDTO.setEmail(user.getEmail());
        usuarioDTO.setIsAdmin(user.getIsAdmin());
        usuarioDTO.setCreatedBy(user.getCreatedBy());
        usuarioDTO.setCreatedDate(user.getCreatedDate());
        usuarioDTO.setLastModifiedBy(user.getLastModifiedBy());
        usuarioDTO.setLastModifiedDate(user.getLastModifiedDate());
        usuarioDTO.setDeletionDate(user.getDeletionDate());
        usuarioDTO.setDeletedBy(user.getDeletedBy());
        usuarioDTO.setOptLock(user.getOptLock());
        usuarioDTO.setUsuarioRolOrganismo(user.getUsuarioRolOrganismo().stream().map(e -> usuarioRolOrganismoMapper.toDto(e)).collect(Collectors.toList()));
        usuarioDTO.setEtls(user.getEtls());
        usuarioDTO.setAllEtlAccess(user.getAllEtlAccess());
        return usuarioDTO;
    }

    public List<UsuarioDTO> usersToUserDTOs(List<Usuario> users) {
        return users.stream().filter(Objects::nonNull).map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Usuario toEntity(UsuarioDTO userDTO) {
        if (userDTO == null) {
            return null;
        } else {
            Usuario user = (userDTO.getId() == null) ? new Usuario() : userFromId(userDTO.getId());
            user.setLogin(userDTO.getLogin());
            user.setNombre(userDTO.getNombre());
            user.setApellido1(userDTO.getApellido1());
            user.setApellido2(userDTO.getApellido2());
            user.setEmail(userDTO.getEmail());
            user.setIsAdmin(userDTO.getIsAdmin());
            if (userDTO.getUsuarioRolOrganismo().isEmpty()) {
                user.setUsuarioRolOrganismo(new ArrayList<UsuarioRolOrganismo>());
            } else {
                user.setUsuarioRolOrganismo(usuariosRolOrganismoToEntity(userDTO.getUsuarioRolOrganismo(), user));
            }
            user.setEtls(setDatasEtlsUsuario(userDTO.getEtls()));
            user.setAllEtlAccess(userDTO.getAllEtlAccess());

            user.setOptLock(userDTO.getOptLock());
            return user;
        }
    }

    public List<Usuario> userDTOsToUsers(List<UsuarioDTO> userDTOs) {
        return userDTOs.stream().filter(Objects::nonNull).map(this::toEntity).collect(Collectors.toList());
    }

    private Usuario userFromId(Long id) {
        return usuarioRepository.findOne(id);
    }

    public Usuario updateFromDTO(Usuario user, ManagedUserVM userVM) {
        if (user == null) {
            return null;
        }
        user.setLogin(userVM.getLogin());
        user.setNombre(userVM.getNombre());
        user.setApellido1(userVM.getApellido1());
        user.setApellido2(userVM.getApellido2());
        user.setEmail(userVM.getEmail());
        user.setIsAdmin(userVM.getIsAdmin());
        user.setOptLock(userVM.getOptLock());
        user.setUsuarioRolOrganismo(usuariosRolOrganismoToEntity(userVM.getUsuarioRolOrganismo(), user));
        user.setEtls(setDatasEtlsUsuario(userVM.getEtls()));
        user.setAllEtlAccess(userVM.getAllEtlAccess());
        return user;
    }

    public List<Etl> setDatasEtlsUsuario(List<Etl> etls) {
        return etls.stream().filter(Objects::nonNull).map(etl -> setDataEtlUsuario(etl)).collect(Collectors.toList());
    }

    public Etl setDataEtlUsuario(Etl etl) {
        if (etl == null) {
            return null;
        } else {
            return etlRepository.findOne(etl.getId());
        }
    }

    public List<UsuarioRolOrganismo> usuariosRolOrganismoToEntity(List<UsuarioRolOrganismoDTO> usuariosRolOrganismo, Usuario usuario) {
        return usuariosRolOrganismo.stream().filter(Objects::nonNull).map(usuarioRolOrganismo -> usuarioRolOrganismoToEntity(usuarioRolOrganismo, usuario))
                .collect(Collectors.toList());
    }

    public UsuarioRolOrganismo usuarioRolOrganismoToEntity(UsuarioRolOrganismoDTO usuarioRolOrganismo, Usuario usuario) {
        if (usuarioRolOrganismo == null) {
            return null;
        } else if (usuarioRolOrganismo.getId() == null) {
            UsuarioRolOrganismo usuarioRolOrganismotmp = new UsuarioRolOrganismo();
            usuarioRolOrganismotmp.setUsuario(usuario);
            usuarioRolOrganismotmp.setRol(usuarioRolOrganismo.getRol());
            usuarioRolOrganismotmp.setOrganismo(usuarioRolOrganismo.getOrganismo());
            return usuarioRolOrganismotmp;
        } else {
            return usuarioRolOrganismoRepository.findOne(usuarioRolOrganismo.getId());
        }
    }

}
