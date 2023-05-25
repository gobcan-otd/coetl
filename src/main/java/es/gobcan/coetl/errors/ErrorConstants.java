package es.gobcan.coetl.errors;

public final class ErrorConstants {

    public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    public static final String ERR_ACCESS_DENIED = "error.accessDenied";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String ERR_METHOD_NOT_SUPPORTED = "error.methodNotSupported";
    public static final String ERR_INTERNAL_SERVER_ERROR = "error.internalServerError";
    public static final String ERR_FIELD_VALUE = "error.field.value";
    public static final String ERR_FIELD_VALIDATION = "error.field.validation";
    public static final String ERR_FIELD_CONSTRAINT = "error.field.constraint";

    public static final String USUARIO_EXISTE = "error.usuario-existe";
    public static final String USUARIO_NO_VALIDO = "error.userManagement.usuario-no-valido";

    public static final String ENTIDAD_NO_ENCONTRADA = "error.entidad-no-encontrada";
    public static final String ID_EXISTE = "error.id-existe";
    public static final String ID_FALTA = "error.id-falta";
    public static final String ENTITY_DELETED = "error.entity.deleted";

    public static final String QUERY_NO_SOPORTADA = "error.query-no-soportada";

    // ETL
    public static final String ETL_CURRENTLY_DELETED = "error.etl.currentlyDeleted";
    public static final String ETL_CURRENTLY_NOT_DELETED = "error.etl.currentlyNotDeleted";
    public static final String ETL_CRON_EXPRESSION_NOT_VALID = "error.etl.cronExpressionNotValid";
    public static final String ETL_SCHEDULE_ERROR = "error.etl.scheduleError";
    public static final String ETL_UNSCHEDULE_ERROR = "error.etl.unscheduleError";
    public static final String ETL_CODE_EXISTS = "error.etl.codeExists";
    public static final String ETL_ATTACHED_FILES_UPLOAD = "error.etl.attachedFilesUpload";
    public static final String ETL_MALFORMED_URL = "error.etl.urlMalformed";
    public static final String ETL_URL_NOT_EXIST = "error.etl.urlNotExist";
    public static final String ETL_CLONE_REPOSITORY = "error.etl.cloneRepository";
    public static final String ETL_REPLACE_REPOSITORY = "error.etl.replacingRepository";
    public static final String ETL_ERROR_ACCESS_DENIED = "error.etl.accessDenied";

    // HEALTH
    public static final String HEALTH_SERVICE_NAME_IS_BLANK = "error.health.serviceName.isBlank";
    public static final String HEALTH_ENDPOINT_IS_BLANK = "error.health.endpoint.isBlank";

    // PARAMETER
    public static final String PARAMETER_KEY_IS_DUPLICATED = "error.parameter.key.isDuplicated";
    public static final String PARAMETER_KEY_IS_BLANK = "error.parameter.key.isBlank";
    public static final String PARAMETER_VALUE_IS_BLANK = "error.parameter.value.isBlank";
    public static final String PARAMETER_EDIT = "error.parameter.edit";
    public static final String PARAMETER_KEY_IS_DUPLICATED_IN_GLOBAL_PARAMETER = "error.parameter.key.isDuplicatedInGlobalParameter";
    public static final String GLOBAL_PARAMETER_KEY_IS_DUPLICATED = "error.parameter.global.key.isDuplicated";

    // ORGANISM
    public static final String ORGANISM_KEY_IS_BLANK = "error.organism.name.isBlank";
    public static final String ORGANISM_KEY_IS_DUPLICATED = "error.organism.key.isDuplicated";
    public static final String ORGANISM_KEY_NOT_DELETED = "error.organism.key.notDeleted";
    public static final String ORGANISM_KEY_NOT_DELETED_BY_USER = "error.organism.key.notDeletedByUser";
    public static final String ORGANISM_FIELD_DESCRIPTION_LIMIT_EXCEED_ERROR = "error.organism.description.limitExceed";

    // ROL - ORGANISMO
    public static final String USER_ADMIN_ROL_ORGANISMOS_NOT_EMPTY = "error.userAdminRolOrganismoNotEmpty";
    public static final String USER_ROL_ORGANISMOS_NOT_EMPTY = "error.userRolOrganismoNotEmpty";
    public static final String USER_ROL_ORGANISMOS_JUST_ONCE = "error.userRolOrganismoJustOnce";

    // EXECUTION ETL GIT
    public static final String EXECUTION_UNKNOWN_ERROR = "error.execution.unkwown";
    public static final String EXECUTION_PULL_ERROR = "error.execution.pull";
    public static final String EXECUTION_URI_ERROR = "error.execution.uri";
    public static final String EXECUTION_CREDENTIALS_ERROR = "error.execution.credentials";

    // QUARZT
    public static final String QUARTZ_JOB_EXECUTION_ERROR = "error.quartz.jobExecutionError";

    private ErrorConstants() {
    }

}
