package es.gobcan.coetl.web.rest.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("permisos")
public class UsuarioRolOrganismoDTO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2767031553485001400L;
    private Long idUsuario;
    private Long idRol;
    private Long idOrganismo;
    
    @JsonCreator
    public UsuarioRolOrganismoDTO(@JsonProperty("idUsuario")Long idUsuario, @JsonProperty("idRol")Long idRol, @JsonProperty("idOrganismo")Long idOrganismo) {
        this.idUsuario = idUsuario;
        this.idRol = idRol;
        this.idOrganismo = idOrganismo;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
    }

    public Long getIdOrganismo() {
        return idOrganismo;
    }

    public void setIdOrganismo(Long idOrganismo) {
        this.idOrganismo = idOrganismo;
    }

}
