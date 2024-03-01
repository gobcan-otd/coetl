package es.gobcan.coetl.web.rest;

import com.codahale.metrics.annotation.Timed;
import es.gobcan.coetl.config.AuditConstants;
import es.gobcan.coetl.config.audit.AuditEventPublisher;
import es.gobcan.coetl.domain.Parameter;
import es.gobcan.coetl.errors.ErrorConstants;
import es.gobcan.coetl.service.ParameterService;
import es.gobcan.coetl.web.rest.dto.ParameterDTO;
import es.gobcan.coetl.web.rest.mapper.ParameterMapper;
import es.gobcan.coetl.web.rest.util.HeaderUtil;
import es.gobcan.coetl.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(GlobalParameterResource.BASE_URI)
public class GlobalParameterResource extends AbstractResource {

    public static final String BASE_URI = "/api/global-parameters";
    private static final String ENTITY_NAME = "parameter";
    private static final Logger LOG = LoggerFactory.getLogger(GlobalParameterResource.class);

    private final ParameterService parameterService;
    private final ParameterMapper parameterMapper;
    private final AuditEventPublisher auditEventPublisher;

    public GlobalParameterResource(ParameterService parameterService, ParameterMapper parameterMapper, AuditEventPublisher auditEventPublisher) {
        this.parameterService = parameterService;
        this.parameterMapper = parameterMapper;
        this.auditEventPublisher = auditEventPublisher;
    }

    @PostMapping()
    @Timed
    @PreAuthorize("@secChecker.isManageGlobalParameters(authentication)")
    public ResponseEntity<ParameterDTO> createGlobalParameter(@RequestBody ParameterDTO parameterDTO) throws URISyntaxException {
        LOG.debug("REST Request to create a Global Parameter: {} ", parameterDTO);

        if (parameterDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, ErrorConstants.ID_EXISTE, "A new parameter must not have an ID")).build();
        }

        Parameter currentParameter = parameterMapper.toEntity(parameterDTO);
        if (currentParameter == null) {
            return ResponseEntity.notFound().build();
        }

        Parameter createdParameter = parameterService.create(currentParameter);
        ParameterDTO result = parameterMapper.toDto(createdParameter);

        auditEventPublisher.publish(AuditConstants.GLOBAL_PARAMETER_CREATED, createdParameter.getId().toString());

        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, result.getId().toString())).body(result);
    }

    @PutMapping()
    @Timed
    @PreAuthorize("@secChecker.isManageGlobalParameters(authentication)")
    public ResponseEntity<ParameterDTO> updateGlobalParameter(@RequestBody ParameterDTO parameterDTO) {
        LOG.debug("REST Request to update a Global Parameter: {}", parameterDTO);
        if (parameterDTO.getId() == null) {
            return ResponseEntity.notFound().build();
        }

        Parameter currentParameter = parameterMapper.toEntity(parameterDTO);
        if (currentParameter == null) {
            return ResponseEntity.notFound().build();
        }

        Parameter updatedParameter = parameterService.update(currentParameter);
        ParameterDTO result = parameterMapper.toDto(updatedParameter);
        auditEventPublisher.publish(AuditConstants.GLOBAL_PARAMETER_UPDATED, updatedParameter.getId().toString());

        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, result.getId().toString())).body(result);
    }

    @GetMapping()
    @Timed
    @PreAuthorize("@secChecker.canReadFile(authentication)")
    public ResponseEntity<List<ParameterDTO>> findAllGlobalParameters(@ApiParam Pageable pageable) {
        LOG.debug("REST Request to find all Global Parameter ");

        Page<ParameterDTO> page = parameterService.findAll(pageable).map(parameterMapper::toDto);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "api/global-parameters");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @DeleteMapping("/{parameterId}")
    @Timed
    @PreAuthorize("@secChecker.isManageGlobalParameters(authentication)")
    public ResponseEntity<Void> deleteGlobalParameter(@PathVariable Long parameterId) {
        LOG.debug("REST Request to delete a Parameter: {} with ETL : {}", parameterId);

        Parameter currentParameter = parameterService.findOneById(parameterId);
        if (currentParameter == null) {
            return ResponseEntity.notFound().build();
        }

        parameterService.delete(currentParameter);
        auditEventPublisher.publish(AuditConstants.GLOBAL_PARAMETER_DELETED, parameterId.toString());

        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, parameterId.toString())).build();
    }


    @GetMapping("/{parameterId}/decode")
    @Timed
    @PreAuthorize("@secChecker.isManageGlobalParameters(authentication)")
    public ResponseEntity<ParameterDTO> decodeGlobalParameter(@PathVariable Long parameterId) {
        LOG.debug("REST Request to decode value of Parameter: {} ", parameterId);

        Parameter parameter = parameterService.findOneById(parameterId);
        ParameterDTO result = parameterMapper.toDto(parameter);
        result.setValue(parameterService.decodeValueByTypology(parameter));

        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(result));
    }
}
