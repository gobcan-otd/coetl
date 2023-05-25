package es.gobcan.coetl.web.rest.mapper;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import es.gobcan.coetl.domain.Organismo;
import es.gobcan.coetl.repository.OrganismoRepository;
import es.gobcan.coetl.web.rest.dto.OrganismoDTO;

@Mapper(componentModel = "spring")
public abstract class OrganismoMapper implements EntityMapper<OrganismoDTO, Organismo> {

    @Autowired
    OrganismoRepository organismoRepository;

    @Override
    public OrganismoDTO toDto(Organismo entity) {
        if (entity == null) {
            return null;
        }

        OrganismoDTO dto = new OrganismoDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());

        return dto;
    }

    @Override
    public Organismo toEntity(OrganismoDTO organismoDto) {
        if (organismoDto == null) {
            return null;
        }

        Organismo organismo = organismoDto.getId() != null ? fromId(organismoDto.getId()) : new Organismo();
        organismo.setName(organismoDto.getName());
        organismo.setDescription(organismoDto.getDescription());

        return organismo;
    }

    public Organismo fromId(Long id) {
        return id != null ? organismoRepository.findOne(id) : null;
    }

}
