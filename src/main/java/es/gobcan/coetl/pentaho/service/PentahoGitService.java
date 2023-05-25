package es.gobcan.coetl.pentaho.service;

import java.io.UnsupportedEncodingException;

import es.gobcan.coetl.domain.Etl;

public interface PentahoGitService {
    public String cloneRepository(Etl etl);
    public String replaceRepository(Etl etl);

    public void updateRepository(Etl etl);

    public String getMainFileContent(Etl etl) throws UnsupportedEncodingException;

    public String getMainFileName(Etl etl);
}
