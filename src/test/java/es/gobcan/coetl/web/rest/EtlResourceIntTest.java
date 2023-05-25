package es.gobcan.coetl.web.rest;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import es.gobcan.coetl.CoetlApp;
import es.gobcan.coetl.config.AESProperties;
import es.gobcan.coetl.config.audit.AuditEventPublisher;
import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Etl.Type;
import es.gobcan.coetl.domain.Organismo;
import es.gobcan.coetl.domain.Parameter;
import es.gobcan.coetl.domain.Roles;
import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.domain.UsuarioRolOrganismo;
import es.gobcan.coetl.domain.enumeration.Rol;
import es.gobcan.coetl.errors.ExceptionTranslator;
import es.gobcan.coetl.pentaho.service.PentahoGitService;
import es.gobcan.coetl.repository.EtlRepository;
import es.gobcan.coetl.repository.OrganismoRepository;
import es.gobcan.coetl.repository.ParameterRepository;
import es.gobcan.coetl.repository.RolesRepository;
import es.gobcan.coetl.repository.UsuarioRepository;
import es.gobcan.coetl.repository.UsuarioRolOrganismoRepository;
import es.gobcan.coetl.security.SecurityUtils;
import es.gobcan.coetl.service.EtlService;
import es.gobcan.coetl.service.ExecutionService;
import es.gobcan.coetl.service.ParameterService;
import es.gobcan.coetl.web.rest.dto.EtlBaseDTO;
import es.gobcan.coetl.web.rest.dto.EtlDTO;
import es.gobcan.coetl.web.rest.dto.ParameterDTO;
import es.gobcan.coetl.web.rest.mapper.EtlMapper;
import es.gobcan.coetl.web.rest.mapper.ExecutionMapper;
import es.gobcan.coetl.web.rest.mapper.ParameterMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoetlApp.class)
public class EtlResourceIntTest {

    private static final String BASE_URI = EtlResource.BASE_URI;

    private static final String DEFAULT_CODE = "DEFAULT_CODE";
    private static final String UPDATED_CODE = "UPDATED_CODE";
    private static final String DEFAULT_NAME = "DEFAULT_NAME";
    private static final String UPDATED_NAME = "UPDATED_NAME";
    private static final String DEFAULT_FUNCTIONAL_IN_CHARGE = "DEFAULT_FUNCTIONAL_IN_CHARGE";
    private static final String UPDATED_FUNCTIONAL_IN_CHARGE = "UPDATED_FUNCTIONAL_IN_CHARGE";
    private static final String DEFAULT_TECHNICAL_IN_CHARGE = "DEFAULT_TECHNICAL_IN_CHARGE";
    private static final String UPDATED_TECHNICAL_IN_CHARGE = "UPDATED_TECHNICAL_IN_CHARGE";
    private static final Type DEFAULT_TYPE = Type.TRANSFORMATION;
    private static final Type UPDATED_TYPE = Type.JOB;


    private static final String DEFAULT_ETL_PARAMETER_KEY = "DEFAULT_ETL_PARAMETER_KEY";
    private static final String DEFAULT_ETL_PARAMETER_VALUE = "DEFAULT_ETL_PARAMETER_VALUE";
    private static final String UPDATED_ETL_PARAMETER_VALUE = "UPDATED_ETL_PARAMETER_VALUE";
    private static final String UPDATED_ETL_PARAMETER_DESCRIPTION = "UPDATED_ETL_PARAMETER_DESCIPTION";
    private static final String DEFAULT_REPOSITORY_VALUE = "https://testing.com/default.git";
    private static final Parameter.Type DEFAULT_ETL_PARAMETER_TYPE = Parameter.Type.MANUAL;
    private static final Parameter.Typology DEFAULT_ETL_PARAMETER_TYPOLOGY = Parameter.Typology.GENERIC;


    @Autowired
    EntityManager entityManager;

    @Autowired
    EtlRepository etlRepository;

    @SpyBean
    EtlService etlService;

    @SpyBean
    EtlMapper etlMapper;

    @SpyBean
    ExecutionService executionService;

    @SpyBean
    ExecutionMapper executionMapper;

    @Mock
    PentahoGitService pentahoGitService;

    @Autowired
    ParameterRepository parameterRepository;

    @Autowired
    ParameterService parameterServie;

    @Autowired
    ParameterMapper parameterMapper;

    @Autowired
    AuditEventPublisher auditEventPublisher;

    @Autowired
    AESProperties aesProperties;
    
    @Autowired
    OrganismoRepository organismoRepository;
    
    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    UsuarioRolOrganismoRepository usuarioRolOrganismoRepository;

    private MockMvc restEtlMockMvc;

    private Organismo newOrganismo;
    private Organismo etlOrganism;
    private Usuario newUser;
    private Roles newRol;
    private UsuarioRolOrganismo rol1;
    List<UsuarioRolOrganismo> roles;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(pentahoGitService.cloneRepository(any(Etl.class))).thenReturn("/path/to/mocking/repository");
        Mockito.when(pentahoGitService.replaceRepository(any(Etl.class))).thenReturn("/path/to/mocking/repository");
        EtlResource etlResource = new EtlResource(etlService, etlMapper, executionService, executionMapper, parameterServie,
            parameterMapper, pentahoGitService, auditEventPublisher);

