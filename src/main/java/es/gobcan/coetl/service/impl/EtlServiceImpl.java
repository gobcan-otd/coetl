package es.gobcan.coetl.service.impl;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import es.gobcan.coetl.config.QuartzConstants;
import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Execution;
import es.gobcan.coetl.domain.Execution.Type;
import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.domain.enumeration.TipoPlataformaEjecucion;
import es.gobcan.coetl.errors.CustomParameterizedExceptionBuilder;
import es.gobcan.coetl.errors.ErrorConstants;
import es.gobcan.coetl.errors.util.CustomExceptionUtil;
import es.gobcan.coetl.job.PlatformExecutionJob;
import es.gobcan.coetl.platform.hop.service.impl.HopExecutionServiceImpl;
import es.gobcan.coetl.platform.pentaho.service.impl.PentahoExecutionServiceImpl;
import es.gobcan.coetl.repository.EtlRepository;
import es.gobcan.coetl.repository.UsuarioRepository;
import es.gobcan.coetl.security.SecurityChecker;
import es.gobcan.coetl.security.SecurityUtils;
import es.gobcan.coetl.service.EtlService;
import es.gobcan.coetl.service.ExecutionService;
import es.gobcan.coetl.service.validator.EtlValidator;
import es.gobcan.coetl.util.CronUtils;
import es.gobcan.coetl.web.rest.dto.EtlDTO;
import es.gobcan.coetl.web.rest.util.QueryUtil;

@Service
public class EtlServiceImpl implements EtlService {

    private static final Logger LOG = LoggerFactory.getLogger(EtlService.class);
    private static final String IDENTITY_JOB_PENTAHO_PREFIX = "pentahoExecutionJob_";
    private static final String IDENTITY_TRIGGER_PENTAHO_PREFIX = "pentahoExectionTrigger_";
    private static final String IDENTITY_JOB_HOP_PREFIX = "hopExecutionJob_";
    private static final String IDENTITY_TRIGGER_HOP_PREFIX = "hopExecutionTrigger_";
    private static final String AND = " AND ";

    @Autowired
    private EtlRepository etlRepository;

    @Autowired
    private EtlValidator etlValidator;

    @Autowired
    private QueryUtil queryUtil;

    @Autowired
    private ExecutionService executionService;

    @Autowired
    private PentahoExecutionServiceImpl pentahoExecutionService;

    @Autowired
    private SchedulerFactoryBean schedulerAccessorBean;
    
    @Autowired
    private SecurityChecker secCheck;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HopExecutionServiceImpl hopExecutionService;

    @Override
    public Etl create(Etl etl) {
        LOG.debug("Request to create an ETL : {}", etl);
        this.trimCodeEtl(etl);
        etlValidator.validate(etl);
        return (etl.isPlanned()) ? planifyAndSave(etl) : save(etl);
    }

    @Override
    public Etl update(Etl etl) {
        LOG.debug("Request to update an ETL : {}", etl);
        etlValidator.validate(etl);
        return (etl.isPlanned()) ? planifyAndSave(etl) : unplanifyAndSave(etl);
    }

    @Override
    public Etl delete(Etl etl) {
        LOG.debug("Request to delete an ETL : {}", etl);
        etl.setDeletedBy(SecurityUtils.getCurrentUserLogin());
        etl.setDeletionDate(Instant.now());

        return (etl.isPlanned()) ? unplanifyAndSave(etl) : save(etl);
    }

    @Override
    public Etl restore(Etl etl) {
        LOG.debug("Request to recover an ETL : {}", etl);
        etl.setDeletedBy(null);
        etl.setDeletionDate(null);
        return (etl.isPlanned()) ? planifyAndSave(etl) : save(etl);
    }

    @Override
    public Etl findOne(Long id) {
        LOG.debug("Request to find an ETL : {}", id);
        return etlRepository.findOne(id);
    }

    @Override
    public Page<Etl> findAll(String query, boolean includeDeleted, Pageable pageable, List<Long> organismosId, String lastExecutionStartDate, String lastExecutionResult,
            String executionPlatform) {
        LOG.debug("Request to find all ETLs by query : {}", query);
        DetachedCriteria criteria = buildEtlCriteria(query, includeDeleted, pageable, organismosId, lastExecutionStartDate, lastExecutionResult, executionPlatform);
        return etlRepository.findAll(criteria, pageable);
    }

