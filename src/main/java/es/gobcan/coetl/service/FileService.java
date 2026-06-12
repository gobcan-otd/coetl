package es.gobcan.coetl.service;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import es.gobcan.coetl.domain.File;
import es.gobcan.coetl.domain.enumeration.TipoPlataformaEjecucion;

public interface FileService {
	
	public void uploadRepository(Path etlResourcesPath, MultipartFile file, TipoPlataformaEjecucion plataforma);
	public void updateRepository(Path etlResourcesPath, MultipartFile file, String originalFilename,  TipoPlataformaEjecucion plataforma);
	public void deleteRepository(Path etlResourcesPath, String filename, TipoPlataformaEjecucion plataforma);
	public File saveDatabase(MultipartFile fichero);
	public File updateDatabase(MultipartFile file, Long id);
	public void deleteDatabase(Long id);
	public File toFile(MultipartFile fichero);
}
