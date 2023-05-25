package es.gobcan.coetl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gobcan.coetl.domain.Health;

@Repository
public interface HealthRepository extends JpaRepository<Health, Long> {

}
