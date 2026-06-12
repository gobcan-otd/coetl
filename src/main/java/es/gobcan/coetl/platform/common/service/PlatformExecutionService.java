package es.gobcan.coetl.platform.common.service;

import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Execution;
import es.gobcan.coetl.domain.Execution.Type;
import es.gobcan.coetl.platform.common.dto.CheckResultResponse;


public interface PlatformExecutionService {

    Execution execute(Etl etl, Type type, String executor);

    CheckResultResponse runEtl(Etl etl, final String etlFilename, final String idExecution);

    CheckResultResponse removeEtl(Etl etl, final String etlFilename, final String idExecution);

    CheckResultResponse registerETL(Etl etl);

    void notifyExecutionError(Etl etl, String menssageError);

}