    @Override
    public List<Etl> findAllByOrganismIdIn(List<Long> organismosId) {
        LOG.debug("Request to find all ETLs by organism list : {}", organismosId);
        return etlRepository.findByOrganizationInChargeIdInAndDeletionDateIsNullAndVisibilityOrderByName(organismosId, false);
    }

    @Override
    public void execute(Etl etl) {
        LOG.debug("Request to execute ETL : {}", etl);
        Execution resultExecution = null;

        switch (etl.getExecutionPlatform()) {
            case PENTAHO: {
                resultExecution = pentahoExecutionService.execute(etl, Type.MANUAL, SecurityContextHolder.getContext().getAuthentication().getName());
                break;
            }
            case APACHE_HOP: {
                resultExecution = hopExecutionService.execute(etl, Type.MANUAL, SecurityContextHolder.getContext().getAuthentication().getName());
                break;
            }
            default: {
                throw new RuntimeException("Execution platform not defined");
            }
        }
        executionService.create(resultExecution);

    }

    @Override
    public boolean goingToChangeRepository(EtlDTO etlDto) {
        LOG.debug("Request to check if its going to change repository from DTO: {}", etlDto);
        if (etlDto.getId() == null) {
            return false;
        }
        Etl etl = etlRepository.findOne(etlDto.getId());
        if (etl.getUriRepository().equals(etlDto.getUriRepository())) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean goingToChangePlatform(EtlDTO etlDto) {
        LOG.debug("Request to check if its going to change platform from DTO: {}", etlDto);
        if (etlDto.getId() == null) {
        	return false;
        }
        Etl etl = etlRepository.findOne(etlDto.getId());
        return !etl.getExecutionPlatform().equals(etlDto.getExecutionPlatform());
    }
    
    private Etl planifyAndSave(Etl etl) {
        LOG.debug("Request to planify and save an ETL : {}", etl);
        JobKey jobKey = etl.getExecutionPlatform() == TipoPlataformaEjecucion.PENTAHO ? new JobKey(IDENTITY_JOB_PENTAHO_PREFIX + etl.getCode()) : 
            new JobKey(IDENTITY_JOB_HOP_PREFIX + etl.getCode());
        final String executionPlanning = etl.getExecutionPlanning();

        CronExpression cronExpression = buildCronExpression(executionPlanning);
        Instant nextExecution = CronUtils.getNextExecutionFromCronExpression(cronExpression);
        etl.setNextExecution(nextExecution);
        schedulePlatformExecutionJob(jobKey, cronExpression, etl);

        return save(etl);
    }

    private CronExpression buildCronExpression(final String executionPlanning) {
        try {
            return new CronExpression(executionPlanning);
        } catch (ParseException e) {
            final String message = String.format("The cron expression %s is not valid", executionPlanning);
            final String code = ErrorConstants.ETL_CRON_EXPRESSION_NOT_VALID;
            throw new CustomParameterizedExceptionBuilder().message(message).code(code, executionPlanning).build();
        }
    }

    private Etl unplanifyAndSave(Etl etl) {
        LOG.debug("Request to unplanify and save an ETL : {}", etl);
        JobKey jobKey = etl.getExecutionPlatform() == TipoPlataformaEjecucion.PENTAHO ? new JobKey(IDENTITY_JOB_PENTAHO_PREFIX + etl.getCode()) : 
            new JobKey(IDENTITY_JOB_HOP_PREFIX + etl.getCode());
        unschedulePentahoExecutionJob(jobKey);
        etl.setNextExecution(null);
        return save(etl);
    }

    @Transactional
    private Etl save(Etl etl) {
        LOG.debug("Request to save an ETL : {}", etl);
        return etlRepository.saveAndFlush(etl);
    }
    
    private Etl trimCodeEtl(Etl etl) {
    	etl.setCode(etl.getCode().trim());
        return etl;
    }

    private void schedulePlatformExecutionJob(JobKey jobKey, CronExpression cronExpression, Etl etl) {
        LOG.debug("Request to scheduled a new Quartz job : {}", jobKey.getName());
        //@formatter:off
        JobDetail job = newJob(PlatformExecutionJob.class)
                .withIdentity(jobKey)
                .usingJobData(QuartzConstants.ETL_CODE_JOB_DATA, etl.getCode())
                .build();

        CronTrigger trigger = newTrigger()
                .withIdentity(etl.getExecutionPlatform() == TipoPlataformaEjecucion.PENTAHO ? IDENTITY_TRIGGER_PENTAHO_PREFIX + etl.getCode() : 
                    IDENTITY_TRIGGER_HOP_PREFIX + etl.getCode())
                .withSchedule(cronSchedule(cronExpression))
                .build();
        //@formatter:on

        try {
            deleteExistingJob(jobKey);
            schedulerAccessorBean.getScheduler().scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            final String message = String.format("Error during scheduling a new job %s", jobKey.getName());
            final String code = ErrorConstants.ETL_SCHEDULE_ERROR;
            CustomExceptionUtil.throwCustomParameterizedException(message, e, code);
        }
    }

    private void unschedulePentahoExecutionJob(JobKey jobKey) {
        LOG.debug("Request to unscheduled (if exists) a Quartz job : {}", jobKey.getName());

        try {
            deleteExistingJob(jobKey);
        } catch (SchedulerException e) {
            final String message = String.format("Error during unscheduling the job %s", jobKey.getName());
            final String code = ErrorConstants.ETL_UNSCHEDULE_ERROR;
            CustomExceptionUtil.throwCustomParameterizedException(message, e, code);
        }
    }

    private void deleteExistingJob(JobKey jobKey) throws SchedulerException {
        if (schedulerAccessorBean.getScheduler().checkExists(jobKey)) {
            schedulerAccessorBean.getScheduler().deleteJob(jobKey);
        }
    }

    private DetachedCriteria buildEtlCriteria(String query, boolean includeDeleted, Pageable pageable, List<Long> organismosId, String lastExecutionStartDate,
            String lastExecutionResult, String executionPlatform) {
        StringBuilder queryBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(query)) {
            queryBuilder.append(query);
        }
        if (organismosId != null && !organismosId.isEmpty()) {
            queryBuilder = getQueryByOrganismos(organismosId, queryBuilder);
        }

        String login = SecurityUtils.getCurrentUserLogin();
        Optional<Usuario> user = usuarioRepository.findOneByLogin(login);
        if (user.isPresent() && !user.get().getIsAdmin() && !user.get().getAllEtlAccess()) {
            queryBuilder = getQueryByEtlsUsuario(user.get().getEtls(), queryBuilder);
        }

        queryBuilder.append(queryUtil.getQueryByLastExecution(lastExecutionStartDate, lastExecutionResult, queryBuilder));
        queryBuilder.append(queryUtil.getQueryByExecutionPlatform(executionPlatform, queryBuilder));
        String finalQuery = getFinalQuery(includeDeleted, queryBuilder);
        return queryUtil.queryToEtlCriteria(pageable, finalQuery);
    }

