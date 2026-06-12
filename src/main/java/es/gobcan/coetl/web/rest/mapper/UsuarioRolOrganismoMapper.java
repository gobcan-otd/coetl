package es.gobcan.coetl.web.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import es.gobcan.coetl.domain.UsuarioRolOrganismo;
import es.gobcan.coetl.web.rest.dto.UsuarioRolOrganismoDTO;

@Mapper(componentModel = "spring", uses = {})
public interface UsuarioRolOrganismoMapper {

    UsuarioRolOrganismoMapper INSTANCIA= Mappers.getMapper(UsuarioRolOrganismoMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "organismo", target = "organismo")
    @Mapping(source = "rol", target = "rol")
    UsuarioRolOrganismoDTO toDto(UsuarioRolOrganismo entity);
    
    @Mapping(source = "id", target = "id")
    @Mapping(source = "organismo", target = "organismo")
    @Mapping(source = "rol", target = "rol")
    UsuarioRolOrganismo toEntity(UsuarioRolOrganismoDTO entity);

}
