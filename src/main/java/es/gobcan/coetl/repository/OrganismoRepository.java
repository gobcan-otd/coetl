package es.gobcan.coetl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.gobcan.coetl.domain.Organismo;

@Repository
public interface OrganismoRepository extends JpaRepository<Organismo, Long> {

    Organismo findOneById(Long organizationInCharge);

    Organismo findByName(String name);

    Organismo findByNameAndIdNot(String name, Long id);

    List<Organismo> findAllByOrderByNameAsc();

}
