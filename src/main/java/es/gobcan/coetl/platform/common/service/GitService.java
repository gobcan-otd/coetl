package es.gobcan.coetl.platform.common.service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import com.xebialabs.overthere.OverthereConnection;

import es.gobcan.coetl.domain.Etl;

public interface GitService {

    public String cloneRepository(Etl etl);

    public String replaceRepository(Etl etl, String oldPath);

    public void updateRepository(Etl etl);
    
    public void deleteRepository(Etl etl);

    public String getMainFileContent(Etl etl) throws UnsupportedEncodingException;

    public String getMainFileName(Etl etl);

    public void checkFileParameters(Etl etl, String originalPath, String newPath, OverthereConnection sudoDestinationConnection);

	Map<String, List<String>> getEtlMetadataInfo(Etl etl) throws UnsupportedEncodingException;

}
