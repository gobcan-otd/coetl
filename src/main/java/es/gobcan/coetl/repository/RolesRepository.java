package es.gobcan.coetl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gobcan.coetl.domain.Roles;

@Repository
public interface RolesRepository extends JpaRepository<Roles, Long> {

}
