package es.gobcan.coetl.platform.common;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import es.gobcan.coetl.config.ApacheHopProperties;
import es.gobcan.coetl.config.PentahoProperties;
import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Execution;
import es.gobcan.coetl.domain.Execution.Result;
import es.gobcan.coetl.domain.enumeration.TipoPlataformaEjecucion;
import es.gobcan.coetl.platform.common.service.GitService;
import es.gobcan.coetl.platform.common.service.PlatformExecutionService;
import es.gobcan.coetl.platform.hop.enumeration.PipelineMethodsEnum;
import es.gobcan.coetl.platform.hop.enumeration.WorkflowMethodsEnum;
import es.gobcan.coetl.platform.hop.service.impl.HopExecutionServiceImpl;
import es.gobcan.coetl.platform.hop.service.util.HopUtil;
import es.gobcan.coetl.platform.hop.web.rest.dto.PipelineStatusDTO;
import es.gobcan.coetl.platform.hop.web.rest.dto.WorkflowStatusDTO;
import es.gobcan.coetl.platform.pentaho.enumeration.JobMethodsEnum;
import es.gobcan.coetl.platform.pentaho.enumeration.TransMethodsEnum;
import es.gobcan.coetl.platform.pentaho.service.impl.PentahoExecutionServiceImpl;
import es.gobcan.coetl.platform.pentaho.service.util.PentahoUtil;
import es.gobcan.coetl.platform.pentaho.web.rest.dto.JobStatusDTO;
import es.gobcan.coetl.platform.pentaho.web.rest.dto.TransStatusDTO;
import es.gobcan.coetl.platform.common.enumeration.Status;
import es.gobcan.coetl.platform.common.dto.CheckResultResponse;
import es.gobcan.coetl.platform.common.dto.EtlStatusDTO;

import es.gobcan.coetl.service.ExecutionService;
import es.gobcan.coetl.service.MailService;
import es.gobcan.coetl.service.UsuarioService;