        this.restEtlMockMvc = MockMvcBuilders.standaloneSetup(etlResource).setCustomArgumentResolvers(pageableArgumentResolver).setControllerAdvice(exceptionTranslator)
                .setMessageConverters(jacksonMessageConverter).build();

        newOrganismo = new Organismo();
        newOrganismo.setDescription("Description1");
        newOrganismo.setName("Name1");
        organismoRepository.saveAndFlush(newOrganismo);

        etlOrganism = new Organismo();
        etlOrganism.setDescription("OtherDescription");
        etlOrganism.setName("otherName");
        organismoRepository.saveAndFlush(etlOrganism);

        createUser();
        setRolOrganismo();
        setPermisos();
    }

    private Etl mockEntityWithoutId() throws IOException, SQLException {
        Etl etl = new Etl();
        etl.setCode(DEFAULT_CODE);
        etl.setName(DEFAULT_NAME);
        etl.setFunctionalInCharge(DEFAULT_FUNCTIONAL_IN_CHARGE);
        etl.setTechnicalInCharge(DEFAULT_TECHNICAL_IN_CHARGE);
        etl.setType(DEFAULT_TYPE);
        etl.setUriRepository(DEFAULT_REPOSITORY_VALUE);
        etl.setVisibility(false);
        return etl;
    }

    private Etl mockEntity() throws IOException, SQLException {
        Etl etl = mockEntityWithoutId();
        etl.setId(1L);
        etl.setVisibility(false);
        return etl;
    }

    private Etl mockDeletedEntity() throws IOException, SQLException {
        Etl etl = mockEntity();
        etl.setDeletionDate(Instant.now());
        etl.setDeletedBy("TEST_USER");
        etl.setVisibility(false);
        return etl;
    }

    private Parameter mockParameterEntityWithoutId(Etl etl) {
        Parameter parameter = new Parameter();
        parameter.setEtl(etl);
        parameter.setKey(DEFAULT_ETL_PARAMETER_KEY);
        parameter.setValue(DEFAULT_ETL_PARAMETER_VALUE);
        parameter.setType(DEFAULT_ETL_PARAMETER_TYPE);
        parameter.setTypology(DEFAULT_ETL_PARAMETER_TYPOLOGY);
        parameter.setDescription(UPDATED_ETL_PARAMETER_DESCRIPTION);
        return parameter;
    }

    private Parameter mockParameterEntity(Etl etl) {
        Parameter parameter = mockParameterEntityWithoutId(etl);
        parameter.setId(1L);
        return parameter;
    }
    
    private void createUser() {
        newUser = new Usuario();
        newUser.setLogin("admin");
        newUser.setNombre("john");
        newUser.setApellido1("doe");
        newUser.setEmail("john.doe@jhipster.com");
        usuarioRepository.saveAndFlush(newUser);
    }
    
    private void setRolOrganismo() {

        newRol = new Roles();
        newRol.setName(Rol.TECNICO.name());
        rolesRepository.saveAndFlush(newRol);

        rol1 = new UsuarioRolOrganismo();
        rol1.setOrganismo(newOrganismo);
        rol1.setRol(newRol);
        rol1.setIdUsuario(newUser.getId());
        rol1.setIdRol(newRol.getId());
        rol1.setIdOrganismo(newOrganismo.getId());
        usuarioRolOrganismoRepository.saveAndFlush(rol1);
    }

    private void setPermisos() {
        newUser.setUsuarioRolOrganismo(getPermisos());
    }

    private List<UsuarioRolOrganismo> getPermisos() {
        roles = new ArrayList<>();
        roles.add(rol1);
        return roles;
    }

    @Test
    @Transactional
    public void createEtl_isStatusOk() throws IOException, SQLException, Exception {
        EtlDTO createdEtlDTOMocked = etlMapper.toDto(mockEntityWithoutId());
        createdEtlDTOMocked.setOrganizationInCharge(newOrganismo);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin"));
        SecurityContextHolder.setContext(securityContext);

        //@formatter:off
        restEtlMockMvc.perform(post(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(createdEtlDTOMocked)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.code").value(createdEtlDTOMocked.getCode()))
            .andExpect(jsonPath("$.name").value(createdEtlDTOMocked.getName()))
            .andExpect(jsonPath("$.purpose").value(is(nullValue())))
            .andExpect(jsonPath("$.organizationInCharge.id").value(createdEtlDTOMocked.getOrganizationInCharge().getId()))
            .andExpect(jsonPath("$.organizationInCharge.name").value(createdEtlDTOMocked.getOrganizationInCharge().getName()))
            .andExpect(jsonPath("$.organizationInCharge.description").value(createdEtlDTOMocked.getOrganizationInCharge().getDescription()))
            .andExpect(jsonPath("$.functionalInCharge").value(createdEtlDTOMocked.getFunctionalInCharge()))
            .andExpect(jsonPath("$.technicalInCharge").value(createdEtlDTOMocked.getTechnicalInCharge()))
            .andExpect(jsonPath("$.type").value(createdEtlDTOMocked.getType().name()))
            .andExpect(jsonPath("$.comments").value(is(nullValue())))
            .andExpect(jsonPath("$.executionDescription").value(is(nullValue())))
            .andExpect(jsonPath("$.executionPlanning").value(is(nullValue())))
            .andExpect(jsonPath("$.uriRepository").isNotEmpty())
            .andExpect(jsonPath("$.deletionDate").value(is(nullValue())))
            .andExpect(jsonPath("$.deletedBy").value(is(nullValue())));
        //@formatter:on
    }

    @Test
    @Transactional
    public void createEtl_isStatusBadRequest_ifExistingId() throws IOException, SQLException, Exception {
        Etl createdEtlMocked = mockEntity();
        createdEtlMocked.setOrganismo(newOrganismo.getId());
        EtlDTO createdEtlDTOMocked = etlMapper.toDto(createdEtlMocked);

        //@formatter:off
        restEtlMockMvc.perform(post(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(createdEtlDTOMocked)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(header().string("X-coetl-error", "error.id-existe"))
            .andExpect(header().string("X-coetl-params", "etl"));
        //@formatter:on
    }

    @SuppressWarnings("unchecked")
    @Test
    @Transactional
    public void updateEtl_isStatusOk() throws IOException, SQLException, Exception {
        Etl updatedEtlMocked = mockEntity();
        updatedEtlMocked.setCode(UPDATED_CODE);
        updatedEtlMocked.setName(UPDATED_NAME);
        updatedEtlMocked.setFunctionalInCharge(UPDATED_FUNCTIONAL_IN_CHARGE);
        updatedEtlMocked.setTechnicalInCharge(UPDATED_TECHNICAL_IN_CHARGE);
        updatedEtlMocked.setType(UPDATED_TYPE);
        updatedEtlMocked.setOrganismo(etlOrganism.getId());
        updatedEtlMocked.setOrganizationInCharge(etlOrganism);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin"));
        SecurityContextHolder.setContext(securityContext);

        Collection<SimpleGrantedAuthority> oldAuthorities = (Collection<SimpleGrantedAuthority>)SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ADMIN");
        List<SimpleGrantedAuthority> updatedAuthorities = new ArrayList<>();
        updatedAuthorities.add(authority);
        updatedAuthorities.addAll(oldAuthorities);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                        SecurityContextHolder.getContext().getAuthentication().getCredentials(),
                        updatedAuthorities)
        );

        etlRepository.saveAndFlush(updatedEtlMocked);

        EtlDTO updatedEtlDTOMocked = etlMapper.toDto(updatedEtlMocked);
        updatedEtlDTOMocked.setOrganizationInCharge(etlOrganism);

        doReturn(updatedEtlMocked).when(etlMapper).toEntity(updatedEtlDTOMocked);

        doReturn(updatedEtlMocked).when(etlService).update(any(Etl.class));

        doReturn(false).when(etlService).goingToChangeRepository(any(EtlDTO.class));

        //@formatter:off
        restEtlMockMvc.perform(put(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedEtlDTOMocked)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id").value(updatedEtlDTOMocked.getId()))
            .andExpect(jsonPath("$.code").value(updatedEtlDTOMocked.getCode()))
            .andExpect(jsonPath("$.name").value(updatedEtlDTOMocked.getName()))
            .andExpect(jsonPath("$.purpose").value(is(nullValue())))
            .andExpect(jsonPath("$.organizationInCharge.id").value(updatedEtlDTOMocked.getOrganizationInCharge().getId()))
            .andExpect(jsonPath("$.organizationInCharge.name").value(updatedEtlDTOMocked.getOrganizationInCharge().getName()))
            .andExpect(jsonPath("$.organizationInCharge.description").value(updatedEtlDTOMocked.getOrganizationInCharge().getDescription()))
            .andExpect(jsonPath("$.functionalInCharge").value(updatedEtlDTOMocked.getFunctionalInCharge()))
            .andExpect(jsonPath("$.technicalInCharge").value(updatedEtlDTOMocked.getTechnicalInCharge()))
            .andExpect(jsonPath("$.type").value(updatedEtlDTOMocked.getType().name()))
            .andExpect(jsonPath("$.comments").value(is(nullValue())))
            .andExpect(jsonPath("$.executionDescription").value(is(nullValue())))
            .andExpect(jsonPath("$.executionPlanning").value(is(nullValue())))
            .andExpect(jsonPath("$.deletionDate").value(is(nullValue())))
            .andExpect(jsonPath("$.deletedBy").value(is(nullValue())));
        //@formatter:on
    }

    @Test
    @Transactional
    public void updateEtl_isStatusBadRequest_ifNotExistingId() throws IOException, SQLException, Exception {
        Etl updatedEtlMocked = mockEntityWithoutId();
        updatedEtlMocked.setCode(UPDATED_CODE);
        updatedEtlMocked.setName(UPDATED_NAME);
        updatedEtlMocked.setFunctionalInCharge(UPDATED_FUNCTIONAL_IN_CHARGE);
        updatedEtlMocked.setTechnicalInCharge(UPDATED_TECHNICAL_IN_CHARGE);
        updatedEtlMocked.setType(UPDATED_TYPE);
        updatedEtlMocked.setOrganismo(etlOrganism.getId());
        updatedEtlMocked.setOrganizationInCharge(etlOrganism);

        EtlDTO updatedEtlDTOMocked = etlMapper.toDto(updatedEtlMocked);
        updatedEtlDTOMocked.setOrganizationInCharge(etlOrganism);

        //@formatter:off
        restEtlMockMvc.perform(put(BASE_URI.concat("?isAttachedFileChanged=\"false\""))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedEtlDTOMocked)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(header().string("X-coetl-error", "error.id-falta"))
            .andExpect(header().string("X-coetl-params", "etl"));
        //@formatter:on
    }

    @Test
    @Transactional
    public void deleteEtl_isStatusOk() throws IOException, SQLException, Exception {
        Etl etlToDeleteMocked = mockEntity();
        etlToDeleteMocked.setOrganismo(newOrganismo.getId());
        etlToDeleteMocked.setOrganizationInCharge(newOrganismo);
        EtlDTO etlToDeleteDTOMocked = etlMapper.toDto(etlToDeleteMocked);
        etlToDeleteDTOMocked.setOrganizationInCharge(newOrganismo);

        doReturn(etlToDeleteMocked).when(etlService).findOne(etlToDeleteDTOMocked.getId());

        Etl deletedEtlMocked = mockEntity();
        deletedEtlMocked.setDeletionDate(Instant.now());
        deletedEtlMocked.setDeletedBy("test");

        doReturn(deletedEtlMocked).when(etlService).delete(any(Etl.class));

        //@formatter:off
        restEtlMockMvc.perform(delete(BASE_URI + "/{idEtl}", etlToDeleteDTOMocked.getId())
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.deletionDate").value(TestUtil.sameInstant(deletedEtlMocked.getDeletionDate())))
            .andExpect(jsonPath("$.deletedBy").value(deletedEtlMocked.getDeletedBy()));
        //@formatter:on
    }

    @Test
    @Transactional
    public void deleteEtl_isStatusBadRequest_ifEtlIsAlreadyDeleted() throws IOException, SQLException, Exception {
        Etl currentlyDeletedEtlMocked = mockEntity();
        currentlyDeletedEtlMocked.setDeletionDate(Instant.now());
        currentlyDeletedEtlMocked.setDeletedBy("test");
        currentlyDeletedEtlMocked.setOrganismo(newOrganismo.getId());
        currentlyDeletedEtlMocked.setOrganizationInCharge(newOrganismo);
        EtlDTO deletedEtlDTOMocked = etlMapper.toDto(currentlyDeletedEtlMocked);
        deletedEtlDTOMocked.setOrganizationInCharge(newOrganismo);

        doReturn(currentlyDeletedEtlMocked).when(etlService).findOne(deletedEtlDTOMocked.getId());

        //@formatter:off
        restEtlMockMvc.perform(MockMvcRequestBuilders.delete(BASE_URI + "/{idEtl}", deletedEtlDTOMocked.getId())
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("error.etl.currentlyDeleted"));
        //@formatter:on
    }

    @Test
    @Transactional
    public void findOneEtl_isStatusOk() throws IOException, SQLException, Exception {
        Etl etlMocked = mockEntity();
        etlMocked.setOrganismo(newOrganismo.getId());
        etlMocked.setOrganizationInCharge(newOrganismo);

        EtlDTO etlDTOMocked = etlMapper.toDto(etlMocked);
        etlDTOMocked.setOrganizationInCharge(newOrganismo);

        doReturn(etlMocked).when(etlService).findOne(etlDTOMocked.getId());

        //@formatter:off
        restEtlMockMvc.perform(get(BASE_URI + "/{idEtl}", etlDTOMocked.getId())
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id").value(etlDTOMocked.getId()))
            .andExpect(jsonPath("$.code").value(etlDTOMocked.getCode()))
            .andExpect(jsonPath("$.name").value(etlDTOMocked.getName()))
            .andExpect(jsonPath("$.purpose").value(is(nullValue())))
            .andExpect(jsonPath("$.organizationInCharge.id").value(etlDTOMocked.getOrganizationInCharge().getId()))
            .andExpect(jsonPath("$.organizationInCharge.name").value(etlDTOMocked.getOrganizationInCharge().getName()))
            .andExpect(jsonPath("$.organizationInCharge.description").value(etlDTOMocked.getOrganizationInCharge().getDescription()))
            .andExpect(jsonPath("$.functionalInCharge").value(etlDTOMocked.getFunctionalInCharge()))
            .andExpect(jsonPath("$.technicalInCharge").value(etlDTOMocked.getTechnicalInCharge()))
            .andExpect(jsonPath("$.type").value(etlDTOMocked.getType().name()))
            .andExpect(jsonPath("$.comments").value(is(nullValue())))
            .andExpect(jsonPath("$.executionDescription").value(is(nullValue())))
            .andExpect(jsonPath("$.executionPlanning").value(is(nullValue())))
            .andExpect(jsonPath("$.deletionDate").value(is(nullValue())))
            .andExpect(jsonPath("$.deletedBy").value(is(nullValue())));
        //@formatter:on
    }

    @Test
    @Transactional
    public void findAllEtl_isStatusOk() throws IOException, SQLException, Exception {
        Etl etlMocked = mockEntity();
        etlMocked.setOrganismo(etlOrganism.getId());
        etlMocked.setOrganizationInCharge(etlOrganism);

        EtlBaseDTO etlDTOMocked = etlMapper.toBaseDto(etlMocked, null, null);
        etlDTOMocked.setOrganizationInCharge(etlOrganism);

        Page<Etl> etlMockPage = new PageImpl<>(new ArrayList<>(Arrays.asList(etlMocked)));
        doReturn(etlMockPage).when(etlService).findAll(any(String.class), any(Boolean.class), any(Pageable.class), Matchers.anyListOf(Long.class), any(String.class), any(String.class));

        //@formatter:off
        restEtlMockMvc.perform(get(BASE_URI + "?sort=id,asc").param("organismos", "1").param("lastExecution", "").param("lastExecutionByResult", "")
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.[*].id").value(hasItem(etlDTOMocked.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(etlDTOMocked.getCode())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(etlDTOMocked.getName())))
            .andExpect(jsonPath("$.[*].organizationInCharge.id").value(hasItem(etlDTOMocked.getOrganizationInCharge()
                    .getId().intValue())))
            .andExpect(jsonPath("$.[*].organizationInCharge.name").value(hasItem(etlDTOMocked.getOrganizationInCharge().getName())))
            .andExpect(jsonPath("$.[*].organizationInCharge.description").value(hasItem(etlDTOMocked.getOrganizationInCharge().getDescription())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(etlDTOMocked.getType().name())))
            .andExpect(jsonPath("$.[*].executionPlanning").value(hasItem(is(nullValue()))))
            .andExpect(jsonPath("$.[*].deletionDate").value(hasItem(is(nullValue()))))
            .andExpect(jsonPath("$.[*].deletedBy").value(hasItem(is(nullValue()))));
        //@formatter:on
    }

    @SuppressWarnings("unchecked")
    @Test
    @Transactional
    public void createParameter_isStatusOk() throws IOException, SQLException, Exception {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin"));
        SecurityContextHolder.setContext(securityContext);

        Collection<SimpleGrantedAuthority> oldAuthorities = (Collection<SimpleGrantedAuthority>)SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ADMIN");
        List<SimpleGrantedAuthority> updatedAuthorities = new ArrayList<>();
        updatedAuthorities.add(authority);
        updatedAuthorities.addAll(oldAuthorities);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                        SecurityContextHolder.getContext().getAuthentication().getCredentials(),
                        updatedAuthorities)
        );

        Etl mockedEtl = mockEntity();
        mockedEtl.setOrganismo(etlOrganism.getId());
        mockedEtl.setOrganizationInCharge(etlOrganism);
        Etl createdEtl = etlRepository.saveAndFlush(mockedEtl);
        Parameter mockedParameter = mockParameterEntityWithoutId(createdEtl);
        ParameterDTO mockedParameterDTO = parameterMapper.toDto(mockedParameter);

        //@formatter:off
        restEtlMockMvc.perform(post(BASE_URI.concat("/{idEtl}").concat("/parameters"), createdEtl.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(mockedParameterDTO)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.key").value(DEFAULT_ETL_PARAMETER_KEY))
            .andExpect(jsonPath("$.value").value(DEFAULT_ETL_PARAMETER_VALUE))
            .andExpect(jsonPath("$.type").value(DEFAULT_ETL_PARAMETER_TYPE.name()))
            .andExpect(jsonPath("$.etlId").value(createdEtl.getId()))
            .andExpect(jsonPath("$.optLock").value(0))
            .andExpect(jsonPath("$.description").value(UPDATED_ETL_PARAMETER_DESCRIPTION));
        //@formatter:on
    }

    @Test
    @Transactional
    public void createParameter_isStatusBadRequest_ifExistingId() throws IOException, SQLException, Exception {
        Etl mockedEtl = mockEntity();
        mockedEtl.setOrganismo(etlOrganism.getId());
        Etl createdEtl = etlRepository.saveAndFlush(mockedEtl);
        Parameter mockedParameter = mockParameterEntity(createdEtl);
        ParameterDTO mockedParameterDTO = parameterMapper.toDto(mockedParameter);

        //@formatter:off
        restEtlMockMvc.perform(post(BASE_URI.concat("/{idEtl}").concat("/parameters"), createdEtl.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(mockedParameterDTO)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(header().string("X-coetl-error", "error.id-existe"))
        .andExpect(header().string("X-coetl-params", "parameter"));
        //@formatter:on
    }

    @Test
    @Transactional
    public void createParameter_isStatusNotFound_ifReferencesAnotherEtl() throws IOException, SQLException, Exception {
        Etl mockedEtl = mockEntity();
        mockedEtl.setOrganismo(newOrganismo.getId());
        Etl createdEtl = etlRepository.saveAndFlush(mockedEtl);

        Etl anotherMockedEtl = mockEntity();
        anotherMockedEtl.setCode(UPDATED_CODE);
        anotherMockedEtl.setOrganismo(newOrganismo.getId());
        Etl anotherCreatedEtl = etlRepository.saveAndFlush(anotherMockedEtl);

        Parameter mockedParameter = mockParameterEntityWithoutId(createdEtl);
        ParameterDTO mockedParameterDTO = parameterMapper.toDto(mockedParameter);

        //@formatter:off
        restEtlMockMvc.perform(post(BASE_URI.concat("/{idEtl}").concat("/parameters"), anotherCreatedEtl.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(mockedParameterDTO)))
        .andDo(print())
        .andExpect(status().isNotFound());
        //@formatter:on
    }

    @Test
    @Transactional
    public void createParameter_fail_ifEtlIsDeleted() throws IOException, SQLException, Exception {
        Etl mockedEtl = mockDeletedEntity();
        mockedEtl.setOrganismo(etlOrganism.getId());
        Etl deletedEtl = etlRepository.saveAndFlush(mockedEtl);

        Parameter mockedParameter = mockParameterEntityWithoutId(deletedEtl);
        ParameterDTO mockedParameterDTO = parameterMapper.toDto(mockedParameter);

        //@formatter:off
        restEtlMockMvc.perform(post(BASE_URI.concat("/{idEtl}").concat("/parameters"), deletedEtl.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(mockedParameterDTO)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(header().string("X-coetl-error", "error.entity.deleted"))
        .andExpect(header().string("X-coetl-params", "etl"));
        //@formatter:on
    }

    @SuppressWarnings("unchecked")
    @Test
    @Transactional
    public void updateParameter_isStatusOk() throws IOException, SQLException, Exception {
        Etl mockedEtl = mockEntity();
        mockedEtl.setOrganismo(etlOrganism.getId());

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin"));
        SecurityContextHolder.setContext(securityContext);

        Collection<SimpleGrantedAuthority> oldAuthorities = (Collection<SimpleGrantedAuthority>)SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ADMIN");
        List<SimpleGrantedAuthority> updatedAuthorities = new ArrayList<>();
        updatedAuthorities.add(authority);
        updatedAuthorities.addAll(oldAuthorities);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                        SecurityContextHolder.getContext().getAuthentication().getCredentials(),
                        updatedAuthorities)
        );

        mockedEtl.setOrganizationInCharge(newOrganismo);

        Etl createdEtl = etlRepository.saveAndFlush(mockedEtl);

        Parameter mockedParameter = mockParameterEntity(createdEtl);
        Parameter createdParameter = parameterRepository.saveAndFlush(mockedParameter);
        ParameterDTO mockedParameterDTO = parameterMapper.toDto(createdParameter);
        mockedParameterDTO.setValue(UPDATED_ETL_PARAMETER_VALUE);

        //@formatter:off
        restEtlMockMvc.perform(put(BASE_URI.concat("/{idEtl}").concat("/parameters"), createdEtl.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(mockedParameterDTO)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.key").value(DEFAULT_ETL_PARAMETER_KEY))
            .andExpect(jsonPath("$.value").value(UPDATED_ETL_PARAMETER_VALUE))
            .andExpect(jsonPath("$.type").value(DEFAULT_ETL_PARAMETER_TYPE.name()))
            .andExpect(jsonPath("$.etlId").value(createdEtl.getId()))
            .andExpect(jsonPath("$.optLock").value(1))
            .andExpect(jsonPath("$.description").value(UPDATED_ETL_PARAMETER_DESCRIPTION));
        //@formatter:on
    }

    @Test
    @Transactional
    public void updateParameter_isStatusBadRequest_ifNotExistingId() throws IOException, SQLException, Exception {
        Etl mockedEtl = mockEntity();
        mockedEtl.setOrganismo(newOrganismo.getId());
        Etl createdEtl = etlRepository.saveAndFlush(mockedEtl);

        Parameter mockedParameter = mockParameterEntityWithoutId(createdEtl);
        ParameterDTO mockedParameterDTO = parameterMapper.toDto(mockedParameter);
        mockedParameterDTO.setValue(UPDATED_ETL_PARAMETER_VALUE);

        //@formatter:off
        restEtlMockMvc.perform(put(BASE_URI.concat("/{idEtl}").concat("/parameters"), createdEtl.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(mockedParameterDTO)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(header().string("X-coetl-error", "error.id-falta"))
            .andExpect(header().string("X-coetl-params", "parameter"));
        //@formatter:on
    }

    @Test
    @Transactional
    public void updateParameter_isStatusNotFound_ifReferencesAnotherEtl() throws IOException, SQLException, Exception {
        Etl mockedEtl = mockEntity();
        mockedEtl.setOrganismo(newOrganismo.getId());
        Etl createdEtl = etlRepository.saveAndFlush(mockedEtl);

        Etl anotherMockedEtl = mockEntity();
        anotherMockedEtl.setCode(UPDATED_CODE);
        anotherMockedEtl.setOrganismo(newOrganismo.getId());
        Etl anotherCreatedEtl = etlRepository.saveAndFlush(anotherMockedEtl);

        Parameter mockedParameter = mockParameterEntity(createdEtl);
        Parameter createdParameter = parameterRepository.saveAndFlush(mockedParameter);
        ParameterDTO mockedParameterDTO = parameterMapper.toDto(createdParameter);
        mockedParameterDTO.setValue(UPDATED_ETL_PARAMETER_VALUE);

        //@formatter:off
        restEtlMockMvc.perform(put(BASE_URI.concat("/{idEtl}").concat("/parameters"), anotherCreatedEtl.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(mockedParameterDTO)))
            .andDo(print())
            .andExpect(status().isNotFound());
        //@formatter:on
    }

    @Test
    @Transactional
    public void updateParameter_isStatusBadRequest_ifEtlIsDeleted() throws IOException, SQLException, Exception {
        Etl mockedEtl = mockDeletedEntity();
        mockedEtl.setOrganismo(etlOrganism.getId());
        Etl deletedEtl = etlRepository.saveAndFlush(mockedEtl);

        Parameter mockedParameter = mockParameterEntity(deletedEtl);
        Parameter createdParameter = parameterRepository.saveAndFlush(mockedParameter);
        ParameterDTO mockedParameterDTO = parameterMapper.toDto(createdParameter);
        mockedParameterDTO.setValue(UPDATED_ETL_PARAMETER_VALUE);

        //@formatter:off
        restEtlMockMvc.perform(put(BASE_URI.concat("/{idEtl}").concat("/parameters"), deletedEtl.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(mockedParameterDTO)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(header().string("X-coetl-error", "error.entity.deleted"))
            .andExpect(header().string("X-coetl-params", "etl"));
        //@formatter:on
    }

    @SuppressWarnings("unchecked")
    @Test
    @Transactional
    public void deleteParameter_isStatusOk() throws IOException, SQLException, Exception {
        Etl mockedEtl = mockEntity();
        mockedEtl.setOrganismo(newOrganismo.getId());
        
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin"));
        SecurityContextHolder.setContext(securityContext);

        Collection<SimpleGrantedAuthority> oldAuthorities = (Collection<SimpleGrantedAuthority>)SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ADMIN");
        List<SimpleGrantedAuthority> updatedAuthorities = new ArrayList<>();
        updatedAuthorities.add(authority);
        updatedAuthorities.addAll(oldAuthorities);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
                        SecurityContextHolder.getContext().getAuthentication().getCredentials(),
                        updatedAuthorities)
        );

        mockedEtl.setOrganizationInCharge(newOrganismo);
        Etl createdEtl = etlRepository.saveAndFlush(mockedEtl);

        Parameter mockedParameter = mockParameterEntity(createdEtl);
        Parameter createdParameter = parameterRepository.saveAndFlush(mockedParameter);

        //@formatter:off
        restEtlMockMvc.perform(delete(BASE_URI.concat("/{idEtl}").concat("/parameters").concat("/{idParameter}"), createdEtl.getId(), createdParameter.getId())
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string("X-coetl-alert", "coetlApp.parameter.deleted"))
            .andExpect(header().string("X-coetl-params", createdParameter.getId().toString()));
        //@formatter:on
    }

    @Test
    @Transactional
    public void deleteParameter_isStatusBadRequest_ifEtlIsDeleted() throws IOException, SQLException, Exception {
        Etl mockedEtl = mockDeletedEntity();
        mockedEtl.setOrganismo(etlOrganism.getId());
        Etl createdEtl = etlRepository.saveAndFlush(mockedEtl);

        Parameter mockedParameter = mockParameterEntity(createdEtl);
        Parameter createdParameter = parameterRepository.saveAndFlush(mockedParameter);

        //@formatter:off
        restEtlMockMvc.perform(delete(BASE_URI.concat("/{idEtl}").concat("/parameters").concat("/{idParameter}"), createdEtl.getId(), createdParameter.getId())
                .accept(MediaType.APPLICATION_JSON_UTF8))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(header().string("X-coetl-error", "error.entity.deleted"))
        .andExpect(header().string("X-coetl-params", "etl"));
        //@formatter:on
    }

    @Test
    @Transactional
    public void deleteParameter_isStatusNotFound_ifReferencesAnotherEtl() throws IOException, SQLException, Exception {
        Etl mockedEtl = mockDeletedEntity();
        mockedEtl.setOrganismo(etlOrganism.getId());
        Etl createdEtl = etlRepository.saveAndFlush(mockedEtl);

        Etl anotherMockedEtl = mockEntity();
        anotherMockedEtl.setCode(UPDATED_CODE);
        anotherMockedEtl.setOrganismo(newOrganismo.getId());
        Etl anotherCreatedEtl = etlRepository.saveAndFlush(anotherMockedEtl);

        Parameter mockedParameter = mockParameterEntity(createdEtl);
        Parameter createdParameter = parameterRepository.saveAndFlush(mockedParameter);

        //@formatter:off
        restEtlMockMvc.perform(delete(BASE_URI.concat("/{idEtl}").concat("/parameters").concat("/{idParameter}"), anotherCreatedEtl.getId(), createdParameter.getId())
                .accept(MediaType.APPLICATION_JSON_UTF8))
        .andDo(print())
        .andExpect(status().isNotFound());
        //@formatter:on
    }

    @Test
    @Transactional
    public void getParameter_isStatusOk() throws IOException, SQLException, Exception {
        Etl mockedEtl = mockEntity();
        mockedEtl.setOrganismo(newOrganismo.getId());
        Etl createdEtl = etlRepository.saveAndFlush(mockedEtl);

        Parameter mockedParameter = mockParameterEntity(createdEtl);
        Parameter createdParameter = parameterRepository.saveAndFlush(mockedParameter);

        //@formatter:off
        restEtlMockMvc.perform(get(BASE_URI.concat("/{idEtl}").concat("/parameters").concat("/{idParameter}"), createdEtl.getId(), createdParameter.getId())
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id").value(createdParameter.getId()))
            .andExpect(jsonPath("$.key").value(DEFAULT_ETL_PARAMETER_KEY))
            .andExpect(jsonPath("$.value").value(DEFAULT_ETL_PARAMETER_VALUE))
            .andExpect(jsonPath("$.type").value(DEFAULT_ETL_PARAMETER_TYPE.name()))
            .andExpect(jsonPath("$.etlId").value(createdEtl.getId()))
            .andExpect(jsonPath("$.optLock").value(0))
            .andExpect(jsonPath("$.description").value(UPDATED_ETL_PARAMETER_DESCRIPTION));
        //@formatter:on
    }

    @Test
    @Transactional
    public void getParameter_isStatusNotFound_ifReferencesAnotherEtl() throws IOException, SQLException, Exception {
        Etl mockedEtl = mockEntity();
        mockedEtl.setOrganismo(newOrganismo.getId());
        Etl createdEtl = etlRepository.saveAndFlush(mockedEtl);

        Etl anotherMockedEtl = mockEntity();
        anotherMockedEtl.setCode(UPDATED_CODE);
        anotherMockedEtl.setOrganismo(newOrganismo.getId());
        Etl anotherCreatedEtl = etlRepository.saveAndFlush(anotherMockedEtl);

        Parameter mockedParameter = mockParameterEntity(createdEtl);
        Parameter createdParameter = parameterRepository.saveAndFlush(mockedParameter);

        //@formatter:off
        restEtlMockMvc.perform(get(BASE_URI.concat("/{idEtl}").concat("/parameters").concat("/{idParameter}"), anotherCreatedEtl.getId(), createdParameter.getId())
                .accept(MediaType.APPLICATION_JSON_UTF8))
        .andDo(print())
        .andExpect(status().isNotFound());
        //@formatter:on
    }

    @Test
    @Transactional
    public void findParameters_isStatusOk() throws IOException, SQLException, Exception {
        Etl mockedEtl = mockEntity();
        mockedEtl.setOrganismo(etlOrganism.getId());
        Etl createdEtl = etlRepository.saveAndFlush(mockedEtl);

        Parameter mockedParameter = mockParameterEntity(createdEtl);
        Parameter createdParameter = parameterRepository.saveAndFlush(mockedParameter);

        //@formatter:off
        restEtlMockMvc.perform(get(BASE_URI.concat("/{idEtl}").concat("/parameters"), createdEtl.getId())
                .accept(MediaType.APPLICATION_JSON_UTF8))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.[*].id").value(hasItem(createdParameter.getId().intValue())))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_ETL_PARAMETER_KEY)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_ETL_PARAMETER_VALUE)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_ETL_PARAMETER_TYPE.name())))
            .andExpect(jsonPath("$.[*].etlId").value(hasItem(createdEtl.getId().intValue())))
            .andExpect(jsonPath("$.[*].optLock").value(hasItem(0)))
            .andExpect(jsonPath("$.[*].description").value(UPDATED_ETL_PARAMETER_DESCRIPTION));
        //@formatter:on
    }


    @Test
    @Transactional
    public void givenParameterValuePassword_whenEncrypt_thenSuccess() {
        String value = "password";

        String cipherText = SecurityUtils.passwordEncoder(value, aesProperties);
        String decryptedCipherText = SecurityUtils.passwordDecode(cipherText, aesProperties);
        assertEquals(value, decryptedCipherText);
    }
}
