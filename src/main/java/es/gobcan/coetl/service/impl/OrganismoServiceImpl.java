package es.gobcan.coetl.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Organismo;
import es.gobcan.coetl.domain.UsuarioRolOrganismo;
import es.gobcan.coetl.domain.enumeration.Rol;
import es.gobcan.coetl.errors.CustomParameterizedExceptionBuilder;
import es.gobcan.coetl.errors.ErrorConstants;
import es.gobcan.coetl.repository.EtlRepository;
import es.gobcan.coetl.repository.OrganismoRepository;
import es.gobcan.coetl.repository.UsuarioRolOrganismoRepository;
import es.gobcan.coetl.security.SecurityChecker;
import es.gobcan.coetl.service.OrganismoService;
import es.gobcan.coetl.service.validator.OrganismValidator;


@Service
public class OrganismoServiceImpl implements OrganismoService {

    private static final String DUPLICATE_ERROR_MESSAGE = "Organism \"%s\" can not be duplicated";
    private static final String DELETE_ERROR_MESSAGE_ETL_HAS_ORGANISMO = "Organism \"%s\" can not be delete because exists a or more etl created.";
    private static final String DELETE_ERROR_MESSAGE_USER_HAS_ORGANISMO = "Organism \"%s\" can not be delete because exists a or more etl created.";

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganismoService.class);

    @Autowired
    private OrganismoRepository organismoRepository;

    @Autowired
    private OrganismValidator organismoValidator;

    @Autowired
    EntityManager entityManager;

    @Autowired
    private EtlRepository etlRepository;

    @Autowired
    private UsuarioRolOrganismoRepository usuarioRolOrganismoRespository;
    
    @Autowired 
    private SecurityChecker secCheck;

    @Override
    public List<Organismo> findAll() {
        return organismoRepository.findAllByOrderByNameAsc();
    }

    @Override
    public List<Organismo> findByIdUsuario(Long idUsuario) {
        if (secCheck.canSeeAllEtls(SecurityContextHolder.getContext().getAuthentication())) {
            return findAll();
        }

        Sort sort = new Sort(Sort.Direction.ASC, "organismo.name");
        List<UsuarioRolOrganismo> organismosUsuario = usuarioRolOrganismoRespository.findByIdUsuario(idUsuario, sort);
        return organismosUsuario.stream().map(or -> or.getOrganismo()).collect(Collectors.toList());
    }

    @Override
    public List<Organismo> findByIdUsuarioManage(Long idUsuario) {
        if (secCheck.canSeeAllEtls(SecurityContextHolder.getContext().getAuthentication())) {
            return findAll();
        }

        Sort sort = new Sort(Sort.Direction.ASC, "organismo.name");
        List<UsuarioRolOrganismo> organismosUsuario = usuarioRolOrganismoRespository.findByIdUsuario(idUsuario, sort);
        return organismosUsuario.stream().filter(obj -> Arrays.asList(Rol.TECNICO.name()).stream().anyMatch(rol -> rol.equals(obj.getRol().getName())))
                .map(org -> org.getOrganismo()).collect(Collectors.toList());
    }

    @Override
    public Organismo create(Organismo organismo) {
        LOGGER.debug("Request to create an Organism : {}", organismo);
        return save(organismo);
    }

    @Override
    public Organismo update(Organismo organismo) {
        LOGGER.debug("Request to update a Parameter : {}", organismo);
        return save(organismo);
    }

    private Organismo save(Organismo organismo) {
        LOGGER.debug("Request to save an Organism : {}", organismo);
        return organismoRepository.saveAndFlush(organismo);
    }

    @Override
    public void delete(Organismo organismo) {
        LOGGER.debug("Request to delete an Organism : {}", organismo);
        organismoRepository.delete(organismo);
    }

    @Override
    public Organismo findOneByOrganizationInCharge(Long id) {
        LOGGER.debug("Request to get a Organism : {}", id);
        return organismoRepository.findOneById(id);
    }

    @Override
    public void validaciones(Organismo organismoForm, Organismo organismoExistente) {
        checkIsExistOrganismo(organismoExistente);
        organismoValidator.validate(organismoForm);
    }

    private void checkIsExistOrganismo(Organismo organismoExistente) {
        if (organismoExistente != null) {
            throw new CustomParameterizedExceptionBuilder().message(String.format(DUPLICATE_ERROR_MESSAGE, organismoExistente.getName()))
                    .code(ErrorConstants.ORGANISM_KEY_IS_DUPLICATED).build();
        }
    }

    @Override
    public void validationDelete(Long idOrganismo) {
        checkIfExistsOrganismoInEtl(idOrganismo);
        checkIfAnyUserHasOrganismo(idOrganismo);
    }

    private void checkIfExistsOrganismoInEtl(Long idOrganismo) {
        List<Etl> existe = etlRepository.findByOrganizationInChargeId(idOrganismo);

        if (!existe.isEmpty()) {
            throw new CustomParameterizedExceptionBuilder().message(String.format(DELETE_ERROR_MESSAGE_ETL_HAS_ORGANISMO, idOrganismo))
                    .code(ErrorConstants.ORGANISM_KEY_NOT_DELETED).build();
        }
    }

    private void checkIfAnyUserHasOrganismo(Long idOrganismo) {
        List<UsuarioRolOrganismo> usuarios = usuarioRolOrganismoRespository.findByIdOrganismo(idOrganismo);

        if (!usuarios.isEmpty()) {
            throw new CustomParameterizedExceptionBuilder().message(String.format(DELETE_ERROR_MESSAGE_USER_HAS_ORGANISMO, idOrganismo))
                    .code(ErrorConstants.ORGANISM_KEY_NOT_DELETED_BY_USER).build();
        }
    }

}
