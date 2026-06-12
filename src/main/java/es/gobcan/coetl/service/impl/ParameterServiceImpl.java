package es.gobcan.coetl.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.gobcan.coetl.config.AESProperties;
import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.File;
import es.gobcan.coetl.domain.Parameter;
import es.gobcan.coetl.domain.Parameter.Type;
import es.gobcan.coetl.domain.Parameter.Typology;
import es.gobcan.coetl.domain.enumeration.TipoPlataformaEjecucion;
import es.gobcan.coetl.repository.EtlRepository;
import es.gobcan.coetl.repository.ParameterRepository;
import es.gobcan.coetl.security.SecurityUtils;
import es.gobcan.coetl.service.FileService;
import es.gobcan.coetl.service.ParameterService;
import es.gobcan.coetl.service.validator.ParameterValidator;
import es.gobcan.coetl.web.rest.mapper.ParameterMapper;

@Service
public class ParameterServiceImpl implements ParameterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterService.class);
    private static final String ETL_RESOURCES = "ETL_RESOURCES";
    
    @Autowired
    private ParameterRepository parameterRepository;

    @Autowired
    private EtlRepository etlRepository;

    @Autowired
    private ParameterValidator parameterValidator;
    
    @Autowired 
    private ParameterMapper parameterMapper;
    
    @Autowired
    private FileService fileService;

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
        LOGGER.debug("Request to get a Parameter by id: {} and etlId: {}", id, etlId);
        parameterRepository.flush();
        return parameterRepository.findByIdAndEtlId(id, etlId);
    }
    
    
    public Parameter findOneByKeyAndEtlId(String key, Long etlId) {
        LOGGER.debug("Request to get one Parameter by key: {} and etlId: {}", key, etlId);
        return parameterRepository.findOneByKeyAndEtlId(key, etlId);
    }

    @Override
    public Parameter findOneById(Long id) {
        LOGGER.debug("Request to get a Parameter by id : {}", id);
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
    
    
    @Override
    public Parameter getOneById(Long paramId) {
        LOGGER.debug("Request to find a single Parameter");
        return parameterRepository.getOne(paramId);
    }

    private Parameter trimValuesParameter(Parameter parameter) {
        parameter.setValue(parameter.getValue().trim());
        parameter.setKey(parameter.getKey().trim());
        return parameter;
    }
    
    public Parameter copyParameter(Parameter originalParameter) {
    	Parameter newParam = new Parameter();
    	newParam.setDescription(originalParameter.getDescription());
    	newParam.setEtl(originalParameter.getEtl());
    	newParam.setFile(originalParameter.getFile());
    	newParam.setId(originalParameter.getId());
    	newParam.setKey(originalParameter.getKey());
    	newParam.setOptLock(originalParameter.getOptLock());
    	newParam.setType(originalParameter.getType());
    	newParam.setTypology(originalParameter.getTypology());
    	newParam.setValue(originalParameter.getValue());
    	
    	return newParam;
    }
    
	@Override
	public Long storeFile(MultipartFile file, Long idEtl) {
		parameterValidator.checkIfFileAlreadyExists(file.getOriginalFilename(), idEtl);
        TipoPlataformaEjecucion tipoPlataformaEjecucion = etlRepository.findOne(idEtl).getExecutionPlatform();
		Parameter parameter = findOneByKeyAndEtlId(ETL_RESOURCES, idEtl);

		if (parameter != null) {
			String etlResourcesPath = parameterMapper.toDto(parameter).getValue();
			File savedFile = fileService.saveDatabase(file);
			Path dir = Paths.get(etlResourcesPath);
			fileService.uploadRepository(dir, file, tipoPlataformaEjecucion);
			return savedFile.getId();
		}
		return null;
	}

	private void changeTypologyFromFileToOther(Parameter currentParameter, Long fileIdNum, Path repositoryPath,
			String originalFilename) {
		currentParameter.setFile(null);
        TipoPlataformaEjecucion tipoPlataformaEjecucion = etlRepository.findOne(currentParameter.getEtl().getId()).getExecutionPlatform();
		fileService.deleteDatabase(fileIdNum);
		fileService.deleteRepository(repositoryPath, originalFilename, tipoPlataformaEjecucion);
	}

	private void changeTypologyToFile(Parameter currentParameter, Path repositoryPath, MultipartFile file, Long idEtl) {
		// If filename changes, check if there is no other file with that name.
		parameterValidator.checkIfFileAlreadyExists(file.getOriginalFilename(), idEtl);
        TipoPlataformaEjecucion tipoPlataformaEjecucion = etlRepository.findOne(idEtl).getExecutionPlatform();
		File fichero = fileService.saveDatabase(file);
		Long fileId = fichero.getId();
		currentParameter.setFile(fileId);
		fileService.uploadRepository(repositoryPath, file, tipoPlataformaEjecucion);
	}

	private void sameTypologyIsFile(Long fileIdNum, Path repositoryPath, MultipartFile file, String originalFilename, Long idEtl) {
		if (!originalFilename.equals(file.getOriginalFilename())) {
	    	// If filename changes, check if there is no other file with that name.
	    	parameterValidator.checkIfFileAlreadyExists(file.getOriginalFilename(), idEtl);
	    }
        TipoPlataformaEjecucion tipoPlataformaEjecucion = etlRepository.findOne(idEtl).getExecutionPlatform();
		fileService.updateRepository(repositoryPath, file, originalFilename, tipoPlataformaEjecucion);
		fileService.updateDatabase(file, fileIdNum);
	}
	
	private void differentTypologies(Typology originalTypology, Typology newTypology, Parameter currentParameter, Long fileIdNum, Path repositoryPath, MultipartFile file, String originalFilename, Long idEtl) {
		if(originalTypology == Typology.FILE) {
			// Change from "FILE" to other typology -> Update param and delete file from database and repository
			changeTypologyFromFileToOther(currentParameter, fileIdNum, repositoryPath, originalFilename);
    	}
    	if(newTypology == Typology.FILE) {
    		changeTypologyToFile(currentParameter, repositoryPath, file, idEtl);
    	}
	}
	
	private void checkTypologies(Typology originalTypology, Typology newTypology, Parameter currentParameter, Long fileIdNum, Path repositoryPath, MultipartFile file, String originalFilename, Long idEtl){
	     if(originalTypology != newTypology) {
	    	 differentTypologies(originalTypology, newTypology, currentParameter, fileIdNum, repositoryPath, file, originalFilename, idEtl);
	     } else {
	    	 if(originalTypology == Typology.FILE) {
	    		 // Update an existing file
	    		 sameTypologyIsFile(fileIdNum, repositoryPath, file, originalFilename, idEtl);	
	    	 }
	     }
	}

	@Override
	public void updateFile(MultipartFile file, Parameter originalParameter, Parameter currentParameter, Long idEtl) {	 
	    String originalFilename = originalParameter.getValue();
		Typology originalTypology = originalParameter.getTypology();
	    Parameter etlPathParameter = findOneByKeyAndEtlId(ETL_RESOURCES, idEtl);
	    Long fileIdNum = originalParameter.getFile();
        Typology newTypology = currentParameter.getTypology();
        Path repositoryPath = etlPathParameter.getValue() == null ? null : Paths.get(etlPathParameter.getValue());
        checkTypologies(originalTypology, newTypology, currentParameter, fileIdNum, repositoryPath, file, originalFilename, idEtl);
	}


	@Override
	public void deleteFile(Parameter parameter, Long idEtl) {
		// Delete file from repository folder and database
	    TipoPlataformaEjecucion tipoPlataformaEjecucion = etlRepository.findOne(idEtl).getExecutionPlatform();
	    fileService.deleteDatabase(parameter.getFile());
    	Parameter etlPathParam = findOneByKeyAndEtlId(ETL_RESOURCES, idEtl);
    	Path dir = Paths.get(etlPathParam.getValue());
    	fileService.deleteRepository(dir, parameter.getValue(), tipoPlataformaEjecucion);
    	
	}
}
