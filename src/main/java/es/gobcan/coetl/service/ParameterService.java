package es.gobcan.coetl.service;

import java.util.List;
import java.util.Map;

import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParameterService {

    public List<Parameter> createDefaultParameters(Etl etl, Map<String, String> parameters);
    public void deleteDefaultParameters(Etl etl);
    public Parameter create(Parameter parameter);
    public Parameter update(Parameter parameter);
    public void delete(Parameter parameter);
    public List<Parameter> findAllByEtlId(Long etlId);
    public Map<String, String> findAllByEtlIdAsMap(Long etlId);
    public Parameter findOneByIdAndEtlId(Long id, Long etlId);

    public String decodeValueByTypology(Parameter parameter);
    Page<Parameter> findAll(Pageable pageable);
    public Parameter findOneById(Long etlId);
}
