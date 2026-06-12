package es.gobcan.coetl.job;

import java.time.Instant;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import es.gobcan.coetl.config.Constants;
import es.gobcan.coetl.config.QuartzConstants;
import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Execution;
import es.gobcan.coetl.domain.Execution.Type;
import es.gobcan.coetl.domain.enumeration.TipoPlataformaEjecucion;
import es.gobcan.coetl.errors.ErrorConstants;
import es.gobcan.coetl.errors.util.CustomExceptionUtil;
import es.gobcan.coetl.util.CronUtils;

@Component
public class PlatformExecutionJob extends AbstractCoetlQuartzJob {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformExecutionJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        LOG.info("Job Pentaho Execution running");
        executePentahoService(context);
    }

    private void executePentahoService(JobExecutionContext context) {
            String etlCode = (String) context.getJobDetail().getJobDataMap().get(QuartzConstants.ETL_CODE_JOB_DATA);
            PlatformTransactionManager platformTransactionManager = getPlatformTransactionManager(context);
            TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);

        try {
            transactionTemplate.execute(status -> {
                Etl currentEtl = getEtlRepository(context).findOneByCode(etlCode);
                Instant nextExecution = CronUtils.getNextExecutionFromJobContext(context);
                currentEtl.setNextExecution(nextExecution);
                getEtlRepository(context).save(currentEtl);
                Execution resultExecution = null;
                if (TipoPlataformaEjecucion.PENTAHO.equals(currentEtl.getExecutionPlatform())) {
                    resultExecution = getPentahoExecutionService(context).execute(currentEtl, Type.AUTO, Constants.CRON_EXECUTOR_USER);
                } else if (TipoPlataformaEjecucion.APACHE_HOP.equals(currentEtl.getExecutionPlatform())) {
                    resultExecution = getHopExecutionService(context).execute(currentEtl, Type.AUTO, Constants.CRON_EXECUTOR_USER);
                }
                getExecutionService(context).create(resultExecution);
                return true;
            });
        } catch(Exception e) {
            Etl currentEtl = getEtlRepository(context).findOneByCode(etlCode);
            final String message = String.format("Error occurred during the execution. ETL %s can not be executed", etlCode);
            final String code = ErrorConstants.ETL_EXECUTE_ERROR;
            if (TipoPlataformaEjecucion.PENTAHO.equals(currentEtl.getExecutionPlatform())) {
                getPentahoExecutionService(context).notifyExecutionError(currentEtl, message);
            } else if (TipoPlataformaEjecucion.APACHE_HOP.equals(currentEtl.getExecutionPlatform())) {
                getHopExecutionService(context).notifyExecutionError(currentEtl, message);
            }
            CustomExceptionUtil.throwCustomParameterizedException(message, code);
        }
    }

}
