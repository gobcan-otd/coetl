package es.gobcan.coetl.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
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
import net.schmizz.sshj.userauth.password.AccountResource;

/**
 * Test class for the AccountResource REST controller.
 *
 * @see AccountResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoetlApp.class)
public class AccountResourceIntTest {

    private static final String ROL_EXAMPLE = "ROLE";

    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
    private UsuarioService userService;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @SuppressWarnings("rawtypes")
    @Autowired
    private HttpMessageConverter[] httpMessageConverters;

    @Mock
    private UsuarioService mockUserService;

    @Mock
    private MailService mockMailService;

    @Autowired
    private AuditEventPublisher auditPublisher;

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
    private UsuarioRolOrganismoService usuarioRolOrganismoService;

    private MockMvc restUserMockMvc;

    private MockMvc restMvc;
    private Organismo newOrganismo;
    private Roles newRol;
    private UsuarioRolOrganismo rol1;
    private Usuario newUser;
    List<UsuarioRolOrganismo> roles;

    private void mockRolSet() {
        newOrganismo = new Organismo();
        newOrganismo = new Organismo();
        newOrganismo.setDescription("Description1");
        newOrganismo.setName("Name1");
        organismoRepository.saveAndFlush(newOrganismo);
        
        newRol = new Roles();
        newRol.setName(ROL_EXAMPLE);
        rolesRepository.saveAndFlush(newRol);

        rol1 = new UsuarioRolOrganismo();
        rol1.setOrganismo(newOrganismo);
        rol1.setRol(newRol);
        rol1.setIdUsuario(newUser.getId());
        rol1.setIdRol(newRol.getId());
        rol1.setIdOrganismo(newOrganismo.getId());
        usuarioRolOrganismoRepository.saveAndFlush(rol1);

    }
    
    private List<UsuarioRolOrganismo> getPermisos() {
        roles = new ArrayList<>();
        roles.add(rol1);
        return roles;
    }

    private void createUser() {
        newUser = new Usuario();
        newUser.setLogin("test");
        newUser.setNombre("john");
        newUser.setApellido1("doe");
        newUser.setEmail("john.doe@jhipster.com");
        usuarioRepository.saveAndFlush(newUser);
    }

    private void setPermisos() {
        newUser.setUsuarioRolOrganismo(getPermisos());
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        UsuarioResource accountResource = new UsuarioResource(userRepository, null, userService, usuarioMapper, auditPublisher, usuarioRolOrganismoService);

        UsuarioResource accountUserMockResource = new UsuarioResource(userRepository, null, mockUserService, usuarioMapper, auditPublisher, usuarioRolOrganismoService);

        this.restMvc = MockMvcBuilders.standaloneSetup(accountResource).setMessageConverters(httpMessageConverters).setControllerAdvice(exceptionTranslator).build();
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource).build();

        createUser();
        mockRolSet();
        setPermisos();
    }

    @Test
    @Transactional
    public void testNonautenticardUser() throws Exception {
        restUserMockMvc.perform(get("/api/autenticar").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(content().string(""));
    }

    @Test
    @Transactional
    public void testautenticardUser() throws Exception {
        restUserMockMvc.perform(get("/api/autenticar").with(request -> {
            request.setRemoteUser("test");
            return request;
        }).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(content().string("test"));
    }

    @Test
    @Transactional
    public void testGetExistingAccount() throws Exception {

        when(mockUserService.getUsuarioWithAuthorities()).thenReturn(newUser);

        restUserMockMvc.perform(get("/api/usuario").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.login").value("test")).andExpect(jsonPath("$.nombre").value("john")).andExpect(jsonPath("$.apellido1").value("doe"))
                .andExpect(jsonPath("$.email").value("john.doe@jhipster.com"))
                .andExpect(jsonPath("$.usuarioRolOrganismo[*].rol.name").value(AccountResourceIntTest.ROL_EXAMPLE));
    }

    @Test
    @Transactional
    public void testGetUnknownAccount() throws Exception {
        when(mockUserService.getUsuarioWithAuthorities()).thenReturn(null);

        restUserMockMvc.perform(get("/api/usuario").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    @Transactional
    @WithMockUser("save-account")
    public void testSaveAccount() throws Exception {

        Usuario user = new Usuario();
        user.setLogin("save-account");
        user.setEmail("save-account@example.com");
        user.setIsAdmin(true);
        userRepository.saveAndFlush(user);
        //@formatter:off
		UsuarioDTO userDTO = UsuarioDTO.builder()
				.setId(user.getId())
				.setOptLock(user.getOptLock())
				.setLogin(user.getLogin())
				.setFirstName("firstname")
				.setLastName("lastname")
				.setEmail("save-account@example.com")
				.setIsAdmin(false)
				.setCreatedBy(null)
				.setCreatedDate(null)
				.setLastModifiedBy(null)
				.setLastModifiedDate(null)
				.setAuthorities(roles)
				.build();
		//@formatter:on

        restMvc.perform(put("/api/usuarios").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(userDTO))).andExpect(status().isOk());

        Usuario updatedUser = userRepository.findOneByLogin(user.getLogin()).orElse(null);
        assertThat(updatedUser.getNombre()).isEqualTo(userDTO.getNombre());
        assertThat(updatedUser.getApellido1()).isEqualTo(userDTO.getApellido1());
        assertThat(updatedUser.getEmail()).isEqualTo(userDTO.getEmail());
        assertThat(updatedUser.getUsuarioRolOrganismo()).size().isEqualTo(1);
    }

    @Test
    @Transactional
    @WithMockUser("save-invalid-email")
    public void testSaveInvalidEmail() throws Exception {
        Usuario user = new Usuario();
        user.setLogin("save-invalid-email");
        user.setEmail("save-invalid-email@example.com");

        userRepository.saveAndFlush(user);

        //@formatter:off
		UsuarioDTO userDTO = UsuarioDTO.builder()
				.setId(user.getId())
				.setLogin(user.getLogin())
				.setFirstName("firstname")
				.setLastName("lastname")
				.setEmail("invalid email")
				.setIsAdmin(false)
				.setCreatedBy(null)
				.setCreatedDate(null)
				.setLastModifiedBy(null)
				.setLastModifiedDate(null)
				.setAuthorities(roles)
				.build();
		//@formatter:on

        restMvc.perform(put("/api/usuarios").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(userDTO))).andExpect(status().isBadRequest());

        assertThat(userRepository.findOneByEmail("invalid email")).isNotPresent();
    }

    @Test
    @Transactional
    @WithMockUser("save-existing-email")
    public void testSaveExistingEmailWhenEmailIsUsedByAnotherUser() throws Exception {
        Usuario user = new Usuario();
        user.setLogin("save-existing-email");
        user.setEmail("save-existing-email@example.com");

        userRepository.saveAndFlush(user);

        Usuario anotherUser = new Usuario();
        anotherUser.setLogin("save-existing-email2");
        anotherUser.setEmail("save-existing-email2@example.com");

        userRepository.saveAndFlush(anotherUser);
        //@formatter:off
		UsuarioDTO userDTO = UsuarioDTO.builder()
				.setId(user.getId())
				.setOptLock(user.getOptLock())
				.setLogin(user.getLogin())
				.setFirstName("firstname")
				.setLastName("lastname")
				.setEmail("save-existing-email2@example.com")
				.setIsAdmin(false)
                .setCreatedBy(null)
                .setCreatedDate(null)
                .setLastModifiedBy(null)
                .setLastModifiedDate(null)
                .setAuthorities(null)
				.build();
		//@formatter:on

        restMvc.perform(put("/api/usuarios").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(userDTO))).andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser("save-existing-email-and-login")
    public void testSaveExistingEmailWhenIsNotBeingUsed() throws Exception {
        Usuario user = new Usuario();
        user.setLogin("save-existing-email-and-login");
        user.setEmail("save-existing-email-and-login@example.com");
        user.setUsuarioRolOrganismo(roles);

        userRepository.saveAndFlush(user);
        //@formatter:off
		UsuarioDTO userDTO = UsuarioDTO.builder()
				.setId(user.getId())
				.setOptLock(user.getOptLock())
				.setLogin(user.getLogin())
				.setFirstName("firstname")
				.setLastName("lastname")
				.setEmail("save-existing-email-and-login2@example.com")
				.setIsAdmin(false)
				.setCreatedBy(null)
				.setCreatedDate(null)
				.setLastModifiedBy(null)
				.setLastModifiedDate(null)
				.setAuthorities(null)
				.setAuthorities(roles)
				.build();
		//@formatter:on

        restMvc.perform(put("/api/usuarios").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(userDTO))).andExpect(status().isOk());

        Usuario updatedUser = userRepository.findOneByLogin("save-existing-email-and-login").orElse(null);
        assertThat(updatedUser.getEmail()).isEqualTo("save-existing-email-and-login2@example.com");
    }
}
