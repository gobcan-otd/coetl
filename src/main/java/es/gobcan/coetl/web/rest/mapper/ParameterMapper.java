package es.gobcan.coetl.web.rest.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import es.gobcan.coetl.config.AESProperties;
import es.gobcan.coetl.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import es.gobcan.coetl.domain.Parameter;
import es.gobcan.coetl.repository.ParameterRepository;
import es.gobcan.coetl.web.rest.dto.ParameterDTO;

@Component
public class ParameterMapper {

    @Autowired
    private ParameterRepository parameterRepository;

    @Autowired
    private EtlMapper etlMapper;

    @Autowired
    private AESProperties aesProperties;

    public Parameter fromId(Long id) {
        return id != null ? parameterRepository.findOne(id) : null;
    }

    public Parameter toEntity(ParameterDTO dto) {
        if (dto == null) {
            return null;
        }

        Parameter entity = dto.getId() != null ? fromId(dto.getId()) : new Parameter();

        entity.setKey(dto.getKey());
        entity.setValue(setEncodeValueByTypology(dto.getTypology(),dto.getValue()));
        entity.setType(dto.getType());
        entity.setTypology(dto.getTypology());
        if(entity.getType() != Parameter.Type.GLOBAL) {
            entity.setEtl(etlMapper.fromId(dto.getEtlId()));
        }
        entity.setOptLock(dto.getOptLock());
        entity.setDescription(dto.getDescription());

        return entity;
    }

    public ParameterDTO toDto(Parameter entity) {
        if (entity == null) {
            return null;
        }

        ParameterDTO dto = new ParameterDTO();
        dto.setId(entity.getId());
        dto.setKey(entity.getKey());
        dto.setValue(entity.getValue());
        dto.setType(entity.getType());
        dto.setTypology(entity.getTypology());

        if(entity.getType() != Parameter.Type.GLOBAL) {
            dto.setEtlId(entity.getEtl().getId());
        }

        dto.setOptLock(entity.getOptLock());
        dto.setDescription(entity.getDescription());

        return dto;
    }

    public List<ParameterDTO> toDto(List<Parameter> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }

        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    private String setEncodeValueByTypology(Parameter.Typology typology, String value){
        if(Parameter.Typology.PASSWORD.equals(typology)){
            String encodeValue = SecurityUtils.passwordEncoder(value, aesProperties);
            return encodeValue;
        }
        return value;
    }
}
