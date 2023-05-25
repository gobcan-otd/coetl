package es.gobcan.coetl.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "tb_usuario_rol_organismo")
public class UsuarioRolOrganismo implements Serializable {

    private static final long serialVersionUID = 6216201787109768159L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_rol_organismo_id_seq")
    @SequenceGenerator(name = "usuario_rol_organismo_id_seq", sequenceName = "usuario_rol_organismo_id_seq", initialValue = 10)
    private Long id;

    // TODO: COETL-94 - Revisar la declaración de las columnas, pues sin ellas el create y el update no funcionan
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "id_rol", nullable = false)
    private Long idRol;

    @Column(name = "id_organismo", nullable = false)
    private Long idOrganismo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_rol", insertable = false, updatable = false)
    private Roles rol;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_organismo", insertable = false, updatable = false)
    private Organismo organismo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", insertable = false, updatable = false)
    private Usuario usuario;

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

    @PrePersist
    @PreUpdate
    public void updateOrganismoAssociation() {
        if (rol != null && organismo != null && usuario != null) {
            this.setIdOrganismo(organismo.getId());
            this.setIdRol(rol.getId());
            this.setIdUsuario(usuario.getId());
        }
    }

}
