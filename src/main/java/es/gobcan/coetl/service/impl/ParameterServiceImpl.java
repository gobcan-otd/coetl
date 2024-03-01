package es.gobcan.coetl.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import es.gobcan.coetl.config.AESProperties;
import es.gobcan.coetl.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Parameter;
import es.gobcan.coetl.domain.Parameter.Type;
import es.gobcan.coetl.repository.ParameterRepository;
import es.gobcan.coetl.service.ParameterService;
import es.gobcan.coetl.service.validator.ParameterValidator;

@Service
public class ParameterServiceImpl implements ParameterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterService.class);

    @Autowired
    private ParameterRepository parameterRepository;

    @Autowired
    private ParameterValidator parameterValidator;

    @Autowired
    private AESProperties aesProperties;

    @Override
    public List<Parameter> createDefaultParameters(Etl etl, Map<String, String> parameters) {
        LOGGER.debug("Request to create default Parameters : {} of ETL : {}", parameters, etl);
        //@formatter:off
        return parameters.entrySet().stream()
                .map(entry -> {
                    Parameter parameter = new Parameter();
                    parameter.setEtl(etl);
                    parameter.setKey(entry.getKey());
                    parameter.setValue(entry.getValue());
                    parameter.setType(Type.AUTO);
                    parameter.setTypology(Parameter.Typology.GENERIC);
                    return parameter;
                })
                .map(this::save)
                .collect(Collectors.toList());
        //@formatter:on
    }

    @Override
    public void deleteDefaultParameters(Etl etl) {
        LOGGER.debug("Request to delete default Parameters of ETL : {}", etl);
        List<Parameter> defaultParameters = parameterRepository.findAllByEtlIdAndType(etl.getId(), Type.AUTO);
        defaultParameters.forEach(this::delete);
        parameterRepository.flush();
    }

    @Override
    public Parameter create(Parameter parameter) {
        LOGGER.debug("Request to create a Parameter : {}", parameter);
        this.trimValuesParameter(parameter);
        parameterValidator.validate(parameter);
        return save(parameter);
    }

    @Override
    public Parameter update(Parameter parameter) {
        LOGGER.debug("Request to update a Parameter : {}", parameter);
        this.trimValuesParameter(parameter);
        parameterValidator.validate(parameter);
        return save(parameter);
    }

    @Override
    public void delete(Parameter parameter) {
        LOGGER.debug("Request to delete a Parameter : {}", parameter);
        parameterRepository.delete(parameter);
    }

    @Override
    public List<Parameter> findAllByEtlId(Long etlId) {
        LOGGER.debug("Request to find all Parameters by ETL: {}", etlId);
        return parameterRepository.findAllByEtlId(etlId);
    }

    @Override
    public Map<String, String> findAllByEtlIdAsMap(Long etlId) {
        List<Parameter> parameters = parameterRepository.findAllByEtlIdOrEtlNullAndType(etlId, Type.GLOBAL);
        return parameters.stream().collect(Collectors.toMap(Parameter::getKey, p -> decodeValueByTypology(p)));
    }

    @Override
    public Parameter findOneByIdAndEtlId(Long id, Long etlId) {
        LOGGER.debug("Request to get a Parameter : {}", id);
        return parameterRepository.findByIdAndEtlId(id, etlId);
    }

    @Override
    public Parameter findOneById(Long id) {
        LOGGER.debug("Request to get a Parameter : {}", id);
        return parameterRepository.findOneById(id);
    }


    private Parameter save(Parameter parameter) {
        LOGGER.debug("Request to save a Parameter : {}", parameter);
        return parameterRepository.saveAndFlush(parameter);
    }

    @Override
    public String decodeValueByTypology(Parameter parameter){
        if(Parameter.Typology.PASSWORD.equals(parameter.getTypology())){
            return SecurityUtils.passwordDecode(parameter.getValue(), aesProperties);
        }
        return parameter.getValue();
    }

    @Override
    public Page<Parameter> findAll(Pageable pageable) {
        LOGGER.debug("Request to find all Global Parameters");
        return parameterRepository.findAllByType(Type.GLOBAL, pageable);
    }

    private Parameter trimValuesParameter(Parameter parameter) {
        parameter.setValue(parameter.getValue().trim());
        parameter.setKey(parameter.getKey().trim());
        return parameter;
    }

}
