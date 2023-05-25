package es.gobcan.coetl.web.rest.mapper;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import es.gobcan.coetl.domain.Roles;
import es.gobcan.coetl.repository.RolesRepository;
import es.gobcan.coetl.web.rest.dto.RolesDTO;

@Mapper(componentModel = "spring")
public abstract class RolesMapper implements EntityMapper<RolesDTO, Roles> {

    @Autowired
    private RolesRepository rolesRepository;

    @Override
    public RolesDTO toDto(Roles entity) {
        if (entity == null) {
            return null;
        }

        RolesDTO dto = new RolesDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());

        return dto;
    }

    @Override
    public Roles toEntity(RolesDTO rolesDTO) {
        if (rolesDTO == null) {
            return null;
        }

        Roles rol = rolesDTO.getId() != null ? fromId(rolesDTO.getId()) : new Roles();
        rol.setName(rolesDTO.getName());

        return rol;
    }

    public Roles fromId(Long id) {
        return id != null ? rolesRepository.findOne(id) : null;
    }

}
