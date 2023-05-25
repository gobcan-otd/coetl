package es.gobcan.coetl.pentaho.service;

import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Execution;
import es.gobcan.coetl.domain.Execution.Type;
import es.gobcan.coetl.pentaho.web.rest.dto.WebResultDTO;

public interface PentahoExecutionService {

    Execution execute(Etl etl, Type type, String executor);
    WebResultDTO runEtl(Etl etl, final String etlFilename, final String idExecution);
    WebResultDTO removeEtl(Etl etl, final String etlFilename, final String idExecution);
}
