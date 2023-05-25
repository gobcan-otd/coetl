package es.gobcan.coetl.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gobcan.coetl.domain.UsuarioRolOrganismo;

@Repository
public interface UsuarioRolOrganismoRepository extends JpaRepository<UsuarioRolOrganismo, Long> {

    List<UsuarioRolOrganismo> findByIdUsuario(Long idUsuario);
    List<UsuarioRolOrganismo> findByIdUsuario(Long idUsuario, Sort sort);
    List<UsuarioRolOrganismo> findByIdOrganismo(Long idOrganismo);

}
