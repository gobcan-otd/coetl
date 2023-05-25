package es.gobcan.coetl.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "tb_roles")
public class Roles implements Serializable {

    private static final long serialVersionUID = 6693527767636990261L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tb_roles_id_seq")
    @SequenceGenerator(name = "tb_roles_id_seq", sequenceName = "tb_roles_id_seq", initialValue = 10)
    private Long id;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 255)
    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
