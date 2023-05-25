package es.gobcan.coetl.repository;

import java.util.List;
import java.util.Optional;

import org.hibernate.criterion.DetachedCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gobcan.coetl.domain.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findOneByEmail(String email);

    Boolean existsUsuarioByEmail(String email);

    Boolean existsUsuarioByEmailAndIdIsNot(String email, Long id);

    Optional<Usuario> findOneByLogin(String login);

    @EntityGraph(attributePaths = "roles")
    Usuario findOneWithRolesByIdAndDeletionDateIsNull(Long id);

    Optional<Usuario> findOneByLoginAndDeletionDateIsNull(String login);

    Page<Usuario> findAll(DetachedCriteria criteria, Pageable pageable);
    
    List<Usuario> findByIsAdminTrue();

}
