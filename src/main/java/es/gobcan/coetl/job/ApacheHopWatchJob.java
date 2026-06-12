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
public class ApacheHopWatchJob {
    private static final Logger LOG = LoggerFactory.getLogger(ApacheHopWatchJob.class);

    private final PlatformTransactionManager transactionManager;
    
    public ApacheHopWatchJob(PlatformTransactionManager transactionManager) {
    	this.transactionManager = transactionManager;
    }

    @Scheduled(cron = Constants.DEFAULT_PLATFORM_WATCH_CRON)
    public void run() {
        LOG.info("Init Apache Hop watch job");
        List<ExecutionInformation> finishedExecutions = transactionManager.updateRunningExecutions(TipoPlataformaEjecucion.APACHE_HOP);
        transactionManager.runExecuteRemoveEtl(TipoPlataformaEjecucion.APACHE_HOP, finishedExecutions);
        transactionManager.updateWaitingExecutions(TipoPlataformaEjecucion.APACHE_HOP);
        LOG.info("Finish Apache Hop watch job");
    }
}
