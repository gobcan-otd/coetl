package es.gobcan.coetl.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import es.gobcan.coetl.CoetlApp;
import es.gobcan.coetl.config.audit.AuditEventPublisher;
import es.gobcan.coetl.domain.Organismo;
import es.gobcan.coetl.domain.Roles;
import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.domain.UsuarioRolOrganismo;
import es.gobcan.coetl.domain.enumeration.Rol;
import es.gobcan.coetl.errors.ExceptionTranslator;
import es.gobcan.coetl.repository.OrganismoRepository;
import es.gobcan.coetl.repository.RolesRepository;
import es.gobcan.coetl.repository.UsuarioRepository;
import es.gobcan.coetl.repository.UsuarioRolOrganismoRepository;
import es.gobcan.coetl.service.MailService;
import es.gobcan.coetl.service.UsuarioRolOrganismoService;
import es.gobcan.coetl.service.UsuarioService;
import es.gobcan.coetl.web.rest.dto.UsuarioDTO;
import es.gobcan.coetl.web.rest.mapper.UsuarioMapper;
import es.gobcan.coetl.web.rest.vm.ManagedUserVM;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UsuarioResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoetlApp.class)
public class UsuarioResourceIntTest {

    private static final String ENDPOINT_URL = "/api/usuarios";

    private static final String DEFAULT_LOGIN_NEW_USER = "johndoe";
    private static final String DEFAULT_LOGIN_EXISTING_USER = "alice";
    private static final String DEFAULT_EMAIL = "johndoe@localhost";
    private static final String DEFAULT_NEW_EMAIL = "jtest@localhost";
    private static final String DEFAULT_NOMBRE = "john";
    private static final String DEFAULT_PRIMER_APELLIDO = "doe";

    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
    UsuarioMapper usuarioMapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private UsuarioService userService;

    @Autowired
    private UsuarioMapper userMapper;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    OrganismoRepository organismoRepository;

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    UsuarioRolOrganismoRepository usuarioRolOrganismoRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager em;

    private MockMvc restUserMockMvc;

    private Usuario newUser;
    private Usuario existingUser;

    private Organismo newOrganismo;
    private Roles newRol;
    private UsuarioRolOrganismo rol1;
    private Usuario newUser2;
    List<UsuarioRolOrganismo> roles;

    @Autowired
    private AuditEventPublisher auditPublisher;

    @Autowired
    private UsuarioRolOrganismoService usuarioRolOrganismoService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        UsuarioResource userResource = new UsuarioResource(userRepository, mailService, userService, userMapper, auditPublisher, usuarioRolOrganismoService);
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(userResource).setCustomArgumentResolvers(pageableArgumentResolver).setControllerAdvice(exceptionTranslator)
                .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create a User.
     * This is a static method, as tests for other entities might also need it, if
     * they test an entity which has a required relationship to the User entity.
     */
    public static Usuario createEntity(String login) {
        Usuario user = new Usuario();
        user.setLogin(login);
        user.setEmail(DEFAULT_EMAIL);
        user.setNombre(DEFAULT_NOMBRE);
        user.setApellido1(DEFAULT_PRIMER_APELLIDO);
        return user;
    }

    public static Usuario mockEntityWithoutId(String login){
        Usuario user = new Usuario();
        user.setLogin(login);
        user.setEmail(DEFAULT_NEW_EMAIL);
        user.setNombre(DEFAULT_NOMBRE);
        user.setApellido1(DEFAULT_PRIMER_APELLIDO);
        return user;
    }

    private void setRolOrganismo() {
        newOrganismo = new Organismo();
        newOrganismo = new Organismo();
        newOrganismo.setDescription("Description1");
        newOrganismo.setName("Name1");
        organismoRepository.saveAndFlush(newOrganismo);
        
        newRol = new Roles();
        newRol.setName(Rol.TECNICO.name());
        rolesRepository.saveAndFlush(newRol);

        rol1 = new UsuarioRolOrganismo();
        rol1.setOrganismo(newOrganismo);
        rol1.setRol(newRol);
        rol1.setIdUsuario(newUser2.getId());
        rol1.setIdRol(newRol.getId());
        rol1.setIdOrganismo(newOrganismo.getId());
        usuarioRolOrganismoRepository.saveAndFlush(rol1);

    }
    