@Component
public class PlatformTransactionManager {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformTransactionManager.class);

    private final ExecutionService executionService;

    private final PentahoExecutionServiceImpl pentahoExecutionService;
    
    private final HopExecutionServiceImpl hopExecutionService;

    private final GitService gitService;
    
    private final MailService mailService;
    
    private final UsuarioService usuarioService;
    
    private final PentahoProperties pentahoProperties;
    
    private final ApacheHopProperties hopProperties;
    
    public PlatformTransactionManager(PentahoProperties pentahoProperties, ApacheHopProperties hopProperties, ExecutionService executionService, PentahoExecutionServiceImpl pentahoExecutionService, HopExecutionServiceImpl hopExecutionService, GitService gitService, MailService mailService, UsuarioService usuarioService) {
        this.executionService = executionService;
        this.pentahoExecutionService = pentahoExecutionService;
        this.hopExecutionService = hopExecutionService;
        this.pentahoProperties = pentahoProperties;
        this.hopProperties = hopProperties;
        this.gitService = gitService;
        this.mailService = mailService;
        this.usuarioService = usuarioService;
    }
	
    @Transactional
    public List<ExecutionInformation> updateRunningExecutions(TipoPlataformaEjecucion platform) {
    	List<Execution> runningExecutions = executionService.getInRunningResultAndEtlExecutionPlatform(platform);
    	List<ExecutionInformation> finishedExecutions = new ArrayList<>();
        if (!CollectionUtils.isEmpty(runningExecutions)) {
            for (Execution runningExecution : runningExecutions) {
                Etl runningEtl = runningExecution.getEtl();
                LOG.info("Watching running ETL {}", runningEtl.getCode());
                final String etlFilename = gitService.getMainFileName(runningEtl);
                
                EtlStatusDTO etlStatusDTO = getExecutionStatus(platform, etlFilename, runningExecution, runningEtl);

                if (etlStatusDTO.isFinished()) {
                    LOG.info("ETL {} finished", runningEtl.getCode());
                    Execution finishedExecution = updateExecutionFromEtlStatus(runningExecution, etlStatusDTO);
                    executionService.update(finishedExecution);
                    finishedExecutions.add(new ExecutionInformation(runningEtl, etlFilename, runningExecution));
                    
                } else {
                    LOG.info("ETL {} not finished yet", runningEtl.getCode());
                }
            }
        }
        return finishedExecutions;
    }
    
    @Transactional
    public void updateWaitingExecutions(TipoPlataformaEjecucion platform) {
    	Execution nextExecution = executionService.getOldestInWaitingResultAndEtlExecutionPlatform(platform);
        if (nextExecution == null) {
            LOG.info("There is not ETL to execute.");
            return;
        }

        Etl nextEtl = nextExecution.getEtl();
        final String etlFilename = gitService.getMainFileName(nextEtl);
        CheckResultResponse webResultDTO = getPlatformExecutionService(platform).runEtl(nextEtl, etlFilename, nextExecution.getIdExecution());

        Execution nextExecutionResult;
        if (!webResultDTO.isOk()) {
            LOG.error("Error executing next ETL {} - cause: {}", nextEtl.getCode(), webResultDTO.getInfo());
            getPlatformExecutionService(platform).removeEtl(nextEtl, etlFilename, nextExecution.getIdExecution());
            nextExecution.setStartDate(Instant.now());
            nextExecutionResult = updateExecutionFromResult(nextExecution, Result.FAILED, webResultDTO.getInfo());
        } else {
            LOG.info("Executing next etl {}", nextEtl.getCode());
            nextExecutionResult = updateExecutionFromResult(nextExecution, Result.RUNNING);
        }
        executionService.update(nextExecutionResult);
    }

    @Transactional
    public void runExecuteRemoveEtl(TipoPlataformaEjecucion platform, List<ExecutionInformation> executionsToDelete) {
    	for (ExecutionInformation execInfo : executionsToDelete) {
    		try {
    			getPlatformExecutionService(platform).removeEtl(execInfo.getEtl(), execInfo.getEtlName(), execInfo.getExecution().getIdExecution());
            } catch (Exception e) {
                LOG.error("Se ha producido un error inesperado al tratar de eliminar el proceso de ejecución de la ETL ({}) en {}: {}", execInfo.getEtlName(), platform.name(), e.getMessage());
            }
    	}
    }
    
    private PlatformExecutionService getPlatformExecutionService(TipoPlataformaEjecucion platform) {
    	return TipoPlataformaEjecucion.PENTAHO.equals(platform) ? pentahoExecutionService : hopExecutionService;
    }
    
    private EtlStatusDTO getExecutionStatus(TipoPlataformaEjecucion platform, String etlFilename, Execution runningExecution, Etl runningEtl) {
    	if (TipoPlataformaEjecucion.PENTAHO.equals(platform)) {
    		if (runningEtl.isTransformation()) {
                return runExecuteStatusTrans(etlFilename, runningExecution, runningEtl);
            } else {
                return runExecuteStatusJob(etlFilename, runningExecution, runningEtl);
            }
    	} else {
    		if (runningEtl.isPipeline()) {
                return runExecuteStatusPipeline(etlFilename, runningExecution, runningEtl);
            } else {
                return runExecuteStatusWorkflow(etlFilename, runningExecution, runningEtl);
            }
    	}
    }

    private EtlStatusDTO runExecuteStatusTrans(String etlFilename, Execution runningExecution, Etl runningEtl) {
        EtlStatusDTO etlStatusDTO;
        try {
            etlStatusDTO = executeStatusTrans(etlFilename, runningExecution.getIdExecution());
        } catch (Exception e) {
            LOG.error("Se ha producido un error inesperado durante la comprobación del estado de la transformación de la ETL ({}): {}", runningEtl.getName(), e.getMessage());
            etlStatusDTO = new TransStatusDTO();
            etlStatusDTO.setErrorDescription("Se ha producido un error inesperado al comprobar el estado de ejecución de la transformación");
            etlStatusDTO.setStatus(Status.FINISHED_WITH_ERRORS);
        }
        return etlStatusDTO;
    }

    private EtlStatusDTO executeStatusTrans(String etlFilename, String idExecution) {
        final MultiValueMap<String, String> queryParams = getQueryParams(etlFilename, idExecution);
        return PentahoUtil.execute(PentahoUtil.getUser(pentahoProperties), PentahoUtil.getPassword(pentahoProperties), PentahoUtil.getUrl(pentahoProperties), 
        		TransMethodsEnum.STATUS, HttpMethod.GET, null, queryParams, TransStatusDTO.class).getBody();
    }

    private EtlStatusDTO runExecuteStatusJob(String etlFilename, Execution runningExecution, Etl runningEtl) {
        EtlStatusDTO etlStatusDTO;
        try {
            etlStatusDTO = executeStatusJob(etlFilename, runningExecution.getIdExecution());
        } catch (Exception e) {
            LOG.error("Se ha producido un error inesperado durante la comprobación del estado del job para la ETL ({}): {}", runningEtl.getName(), e.getMessage());
            etlStatusDTO = new JobStatusDTO();
            etlStatusDTO.setErrorDescription("Se ha producido un error inesperado al comprobar el estado de ejecución del job");
            etlStatusDTO.setStatus(Status.FINISHED_WITH_ERRORS);
        }
        return etlStatusDTO;
    }

    private EtlStatusDTO executeStatusJob(String etlFilename, String idExecution) {
        final MultiValueMap<String, String> queryParams = getQueryParams(etlFilename, idExecution);
        return PentahoUtil.execute(PentahoUtil.getUser(pentahoProperties), PentahoUtil.getPassword(pentahoProperties), PentahoUtil.getUrl(pentahoProperties), 
        		JobMethodsEnum.STATUS, HttpMethod.GET, null, queryParams, JobStatusDTO.class).getBody();
    }

    private Execution updateExecutionFromEtlStatus(Execution currentExecution, EtlStatusDTO etlStatusDTO) {
        if (etlStatusDTO.isFinishedWithErrors() || etlStatusDTO.isStoppedWithErrors() || etlStatusDTO.isStopped()) {
        	mailService.sendEmailErrorETL(usuarioService.getNotRepeatEmailsUsuarioAdmin(), currentExecution.getEtl(), String.format("La ETL con código '%s' ha fallado en su ejecución", currentExecution.getEtl().getCode()));
            return updateExecutionFromResult(currentExecution, Result.FAILED, etlStatusDTO.getErrorDescription());
        }
        return updateExecutionFromResult(currentExecution, Result.SUCCESS);
    }

    private Execution updateExecutionFromResult(Execution currentExecution, Result result, String notes) {
        if (Result.RUNNING.equals(result)) {
            currentExecution.setStartDate(Instant.now());
        }
        if (Result.FAILED.equals(result) || Result.SUCCESS.equals(result)) {
            currentExecution.setFinishDate(Instant.now());
        }
        currentExecution.setResult(result);
        currentExecution.setNotes(notes);
        return currentExecution;
    }

    private Execution updateExecutionFromResult(Execution currentExecution, Result result) {
        return updateExecutionFromResult(currentExecution, result, null);
    }
    
    private EtlStatusDTO runExecuteStatusPipeline(String etlFilename, Execution runningExecution, Etl runningEtl) {
        EtlStatusDTO etlStatusDTO;
        try {
            etlStatusDTO = executeStatusPipeline(etlFilename, runningExecution.getIdExecution());
        } catch (Exception e) {
            LOG.error("An unexpected error occurred checking pipeline execution ({}): {}", runningEtl.getName(), e.getMessage());
            etlStatusDTO = new PipelineStatusDTO();
            etlStatusDTO.setErrorDescription(e.getMessage());
            etlStatusDTO.setStatus(Status.FINISHED_WITH_ERRORS);
        }
        return etlStatusDTO;
    }

    private EtlStatusDTO executeStatusPipeline(String etlFilename, String idExecution) {
        final MultiValueMap<String, String> queryParams = getQueryParams(etlFilename, idExecution);
        return HopUtil.execute(HopUtil.getUser(hopProperties), HopUtil.getPassword(hopProperties), HopUtil.getUrl(hopProperties), 
        		PipelineMethodsEnum.STATUS, HttpMethod.GET, null, queryParams, PipelineStatusDTO.class).getBody();
    }
    
    private EtlStatusDTO runExecuteStatusWorkflow(String etlFilename, Execution runningExecution, Etl runningEtl) {
        EtlStatusDTO etlStatusDTO;
        try {
            etlStatusDTO = executeStatusWorkflow(etlFilename, runningExecution.getIdExecution());
        } catch (Exception e) {
            LOG.error("An unexpected error occurred checking workflow execution ({}): {}", runningEtl.getName(), e.getMessage());
            etlStatusDTO = new WorkflowStatusDTO();
            etlStatusDTO.setErrorDescription(e.getMessage());
            etlStatusDTO.setStatus(Status.FINISHED_WITH_ERRORS);
        }
        return etlStatusDTO;
    }

    private EtlStatusDTO executeStatusWorkflow(String etlFilename, String idExecution) {
        final MultiValueMap<String, String> queryParams = getQueryParams(etlFilename, idExecution);
        return HopUtil.execute(HopUtil.getUser(hopProperties), HopUtil.getPassword(hopProperties), HopUtil.getUrl(hopProperties), 
        		WorkflowMethodsEnum.STATUS, HttpMethod.GET, null, queryParams, WorkflowStatusDTO.class).getBody();
    }
    
    private MultiValueMap<String, String> getQueryParams(String etlFilename, String idExecution) {
    	MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("xml", "y");
        queryParams.add("name", etlFilename);
        queryParams.add("id", idExecution);
        return queryParams;
    }
    
    
    public class ExecutionInformation {
    	final Etl etl;
    	final String etlName;
    	final Execution execution;
    	
		public ExecutionInformation(Etl etl, String etlName, Execution execution) {
			super();
			this.etl = etl;
			this.etlName = etlName;
			this.execution = execution;
		}

		public Etl getEtl() {
			return etl;
		}

		public String getEtlName() {
			return etlName;
		}

		public Execution getExecution() {
			return execution;
		}
    }
}
