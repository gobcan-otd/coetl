package es.gobcan.coetl.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.web.rest.dto.EtlDTO;

public interface EtlService {

    public Etl create(Etl etl);
    public Etl update(Etl etl);
    public Etl delete(Etl etl);
    public Etl restore(Etl etl);
    public Etl findOne(Long id);
    public Page<Etl> findAll(String query, boolean includeDeleted, Pageable pageable, List<Long> organismosId, String lastExecutionStartDate, String lastExecutionResult);
    public void execute(Etl etl);

    public boolean goingToChangeRepository(EtlDTO etlDto);

}
