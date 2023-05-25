package es.gobcan.coetl.web.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

import es.gobcan.coetl.domain.Roles;
import es.gobcan.coetl.service.RolesService;
import es.gobcan.coetl.web.rest.dto.RolesDTO;
import es.gobcan.coetl.web.rest.mapper.RolesMapper;

@RestController
@RequestMapping(RolesResource.BASE_URI)
public class RolesResource extends AbstractResource {
    
    private final Logger LOG = LoggerFactory.getLogger(OrganismoResource.class);

    public static final String BASE_URI = "/api/roles";
    public static final String ORGANISM_ENTITY_NAME = "roles";
    
    private final RolesService rolesService;
    private final RolesMapper rolesMapper;

    public RolesResource(RolesService rolesService, RolesMapper rolesMapper) {
        this.rolesService = rolesService;
        this.rolesMapper = rolesMapper;
    }

    @GetMapping()
    @Timed
    @PreAuthorize("@secChecker.canReadEtl(authentication)")
    public ResponseEntity<List<RolesDTO>> findAll() {
        LOG.debug("REST Request to find all Roles");
        List<Roles> healths = rolesService.findAll();
        List<RolesDTO> result = rolesMapper.toDto(healths);

        return ResponseEntity.ok(result);
    }

}
