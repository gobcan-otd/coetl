package es.gobcan.coetl.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.gobcan.coetl.domain.Execution;
import es.gobcan.coetl.domain.Execution.Result;

@Repository
public interface ExecutionRepository extends JpaRepository<Execution, Long> {

    Page<Execution> findAllByEtlId(Long idEtl, Pageable pageable);

    boolean existsByResultInAndEtlId(List<Result> results, Long idEtl);

    List<Execution> findByResult(Result running);

    Execution findFirstByResultOrderByPlanningDateAsc(Result waiting);

    Execution findFirstByEtlIdOrderByPlanningDateDesc(Long idEtl);

    @Query(value = "select e.id, e.planning_date, e.start_date, e.finish_date, e.\"type\", e.\"result\", e.notes, e.etl_fk, e.id_execution, e.executor"
            + "from tb_executions e where e.etl_fk = ?1 and to_char(e.planning_date, 'DD/MM/YYYY') = ?2 and e.\"result\" = ?3 order by e.id desc limit 1", nativeQuery = true)
    Execution findFirstByEtlIdAndPlanningDateAndResultOrderByIdDesc(Long idEtl, String planningExecutionDate, String running);

    Execution findFirstByEtlIdAndResultOrderByIdDesc(Long idEtl, Result running);

    @Query(value = "select e.id,e.planning_date,e.start_date,e.finish_date,e.\"type\",e.\"result\",e.notes,e.etl_fk,e.id_execution, e.executor from tb_executions e where e.etl_fk = ?1 and to_char(e.planning_date, 'DD/MM/YYYY') = ?2 order by e.id desc limit 1", nativeQuery = true)
    Execution findFirstByEtlIdAndPlanningDateOrderByIdDesc(Long idEtl, String planningExecutionDate);

}
