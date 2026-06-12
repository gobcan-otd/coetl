package es.gobcan.coetl.job;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.gobcan.coetl.config.Constants;
import es.gobcan.coetl.domain.enumeration.TipoPlataformaEjecucion;
import es.gobcan.coetl.platform.common.PlatformTransactionManager;
import es.gobcan.coetl.platform.common.PlatformTransactionManager.ExecutionInformation;

@Component
public class PentahoWatchJob {

    private static final Logger LOG = LoggerFactory.getLogger(PentahoWatchJob.class);

    private final PlatformTransactionManager transactionManager;
    
    public PentahoWatchJob(PlatformTransactionManager transactionManager) {
    	this.transactionManager = transactionManager;
    }

    @Scheduled(cron = Constants.DEFAULT_PLATFORM_WATCH_CRON)
    public void run() {
        LOG.info("Init Pentaho watch job");
        List<ExecutionInformation> finishedExecutions = transactionManager.updateRunningExecutions(TipoPlataformaEjecucion.PENTAHO);
        transactionManager.runExecuteRemoveEtl(TipoPlataformaEjecucion.PENTAHO, finishedExecutions);
        transactionManager.updateWaitingExecutions(TipoPlataformaEjecucion.PENTAHO);
        LOG.info("Finish Pentaho watch job");
    }
    
}
