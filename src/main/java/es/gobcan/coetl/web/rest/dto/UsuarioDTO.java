package es.gobcan.coetl.web.rest.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import es.gobcan.coetl.config.Constants;
import es.gobcan.coetl.domain.UsuarioRolOrganismo;

public class UsuarioDTO extends AbstractVersionedAndAuditingWithDeletionDTO {

    private Long id;

    @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 255)
    private String login;

    @Size(max = 255)
    private String nombre;

    @Size(max = 255)
    private String apellido1;

    @Size(max = 255)
    private String apellido2;

    @Email
    @Size(min = 3, max = 255)
    private String email;
    
    private Boolean isAdmin;

    private List<UsuarioRolOrganismo> usuarioRolOrganismo;

    public UsuarioDTO() {
        // Empty constructor needed for Jackson.
    }

    public void updateFrom(UsuarioDTO source) {
        this.id = source.getId();
        this.login = source.getLogin();
        this.nombre = source.getNombre();
        this.apellido1 = source.getApellido1();
        this.apellido2 = source.getApellido2();
        this.email = source.getEmail();
        this.isAdmin = source.getIsAdmin();
        this.usuarioRolOrganismo = new ArrayList<>(source.getUsuarioRolOrganismo());
        this.setCreatedBy(source.getCreatedBy());
        this.setCreatedDate(source.getCreatedDate());
        this.setLastModifiedBy(source.getLastModifiedBy());
        this.setLastModifiedDate(source.getLastModifiedDate());
        this.setDeletedBy(source.getDeletedBy());
        this.setDeletionDate(source.getDeletionDate());
        this.setOptLock(source.getOptLock());
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido1() {
        return apellido1;
    }

    public String getApellido2() {
        return apellido2;
    }

    public String getEmail() {
        return email;
    }
    
    public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public List<UsuarioRolOrganismo> getUsuarioRolOrganismo() {
        return usuarioRolOrganismo;
    }

    public void setUsuarioRolOrganismo(List<UsuarioRolOrganismo> usuarioRolOrganismo) {
        if (usuarioRolOrganismo == null) {
            this.usuarioRolOrganismo = new ArrayList<>();
        } else {
            this.usuarioRolOrganismo = new ArrayList<>(usuarioRolOrganismo);
        }
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setApellido1(String lastName) {
        this.apellido1 = lastName;
    }

    public void setApellido2(String lastName) {
        this.apellido2 = lastName;
    }

    public void setNombre(String firstName) {
        this.nombre = firstName;
    }

    @Override
    public String toString() {
        return "UsuarioDTO (id = " + getId() + ", Nombre = " + getNombre() + ", Apellido1 = " + getApellido1() + ", Apellido2 = " + getApellido2() + ")";
    }

    public static class Builder {

        private Long id;
        private String login;
        private String firstName;
        private String lastName;
        private String lastName2;
        private String email;
        private Boolean isAdmin;
        private String createdBy;
        private Instant createdDate;
        private String lastModifiedBy;
        private Instant lastModifiedDate;
        private String deletedBy;
        private Instant deletionDate;
        private List<UsuarioRolOrganismo> authorities;
        private Long optLock;

        public UsuarioDTO build() {
            UsuarioDTO userDTO = new UsuarioDTO();
            userDTO.setId(this.id);
            userDTO.setOptLock(this.optLock);
            userDTO.setLogin(this.login);
            userDTO.setNombre(this.firstName);
            userDTO.setApellido1(this.lastName);
            userDTO.setApellido2(this.lastName2);
            userDTO.setEmail(this.email);
            userDTO.setIsAdmin(this.isAdmin);
            userDTO.setCreatedBy(this.createdBy);
            userDTO.setCreatedDate(this.createdDate);
            userDTO.setLastModifiedBy(this.lastModifiedBy);
            userDTO.setLastModifiedDate(this.lastModifiedDate);
            userDTO.setDeletedBy(this.deletedBy);
            userDTO.setDeletionDate(this.deletionDate);
            userDTO.setUsuarioRolOrganismo(this.authorities);
            return userDTO;
        }

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setOptLock(Long optLock) {
            this.optLock = optLock;
            return this;
        }

        public Builder setLogin(String login) {
            this.login = login;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setLastName2(String lastName) {
            this.lastName2 = lastName;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }
        
        public Builder setIsAdmin(Boolean isAdmin) {
            this.isAdmin = isAdmin;
            return this;
        }

        public Builder setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder setCreatedDate(Instant createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public Builder setLastModifiedBy(String lastModifiedBy) {
            this.lastModifiedBy = lastModifiedBy;
            return this;
        }

        public Builder setLastModifiedDate(Instant lastModifiedDate) {
            this.lastModifiedDate = lastModifiedDate;
            return this;
        }

        public Builder setDeletedBy(String deletedBy) {
            this.deletedBy = deletedBy;
            return this;
        }

        public Builder setDeletionDate(Instant deletionDate) {
            this.deletionDate = deletionDate;
            return this;
        }

        public Builder setAuthorities(List<UsuarioRolOrganismo> authorities) {
            this.authorities = authorities;
            return this;
        }

    }
}
