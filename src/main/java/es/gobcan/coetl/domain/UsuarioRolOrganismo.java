package es.gobcan.coetl.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "tb_usuario_rol_organismo")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UsuarioRolOrganismo implements Serializable {

    private static final long serialVersionUID = 6216201787109768159L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_rol_organismo_id_seq")
    @SequenceGenerator(name = "usuario_rol_organismo_id_seq", sequenceName = "usuario_rol_organismo_id_seq", initialValue = 10)
    private Long id;

    @ManyToOne(optional = false, targetEntity = Usuario.class)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false, targetEntity = Roles.class)
    @JoinColumn(name = "id_rol")
    private Roles rol;

    @ManyToOne(fetch = FetchType.EAGER, optional = false, targetEntity = Organismo.class)
    @JoinColumn(name = "id_organismo")
    private Organismo organismo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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

    @PrePersist
    @PreUpdate
    public void updateOrganismoAssociation() {
        if (this.usuario != null && this.rol != null && this.organismo != null) {
            this.setUsuario(this.usuario);
            this.setRol(this.rol);
            this.setOrganismo(this.organismo);
        }
    }

}
