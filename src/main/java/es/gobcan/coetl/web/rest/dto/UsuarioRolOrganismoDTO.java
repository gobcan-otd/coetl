package es.gobcan.coetl.web.rest.dto;

import java.io.Serializable;

import es.gobcan.coetl.domain.Organismo;
import es.gobcan.coetl.domain.Roles;

public class UsuarioRolOrganismoDTO implements Serializable {

    private static final long serialVersionUID = -2729529757088120751L;

    private Long id;
    private Roles rol;
    private Organismo organismo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Roles getRol() {
        return rol;
    }

    public void setRol(Roles rol) {
        this.rol = rol;
    }

    public Organismo getOrganismo() {
        return organismo;
    }

    public void setOrganismo(Organismo organismo) {
        this.organismo = organismo;
    }

}
