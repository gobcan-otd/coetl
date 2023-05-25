package es.gobcan.coetl.web.rest;

import java.net.URISyntaxException;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

import es.gobcan.coetl.config.AuditConstants;
import es.gobcan.coetl.config.audit.AuditEventPublisher;
import es.gobcan.coetl.domain.Organismo;
import es.gobcan.coetl.errors.ErrorConstants;
import es.gobcan.coetl.repository.OrganismoRepository;
import es.gobcan.coetl.service.OrganismoService;
import es.gobcan.coetl.web.rest.dto.OrganismoDTO;
import es.gobcan.coetl.web.rest.mapper.OrganismoMapper;
import es.gobcan.coetl.web.rest.util.HeaderUtil;


@RestController
@RequestMapping(OrganismoResource.BASE_URI)
public class OrganismoResource extends AbstractResource {

    private final Logger LOG = LoggerFactory.getLogger(OrganismoResource.class);

    public static final String BASE_URI = "/api/organism";
    public static final String ORGANISM_ENTITY_NAME = "organism";

    private final OrganismoService organismoService;
    private final OrganismoMapper organismoMapper;
    private final AuditEventPublisher auditEventPublisher;
    private final OrganismoRepository organismoRepository;

    public OrganismoResource(OrganismoService organismoService, OrganismoMapper organismoMapper, AuditEventPublisher auditEventPublisher, 
            OrganismoRepository organismoRepository) {
        this.organismoService = organismoService;
        this.organismoMapper = organismoMapper;
        this.auditEventPublisher = auditEventPublisher;
        this.organismoRepository = organismoRepository;
    }

    @GetMapping()
    @Timed
    @PreAuthorize("@secChecker.canReadEtl(authentication)")
    public ResponseEntity<List<OrganismoDTO>> findAll() {
        LOG.debug("REST Request to find all Organism");
        List<Organismo> healths = organismoService.findAll();
        List<OrganismoDTO> result = organismoMapper.toDto(healths);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{idUsuario}")
    @Timed
    @PreAuthorize("@secChecker.canReadEtl(authentication)")
    public ResponseEntity<List<OrganismoDTO>> findByIdUsuario(@PathVariable Long idUsuario) {
        LOG.debug("REST Request to find all Organism");
        List<Organismo> healths = organismoService.findByIdUsuario(idUsuario);
        List<OrganismoDTO> result = organismoMapper.toDto(healths);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{idUsuario}/manage")
    @Timed
    @PreAuthorize("@secChecker.canCreateEtl(authentication)")
    public ResponseEntity<List<OrganismoDTO>> findByIdUsuarioRolOrganismoManageEtl(@PathVariable Long idUsuario) {
        LOG.debug("REST Request to find all Organism");
        List<Organismo> healths = organismoService.findByIdUsuarioManage(idUsuario);
        List<OrganismoDTO> result = organismoMapper.toDto(healths);

        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Timed
    @PreAuthorize("@secChecker.canManageOrganismo(authentication)")
    public ResponseEntity<OrganismoDTO> create(@Valid @RequestBody OrganismoDTO organismDTO) throws URISyntaxException {
        LOG.debug("REST Request to create an Organism : {}", organismDTO);
        Organismo organismoRepetido = organismoRepository.findByName(organismDTO.getName());
        organismoService.validaciones(organismoMapper.toEntity(organismDTO), organismoRepetido);
        if (organismDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ORGANISM_ENTITY_NAME, ErrorConstants.ID_EXISTE, "A new ORGANISM must not have an ID")).build();
        }
        Organismo newOrganism = this.organismoService.create(organismoMapper.toEntity(organismDTO));
        if (newOrganism == null) {
            return ResponseEntity.notFound().build();
        }

        OrganismoDTO result = organismoMapper.toDto(newOrganism);
        auditEventPublisher.publish(AuditConstants.GLOBAL_ORGANISMO_CREATED, newOrganism.getId().toString());

        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ORGANISM_ENTITY_NAME, result.getId().toString())).body(result);
    }

    @PutMapping()
    @Timed
    @PreAuthorize("@secChecker.canManageOrganismo(authentication)")
    public ResponseEntity<OrganismoDTO> update(@RequestBody OrganismoDTO organismDTO) {
        LOG.debug("REST Request to update a Organism: {}", organismDTO);
        Organismo organismoRepetido = organismoRepository.findByNameAndIdNot(organismDTO.getName(), organismDTO.getId());
        organismoService.validaciones(organismoMapper.toEntity(organismDTO), organismoRepetido);
        if (organismDTO.getId() == null) {
            return ResponseEntity.notFound().build();
        }

        Organismo currentOrganismo = organismoMapper.toEntity(organismDTO);
        if (currentOrganismo == null) {
            return ResponseEntity.notFound().build();
        }

        Organismo updatedOrganismo = organismoService.update(currentOrganismo);

        OrganismoDTO result = organismoMapper.toDto(updatedOrganismo);
        auditEventPublisher.publish(AuditConstants.GLOBAL_ORGANISMO_UPDATED, updatedOrganismo.getId().toString());

        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ORGANISM_ENTITY_NAME, result.getId().toString())).body(result);
    }

    @DeleteMapping("/{organismoId}")
    @Timed
    @PreAuthorize("@secChecker.canManageOrganismo(authentication)")
    public ResponseEntity<Void> delete(@PathVariable Long organismoId) {
        LOG.debug("REST Request to delete a Organism: {} ", organismoId);
        organismoService.validationDelete(organismoId);
        Organismo currentOrganismo = organismoService.findOneByOrganizationInCharge(organismoId);

        if (currentOrganismo == null) {
            return ResponseEntity.notFound().build();
        }

        organismoService.delete(currentOrganismo);
        auditEventPublisher.publish(AuditConstants.GLOBAL_ORGANISMO_DELETED, organismoId.toString());

        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ORGANISM_ENTITY_NAME, organismoId.toString())).build();
    }

}
