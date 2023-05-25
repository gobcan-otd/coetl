package es.gobcan.coetl.job;

import java.time.Instant;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import es.gobcan.coetl.config.Constants;
import es.gobcan.coetl.config.PentahoProperties;
import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Execution;
import es.gobcan.coetl.domain.Execution.Result;
import es.gobcan.coetl.pentaho.enumeration.JobMethodsEnum;
import es.gobcan.coetl.pentaho.enumeration.Status;
import es.gobcan.coetl.pentaho.enumeration.TransMethodsEnum;
import es.gobcan.coetl.pentaho.service.PentahoExecutionService;
import es.gobcan.coetl.pentaho.service.PentahoGitService;
import es.gobcan.coetl.pentaho.service.util.PentahoUtil;
import es.gobcan.coetl.pentaho.web.rest.dto.EtlStatusDTO;
import es.gobcan.coetl.pentaho.web.rest.dto.JobStatusDTO;
import es.gobcan.coetl.pentaho.web.rest.dto.TransStatusDTO;
import es.gobcan.coetl.pentaho.web.rest.dto.WebResultDTO;
import es.gobcan.coetl.service.ExecutionService;
import es.gobcan.coetl.service.MailService;
import es.gobcan.coetl.service.UsuarioService;

@Component
public class PentahoWatchJob {

    private static final Logger LOG = LoggerFactory.getLogger(PentahoWatchJob.class);

    private final ExecutionService executionService;

    private final PentahoExecutionService pentahoExecutionService;

    private final PentahoGitService pentahoGitService;
    
    private final MailService mailService;
    
    private final UsuarioService usuarioService;

    private final String url;
    private final String user;
    private final String password;

    public PentahoWatchJob(PentahoProperties pentahoProperties, ExecutionService executionService, PentahoExecutionService pentahoExecutionService, PentahoGitService pentahoGitService, MailService mailService, UsuarioService usuarioService) {
        this.executionService = executionService;
        this.pentahoExecutionService = pentahoExecutionService;
        this.url = PentahoUtil.getUrl(pentahoProperties);
        this.user = PentahoUtil.getUser(pentahoProperties);
        this.password = PentahoUtil.getPassword(pentahoProperties);
        this.pentahoGitService = pentahoGitService;
        this.mailService = mailService;
        this.usuarioService = usuarioService;
    }

    @Scheduled(cron = Constants.DEFAULT_PENTAHO_WATCH_CRON)
    @Transactional
    public void run() {
        LOG.info("Init Pentaho watch job");
        List<Execution> runningExecutions = executionService.getInRunningResult();

        if (!CollectionUtils.isEmpty(runningExecutions)) {
            for (Execution runningExecution : runningExecutions) {
                Etl runningEtl = runningExecution.getEtl();
                LOG.info("Watching running ETL {}", runningEtl.getCode());
                final String etlFilename = pentahoGitService.getMainFileName(runningEtl);
                EtlStatusDTO etlStatusDTO;

                if (runningEtl.isTransformation()) {
                    etlStatusDTO = runExecuteStatusTrans(etlFilename, runningExecution, runningEtl);
                } else {
                    etlStatusDTO = runExecuteStatusJob(etlFilename, runningExecution, runningEtl);
                }

                if (etlStatusDTO.isFinished()) {
                    LOG.info("ETL {} finished", runningEtl.getCode());
                    Execution finishedExecution = updateExecutionFromEtlStatus(runningExecution, etlStatusDTO);
                    executionService.update(finishedExecution);
                    runExecuteRemoveEtl(runningEtl, etlFilename, runningExecution);
                } else {
                    LOG.info("ETL {} not finished yet", runningEtl.getCode());
                }
            }
        }

        Execution nextExecution = executionService.getOldestInWaitingResult();
        if (nextExecution == null) {
            LOG.info("There is not ETL to execute.");
            return;
        }

        Etl nextEtl = nextExecution.getEtl();
        final String etlFilename = pentahoGitService.getMainFileName(nextEtl);
        WebResultDTO webResultDTO = pentahoExecutionService.runEtl(nextEtl, etlFilename, nextExecution.getIdExecution());

        Execution nextExecutionResult;
        if (!webResultDTO.isOk()) {
            LOG.error("Error executing next ETL {} - cause: {}", nextEtl.getCode(), webResultDTO.getMessage());
            pentahoExecutionService.removeEtl(nextEtl, etlFilename, nextExecution.getIdExecution());
            nextExecution.setStartDate(Instant.now());
            nextExecutionResult = updateExecutionFromResult(nextExecution, Result.FAILED, webResultDTO.getMessage());
        } else {
            LOG.info("Executing next etl {}", nextEtl.getCode());
            nextExecutionResult = updateExecutionFromResult(nextExecution, Result.RUNNING);
        }
        executionService.update(nextExecutionResult);
    }

    private void runExecuteRemoveEtl(Etl runningEtl, String etlFilename, Execution runningExecution) {
        try {
            pentahoExecutionService.removeEtl(runningEtl, etlFilename, runningExecution.getIdExecution());
        } catch (Exception e) {
            LOG.error("Se ha producido un error inesperado al tratar de eliminar el proceso de ejecución de la ETL ({}) en pentaho: {}", runningEtl.getName(), e.getMessage());
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
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("xml", "y");
        queryParams.add("name", etlFilename);
        queryParams.add("id", idExecution);
        return PentahoUtil.execute(user, password, url, TransMethodsEnum.STATUS, HttpMethod.GET, null, queryParams, TransStatusDTO.class).getBody();
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
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("xml", "y");
        queryParams.add("name", etlFilename);
        queryParams.add("id", idExecution);
        return PentahoUtil.execute(user, password, url, JobMethodsEnum.STATUS, HttpMethod.GET, null, queryParams, JobStatusDTO.class).getBody();
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
}
