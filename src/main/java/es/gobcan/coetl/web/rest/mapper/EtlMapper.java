package es.gobcan.coetl.web.rest.mapper;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Execution;
import es.gobcan.coetl.domain.Execution.Result;
import es.gobcan.coetl.repository.EtlRepository;
import es.gobcan.coetl.repository.ExecutionRepository;
import es.gobcan.coetl.web.rest.dto.EtlBaseDTO;
import es.gobcan.coetl.web.rest.dto.EtlDTO;

@Mapper(componentModel = "spring")
public abstract class EtlMapper implements EntityMapper<EtlDTO, Etl> {

    @Autowired
    private EtlRepository etlRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    public Etl fromId(Long id) {
        return etlRepository.findOne(id);
    }

    @Override
    public Etl toEntity(EtlDTO dto) {
        if (dto == null) {
            return null;
        }

        Etl entity = (dto.getId() != null) ? fromId(dto.getId()) : new Etl();

        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setPurpose(dto.getPurpose());
        entity.setOrganizationInCharge(dto.getOrganizationInCharge());
        entity.setFunctionalInCharge(dto.getFunctionalInCharge());
        entity.setTechnicalInCharge(dto.getTechnicalInCharge());
        entity.setType(dto.getType());
        entity.setComments(dto.getComments());
        entity.setExecutionDescription(dto.getExecutionDescription());
        entity.setExecutionPlanning(dto.getExecutionPlanning());
        entity.setNextExecution(dto.getNextExecution());
        entity.setVisibility(dto.isVisibility());

        entity.setUriRepository(dto.getUriRepository());

        entity.setOptLock(dto.getOptLock());

        return entity;
    }

    public EtlBaseDTO toBaseDto(Etl entity, Execution execution) {
        if (entity == null) {
            return null;
        }

        EtlBaseDTO baseDto = new EtlBaseDTO();

        baseDto.setId(entity.getId());
        baseDto.setCode(entity.getCode());
        baseDto.setName(entity.getName());
        baseDto.setOrganizationInCharge(entity.getOrganizationInCharge());
        baseDto.setType(entity.getType());
        baseDto.setExecutionPlanning(entity.getExecutionPlanning());
        baseDto.setNextExecution(entity.getNextExecution());
        setDataExecution(execution, baseDto);

        baseDto.setCreatedBy(entity.getCreatedBy());
        baseDto.setCreatedDate(entity.getCreatedDate());
        baseDto.setLastModifiedBy(entity.getLastModifiedBy());
        baseDto.setLastModifiedDate(entity.getLastModifiedDate());
        baseDto.setDeletedBy(entity.getDeletedBy());
        baseDto.setDeletionDate(entity.getDeletionDate());
        baseDto.setVisibility(entity.isVisibility());

        baseDto.setOptLock(entity.getOptLock());

        return baseDto;
    }

    private void setDataExecution(Execution execution, EtlBaseDTO baseDto) {
        if (execution != null) {
            baseDto.setLastExecution(execution.getStartDate());
            baseDto.setResult(execution.getResult());
        }
    }

    public EtlBaseDTO toBaseDto(Etl entity, String lastExecutionStartDate, String lastExecutionResult) {
        Execution execution;
        if (StringUtils.isNotBlank(lastExecutionResult) && StringUtils.isNotBlank(lastExecutionStartDate)) {
            execution = executionRepository.findFirstByEtlIdAndPlanningDateAndResultOrderByIdDesc(entity.getId(), lastExecutionStartDate, lastExecutionResult);
        } else if (StringUtils.isNotBlank(lastExecutionResult) && StringUtils.isBlank(lastExecutionStartDate)) {
            execution = executionRepository.findFirstByEtlIdAndResultOrderByIdDesc(entity.getId(), Result.valueOf(lastExecutionResult));
        } else if (StringUtils.isBlank(lastExecutionResult) && StringUtils.isNotBlank(lastExecutionStartDate)) {
            execution = executionRepository.findFirstByEtlIdAndPlanningDateOrderByIdDesc(entity.getId(), lastExecutionStartDate);
        } else {
            execution = executionRepository.findFirstByEtlIdOrderByPlanningDateDesc(entity.getId());
        }
        return toBaseDto(entity, execution);
    }

}
