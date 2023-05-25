package es.gobcan.coetl.web.rest.mapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.repository.UsuarioRepository;
import es.gobcan.coetl.web.rest.dto.UsuarioDTO;
import es.gobcan.coetl.web.rest.vm.ManagedUserVM;

@Service
public class UsuarioMapper {

    @Autowired
    UsuarioRepository usuarioRepository;

    public UsuarioDTO userToUserDTO(Usuario user) {
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
        usuarioDTO.setUsuarioRolOrganismo(user.getUsuarioRolOrganismo());
        usuarioDTO.setDeletionDate(user.getDeletionDate());
        usuarioDTO.setDeletedBy(user.getDeletedBy());
        usuarioDTO.setOptLock(user.getOptLock());
        return usuarioDTO;
    }

    public List<UsuarioDTO> usersToUserDTOs(List<Usuario> users) {
        return users.stream().filter(Objects::nonNull).map(this::userToUserDTO).collect(Collectors.toList());
    }

    public Usuario userDTOToUser(UsuarioDTO userDTO) {
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
            user.setUsuarioRolOrganismo(userDTO.getUsuarioRolOrganismo());

            user.setOptLock(userDTO.getOptLock());
            return user;
        }
    }

    public List<Usuario> userDTOsToUsers(List<UsuarioDTO> userDTOs) {
        return userDTOs.stream().filter(Objects::nonNull).map(this::userDTOToUser).collect(Collectors.toList());
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
        user.setUsuarioRolOrganismo(userVM.getUsuarioRolOrganismo());
        return user;
    }
}