    private String getFinalQuery(boolean includeDeleted, StringBuilder queryBuilder) {
        String finalQuery = queryBuilder.toString();
        if (BooleanUtils.isTrue(includeDeleted)) {
            finalQuery = queryUtil.queryIncludingDeleted(finalQuery);
        }
        return finalQuery;
    }

    private StringBuilder getQueryByOrganismos(List<Long> organismosId, StringBuilder queryBuilder) {
        StringBuilder query = new StringBuilder();
        if (!secCheck.canSeeAllEtls(SecurityContextHolder.getContext().getAuthentication())) {
            if (StringUtils.isNotBlank(queryBuilder)) {
                queryBuilder.append(AND);
            }
            query.append(queryUtil.queryIncludingIdOrganismo(queryBuilder.toString(), organismosId));
        }
        return query;
    }

    private StringBuilder getQueryByEtlsUsuario(List<Etl> etls, StringBuilder queryBuilder) {
        StringBuilder query = new StringBuilder();
        List<Long> etlsId = etls.stream().map(etl -> etl.getId()).collect(Collectors.toList());
        if (StringUtils.isNotBlank(queryBuilder)) {
            queryBuilder.append(AND);
        }
        query.append(queryUtil.queryIncludingIdEtl(queryBuilder.toString(), etlsId));
        return query;
    }

}