    private List<UsuarioRolOrganismo> getPermisos() {
        roles = new ArrayList<>();
        roles.add(rol1);
        return roles;
    }

    private void createUser2() {
        newUser2 = new Usuario();
        newUser2.setLogin("test");
        newUser2.setNombre("john");
        newUser2.setApellido1("doe");
        newUser2.setEmail("john.doe@jhipster.com");
        usuarioRepository.saveAndFlush(newUser2);
    }

    private void setPermisos() {
        newUser.setUsuarioRolOrganismo(getPermisos());
    }

    @Before
    public void initTest() {
        newUser = mockEntityWithoutId(DEFAULT_LOGIN_NEW_USER);
        existingUser = createEntity(DEFAULT_LOGIN_EXISTING_USER);
        em.persist(existingUser);

        createUser2();
        setRolOrganismo();
        setPermisos();
    }

    @Test
    @Transactional
    public void createUser() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        UsuarioDTO source = usuarioMapper.userToUserDTO(newUser);
        source.setUsuarioRolOrganismo(roles);
        managedUserVM.updateFrom(source);

        restUserMockMvc.perform(post(ENDPOINT_URL).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(managedUserVM))).andExpect(status().isCreated());

        // Validate the User in the database
        List<Usuario> userList = userRepository.findAll().stream().sorted((u1, u2) -> u2.getId().compareTo(u1.getId())).collect(Collectors.toList());
        assertThat(userList).hasSize(databaseSizeBeforeCreate + 1);
        Usuario testUser = userList.get(0);
        assertThat(testUser.getLogin()).isEqualTo(DEFAULT_LOGIN_NEW_USER);
        assertThat(testUser.getNombre()).isEqualTo(DEFAULT_NOMBRE);
        assertThat(testUser.getApellido1()).isEqualTo(DEFAULT_PRIMER_APELLIDO);
        assertThat(testUser.getEmail()).isEqualTo(DEFAULT_NEW_EMAIL);
    }

    @Test
    @Transactional
    public void createUserWithExistingId() throws Exception {
        userRepository.save(newUser);
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        Usuario userWithExistingId = userRepository.findOne(newUser.getId());
        userWithExistingId.setLogin("anotherlogin");
        userWithExistingId.setEmail("anothermail@localhost");

        ManagedUserVM managedUserVM = new ManagedUserVM();
        UsuarioDTO source = usuarioMapper.userToUserDTO(userWithExistingId);
        managedUserVM.updateFrom(source);

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserMockMvc.perform(post(ENDPOINT_URL).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(managedUserVM))).andExpect(status().isBadRequest());

        // Validate the User in the database
        List<Usuario> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUserWithExistingLogin() throws Exception {
        // Initialize the database
        userRepository.save(newUser);
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        Usuario userWithExistingLogin = new Usuario();
        userWithExistingLogin.setId(null);
        userWithExistingLogin.setLogin(newUser.getLogin());
        userWithExistingLogin.setNombre("anothername");
        userWithExistingLogin.setApellido1("anotherelastname");
        userWithExistingLogin.setEmail("another@localhost");

        ManagedUserVM managedUserVM = new ManagedUserVM();
        UsuarioDTO source = usuarioMapper.userToUserDTO(userWithExistingLogin);
        managedUserVM.updateFrom(source);

        // Create the User
        restUserMockMvc.perform(post(ENDPOINT_URL).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(managedUserVM))).andExpect(status().isBadRequest());

        // Validate the User in the database
        List<Usuario> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUserWithExistingEmail() throws Exception {

        // Initialize the database
        userRepository.save(newUser);
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        Usuario userWithExistingEmail = new Usuario();
        userWithExistingEmail.setId(null);
        userWithExistingEmail.setLogin("anotherlogin");
        userWithExistingEmail.setNombre("anothername");
        userWithExistingEmail.setApellido1("anotherelastname");
        userWithExistingEmail.setEmail(newUser.getEmail());

        ManagedUserVM managedUserVM = new ManagedUserVM();
        UsuarioDTO source = usuarioMapper.userToUserDTO(userWithExistingEmail);
        managedUserVM.updateFrom(source);

        // Create the User
        restUserMockMvc.perform(post(ENDPOINT_URL).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(managedUserVM))).andExpect(status().isBadRequest());

        // Validate the User in the database
        List<Usuario> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }
    
    @Test
    @Transactional
    public void createAdminUser() throws Exception {
    	int databaseSizeBeforeCreate = userRepository.findAll().size();
    	ManagedUserVM managedUserVM = new ManagedUserVM();
        UsuarioDTO source = usuarioMapper.userToUserDTO(newUser);
        source.setUsuarioRolOrganismo(null);
        source.setIsAdmin(true);
        managedUserVM.updateFrom(source);
        
        restUserMockMvc.perform(post(ENDPOINT_URL).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(managedUserVM))).andExpect(status().isCreated());
        
        // Validate the User in the database
        List<Usuario> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate + 1);
    }
    
    @Test
    @Transactional
    public void createAdminUserWithRoleOrganism() throws Exception {
    	int databaseSizeBeforeCreate = userRepository.findAll().size();
    	ManagedUserVM managedUserVM = new ManagedUserVM();
        UsuarioDTO source = usuarioMapper.userToUserDTO(newUser);
        source.setIsAdmin(true);
        source.setUsuarioRolOrganismo(roles);
        managedUserVM.updateFrom(source);
        
        restUserMockMvc.perform(post(ENDPOINT_URL).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(managedUserVM))).andExpect(status().isBadRequest());
        
        // Validate the User in the database
        List<Usuario> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllUsers() throws Exception {
        // Initialize the database
        userRepository.save(newUser);

        // Get all the users
        restUserMockMvc.perform(get("/api/usuarios?sort=id,desc").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN_NEW_USER))).andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
                .andExpect(jsonPath("$.[*].apellido1").value(hasItem(DEFAULT_PRIMER_APELLIDO))).andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)));
    }

    @Test
    @Transactional
    public void getUser() throws Exception {
        // Initialize the database
        userRepository.save(newUser);

        // Get the user
        restUserMockMvc.perform(get("/api/usuarios/{login}", newUser.getLogin())).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.login").value(newUser.getLogin())).andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE)).andExpect(jsonPath("$.apellido1").value(DEFAULT_PRIMER_APELLIDO))
                .andExpect(jsonPath("$.email").value(DEFAULT_NEW_EMAIL));
    }

    @Test
    @Transactional
    public void getNonExistingUser() throws Exception {
        restUserMockMvc.perform(get("/api/usuarios/unknown")).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateUser() throws Exception {

        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        Usuario updatedUser = userRepository.findOne(existingUser.getId());

        ManagedUserVM managedUserVM = new ManagedUserVM();
        UsuarioDTO source = usuarioMapper.userToUserDTO(updatedUser);
        source.setLogin("daniel");
        source.setNombre("Daniel");
        source.setApellido1("Smith");
        source.setApellido2("Down");
        source.setEmail("email@email.com");
        source.setUsuarioRolOrganismo(roles);
        managedUserVM.updateFrom(source);

        restUserMockMvc.perform(put(ENDPOINT_URL).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(managedUserVM))).andExpect(status().isOk());

        List<Usuario> userList = userRepository.findAll().stream().sorted((u1, u2) -> u2.getId().compareTo(u1.getId())).collect(Collectors.toList());
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
        Usuario testUser = userList.get(1);
        assertThat(testUser.getLogin()).isEqualTo(source.getLogin());
        assertThat(testUser.getNombre()).isEqualTo(source.getNombre());
        assertThat(testUser.getApellido1()).isEqualTo(source.getApellido1());
        assertThat(testUser.getApellido2()).isEqualTo(source.getApellido2());
        assertThat(testUser.getEmail()).isEqualTo(source.getEmail());
    }

    @Test
    @Transactional
    public void updateUserExistingEmail() throws Exception {
        Usuario anotherUser = new Usuario();
        anotherUser.setLogin("jhipster");
        anotherUser.setEmail("jhipster@localhost");
        anotherUser.setNombre("java");
        anotherUser.setApellido1("hipster");
        userRepository.save(anotherUser);

        // Update the user
        Usuario updatedUser = userRepository.findOne(existingUser.getId());

        //@formatter:off
		ManagedUserVM managedUserVM = new ManagedUserVM();
		UsuarioDTO source = usuarioMapper.userToUserDTO(updatedUser);
        source.setEmail(anotherUser.getEmail());
		managedUserVM.updateFrom(source);
		//@formatter:on

        restUserMockMvc.perform(put(ENDPOINT_URL).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(managedUserVM))).andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void updateUserExistingLogin() throws Exception {

        Usuario anotherUser = new Usuario();
        anotherUser.setLogin("jhipster");
        anotherUser.setEmail("jhipster@localhost");
        anotherUser.setNombre("java");
        anotherUser.setApellido1("hipster");
        userRepository.save(anotherUser);

        // Update the user
        Usuario updatedUser = userRepository.findOne(existingUser.getId());

        //@formatter:off
		ManagedUserVM managedUserVM = new ManagedUserVM();
		UsuarioDTO source = usuarioMapper.userToUserDTO(updatedUser);
        source.setLogin(anotherUser.getLogin());
		managedUserVM.updateFrom(source);
		//@formatter:on
        restUserMockMvc.perform(put(ENDPOINT_URL).contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(managedUserVM))).andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void deleteUser() throws Exception {
        int databaseSizeBeforeDelete = userRepository.findAll().size();

        // Delete the user
        restUserMockMvc.perform(delete("/api/usuarios/{login}", existingUser.getLogin()).accept(TestUtil.APPLICATION_JSON_UTF8)).andExpect(status().isOk());

        // Validate the database is empty
        List<Usuario> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeDelete);
        Usuario deleted = userRepository.findOne(existingUser.getId());
        assertThat(deleted.getDeletionDate()).isNotNull();
    }

    @Test
    @Transactional
    public void testUserDTOtoUser() {
        //@formatter:off
		ManagedUserVM managedUserVM = new ManagedUserVM();
		UsuarioDTO source = UsuarioDTO.builder()
				.setLogin(DEFAULT_LOGIN_NEW_USER)
				.setFirstName(DEFAULT_NOMBRE)
				.setLastName(DEFAULT_PRIMER_APELLIDO)
				.setEmail(DEFAULT_EMAIL)
				.setCreatedBy(DEFAULT_LOGIN_NEW_USER)
				.setCreatedDate(null)
				.setLastModifiedBy(DEFAULT_LOGIN_NEW_USER)
				.setLastModifiedDate(null)
				.setAuthorities(roles)
				.build();
		managedUserVM.updateFrom(source);
		//@formatter:on

        Usuario user = userMapper.userDTOToUser(source);
        assertThat(user.getId()).isEqualTo(source.getId());
        assertThat(user.getLogin()).isEqualTo(source.getLogin());
        assertThat(user.getNombre()).isEqualTo(source.getNombre());
        assertThat(user.getApellido1()).isEqualTo(source.getApellido1());
        assertThat(user.getEmail()).isEqualTo(source.getEmail());
        assertThat(user.getCreatedBy()).isNull();
        assertThat(user.getCreatedDate()).isNotNull();
        assertThat(user.getLastModifiedBy()).isNull();
        assertThat(user.getLastModifiedDate()).isNotNull();
        assertThat(user.getUsuarioRolOrganismo().get(0).getRol().getName()).isEqualTo(Rol.TECNICO.name());
    }

    @Test
    @Transactional
    public void testUserToUserDTO() {
        UsuarioDTO userDTO = userMapper.userToUserDTO(newUser);
        assertThat(userDTO.getId()).isEqualTo(newUser.getId());
        assertThat(userDTO.getLogin()).isEqualTo(newUser.getLogin());
        assertThat(userDTO.getNombre()).isEqualTo(newUser.getNombre());
        assertThat(userDTO.getApellido1()).isEqualTo(newUser.getApellido1());
        assertThat(userDTO.getEmail()).isEqualTo(newUser.getEmail());
        assertThat(userDTO.getCreatedBy()).isEqualTo(newUser.getCreatedBy());
        assertThat(userDTO.getCreatedDate()).isEqualTo(newUser.getCreatedDate());
        assertThat(userDTO.getLastModifiedBy()).isEqualTo(newUser.getLastModifiedBy());
        assertThat(userDTO.getLastModifiedDate()).isEqualTo(newUser.getLastModifiedDate());
        assertEquals(userDTO.getUsuarioRolOrganismo(), newUser.getUsuarioRolOrganismo());
        assertThat(userDTO.toString()).isNotNull();
    }
}
