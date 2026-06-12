package es.gobcan.coetl.service.impl;


import static es.gobcan.coetl.platform.pentaho.service.util.RemoteConnectionUtils.executeCommand;
import static es.gobcan.coetl.platform.pentaho.service.util.RemoteConnectionUtils.getSudoDestinationOptions;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Blob;
import java.sql.Timestamp;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ValidationException;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;

import es.gobcan.coetl.config.PentahoProperties;
import es.gobcan.coetl.config.common.PlatformHost;
import es.gobcan.coetl.config.ApacheHopProperties;
import es.gobcan.coetl.domain.File;
import es.gobcan.coetl.domain.enumeration.TipoPlataformaEjecucion;
import es.gobcan.coetl.repository.FileRepository;
import es.gobcan.coetl.service.FileService;
import es.gobcan.coetl.service.validator.FileValidator;

import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;


@Service
public class FileServiceImpl implements FileService {
	@Autowired
    private PentahoProperties pentahoProperties;
	
	@Autowired
	private ApacheHopProperties hopProperties;
	
	@Autowired
    private FileValidator fileValidator;
	
	@PersistenceContext
	private EntityManager entityManager;
	private final FileRepository fileRepository;

    public FileServiceImpl(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }
    
    private PlatformHost selectPlatform(TipoPlataformaEjecucion plataforma) {
        switch (plataforma) {
            case APACHE_HOP:
                return hopProperties.getHost();
            case PENTAHO:
                return pentahoProperties.getHost();
            default:
                throw new RuntimeException("Error occurred when determining the execution platform.");
        }
    }

    @Override
    // Saving file in local repository
    public void uploadRepository(Path etlResourcesPath, MultipartFile file, TipoPlataformaEjecucion plataforma) {
        // Permissions set to fix error when moving in PRE. This error happens because
        // the user who creates the file and the user who moves the file are not thesame.
        Set<PosixFilePermission> filePermissions = new HashSet<PosixFilePermission>();
        filePermissions.add(PosixFilePermission.OWNER_READ);
        filePermissions.add(PosixFilePermission.OWNER_WRITE);
        filePermissions.add(PosixFilePermission.GROUP_READ);
        filePermissions.add(PosixFilePermission.OTHERS_READ);

        OverthereConnection sudoDestinationConnection = Overthere.getConnection("ssh",getSudoDestinationOptions(selectPlatform(plataforma)));

        try {
            // Saving in TMP
            // Create fichero temporal
            Path tmpFilePath = Files.createTempFile(file.getOriginalFilename().replace(".", "_").concat("-"), null);
            Files.copy(file.getInputStream(), tmpFilePath, StandardCopyOption.REPLACE_EXISTING);
            Files.setPosixFilePermissions(tmpFilePath, filePermissions);
            // Moving file to repo folder
            executeCommand(sudoDestinationConnection, "cp", tmpFilePath.toString(),etlResourcesPath.toString().concat("/").concat(file.getOriginalFilename()));
            // Deleting temp file
            Files.delete(tmpFilePath);
        } catch (FileAlreadyExistsException e) {
            throw new RuntimeException("A file with that name already exists.");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            sudoDestinationConnection.close();
        }
    }

	@Override
	// Update file in local repository
	public void updateRepository(Path etlResourcesPath, MultipartFile file, String originalFilename,  TipoPlataformaEjecucion plataforma) {
		deleteRepository(etlResourcesPath, originalFilename, plataforma);
		uploadRepository(etlResourcesPath, file, plataforma);
	}

	@Override
	// File removal from local repository
	public void deleteRepository(Path etlResourcesPath, String filename, TipoPlataformaEjecucion plataforma) {
	      
		OverthereConnection sudoDestinationConnection = Overthere.getConnection("ssh", getSudoDestinationOptions(selectPlatform(plataforma)));
		try {
			executeCommand(sudoDestinationConnection, "rm", etlResourcesPath.toString().concat("/").concat(filename));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			sudoDestinationConnection.close();
		}
	}

	@Override
	public File saveDatabase(MultipartFile fichero) {
		File documento = this.toFile(fichero);
        fileValidator.validate(documento);
        return fileRepository.saveAndFlush(documento);
	}
	
	@Override
	public void deleteDatabase(Long id) {
		fileRepository.delete(id);
	}
	
	@Override
	public File updateDatabase(MultipartFile file, Long id) {
		File fichero = this.toFile(file);
		fileValidator.validate(fichero);
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		File realFile = fileRepository.getOne(id);
		realFile.setContent(fichero.getContent());
		realFile.setCreationDate(timestamp);
		realFile.setFormat(fichero.getFormat());
		realFile.setName(fichero.getName());

		return null;
	}

	@Override
	public File toFile(MultipartFile fichero) {
		Blob data;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        try {
            data = Hibernate.getLobCreator((Session) entityManager.getDelegate()).createBlob(fichero.getInputStream(), fichero.getSize());
        } catch (IOException e) {
            throw new ValidationException(e);
        }

        File documento = new File();
        documento.setName(fichero.getOriginalFilename());
        documento.setContent(data);
        documento.setFormat(fichero.getContentType());
        documento.setCreationDate(timestamp);
        return documento;
	}

}
