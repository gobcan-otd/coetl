package es.gobcan.coetl.pentaho.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.stream.Collectors;

import es.gobcan.coetl.config.GitProperties;
import es.gobcan.coetl.config.PentahoProperties;
import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.errors.CustomParameterizedExceptionBuilder;
import es.gobcan.coetl.errors.ErrorConstants;
import es.gobcan.coetl.pentaho.service.PentahoGitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler;

import static es.gobcan.coetl.pentaho.service.util.RemoteConnectionUtils.getSudoDestinationOptions;
import static es.gobcan.coetl.pentaho.service.util.RemoteConnectionUtils.executeCommand;
import static es.gobcan.coetl.pentaho.service.util.RemoteConnectionUtils.SftpException;

@Service
public class PentahoGitServiceImpl implements PentahoGitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PentahoGitServiceImpl.class);

    private static final String REPOSITORY_FOLDER_NAME = "repository";
    private static final String REPOSITORY_FOLDER_BACKUP_NAME = "repositoryBackup";
    private static final String ERROR_CREDENCIALES_GIT = "An error ocurred encoding git credentials";
    private static final String ERROR_URI_INCORRECT = "An error ocurred with URI repository in ETL with code \"%s\"";
    private static final String ERROR_PULL = "An error ocurred executing shell commands while pulling repository";
    private static final String ERROR_DESCONOCIDO = "Unknown error ocurred while pulling repository \"%s\"";

    @Autowired
    private PentahoProperties pentahoProperties;

    @Autowired
    private GitProperties gitProperties;

    @Override
    public String cloneRepository(Etl etl) {
        OverthereConnection sudoDestinationConnection = Overthere.getConnection("ssh", getSudoDestinationOptions(pentahoProperties.getHost()));
        String path = pentahoProperties.getHost().getResourcesPath().concat("/").concat(etl.getCode());

        try {
            executeCommand(sudoDestinationConnection, "mkdir", "-p", path);
            executeCommand(sudoDestinationConnection, "git", "-C", path, "clone", "--branch", gitProperties.getBranch(), getUrlRepositoryWithCredentials(etl.getUriRepository()));
            executeCommand(sudoDestinationConnection, "mv", path.concat("/").concat(getFolderRepositoryName(etl)), path.concat("/").concat(REPOSITORY_FOLDER_NAME));
            executeCommand(sudoDestinationConnection, "git", "-C", path.concat("/").concat(REPOSITORY_FOLDER_NAME), "remote", "set-url", "origin", etl.getUriRepository());
            changeOwnerUnzippedFiles(sudoDestinationConnection, path);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("An error ocurred encoding git credentials", e);
            return null;
        } catch (MalformedURLException e) {
            LOGGER.error("An error ocurred with URI repository in ETL with code {}", etl.getCode(), e);
            return null;
        } catch (SftpException e) {
            LOGGER.error("An error ocurred executing shell commands while cloning repository");
            return null;
        } catch (Exception e) {
            LOGGER.error("Unknown error ocurred while clone repository {}", etl.getUriRepository(), e);
            return null;
        } finally {
            sudoDestinationConnection.close();
        }

        return path.concat("/").concat(REPOSITORY_FOLDER_NAME);
    }

    @Override
    public void updateRepository(Etl etl) {
        OverthereConnection sudoDestinationConnection = Overthere.getConnection("ssh", getSudoDestinationOptions(pentahoProperties.getHost()));
        String path = pentahoProperties.getHost().getResourcesPath().concat("/").concat(etl.getCode()).concat("/").concat(REPOSITORY_FOLDER_NAME);
        try {
            executeCommand(sudoDestinationConnection, "git", "-C", path, "remote", "set-url", "origin", getUrlRepositoryWithCredentials(etl.getUriRepository()));
            executeCommand(sudoDestinationConnection, "git", "-C", path, "pull");
            executeCommand(sudoDestinationConnection, "git", "-C", path, "remote", "set-url", "origin", etl.getUriRepository());
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(ERROR_CREDENCIALES_GIT);
            throw new CustomParameterizedExceptionBuilder().message(ERROR_CREDENCIALES_GIT).code(ErrorConstants.EXECUTION_CREDENTIALS_ERROR).build();
        } catch (MalformedURLException e) {
            LOGGER.error(String.format(ERROR_URI_INCORRECT, etl.getCode()), e);
            throw new CustomParameterizedExceptionBuilder().message(String.format(ERROR_URI_INCORRECT, etl.getCode()))
            .code(ErrorConstants.EXECUTION_URI_ERROR).build();
        }catch (SftpException e) {
            LOGGER.error(ERROR_PULL, e);
            throw new CustomParameterizedExceptionBuilder().message(ERROR_PULL).code(ErrorConstants.EXECUTION_PULL_ERROR).build();
        } catch (Exception e) {
            LOGGER.error(String.format(ERROR_DESCONOCIDO, etl.getUriRepository()), e);
            throw new CustomParameterizedExceptionBuilder().message(String.format(ERROR_DESCONOCIDO, etl.getUriRepository()))
            .code(ErrorConstants.EXECUTION_UNKNOWN_ERROR).build();
        } finally {
            sudoDestinationConnection.close();
        }
    }

    @Override
    public String replaceRepository(Etl etl) {
        OverthereConnection sudoDestinationConnection = Overthere.getConnection("ssh", getSudoDestinationOptions(pentahoProperties.getHost()));
        String path = pentahoProperties.getHost().getResourcesPath().concat("/").concat(etl.getCode());
        String newRepository = null;

        try {
            executeCommand(sudoDestinationConnection, "mv", path.concat("/").concat(REPOSITORY_FOLDER_NAME), path.concat("/").concat(REPOSITORY_FOLDER_BACKUP_NAME));

            newRepository = cloneRepository(etl);

            if (newRepository == null) {
                executeCommand(sudoDestinationConnection, "rm", "-Rf", path.concat("/").concat(REPOSITORY_FOLDER_NAME));
                executeCommand(sudoDestinationConnection, "mv", path.concat("/").concat(REPOSITORY_FOLDER_BACKUP_NAME), path.concat("/").concat(REPOSITORY_FOLDER_NAME));
            } else {
                executeCommand(sudoDestinationConnection, "rm", "-Rf", path.concat("/").concat(REPOSITORY_FOLDER_BACKUP_NAME));
            }
        } catch (Exception e) {
            LOGGER.error("Unknown error ocurred while replacing repository {}", etl.getUriRepository());
            return null;
        } finally {
            sudoDestinationConnection.close();
        }

        return newRepository;
    }

    @Override
    public String getMainFileContent(Etl etl) throws UnsupportedEncodingException {
        OverthereConnection sudoSourceConnection = Overthere.getConnection("ssh", getSudoDestinationOptions(pentahoProperties.getHost()));

        CapturingOverthereExecutionOutputHandler oh = CapturingOverthereExecutionOutputHandler.capturingHandler();
        String basePath = pentahoProperties.getHost().getResourcesPath().concat("/").concat(etl.getCode()).concat("/" + REPOSITORY_FOLDER_NAME + "/");

        executeCommand(sudoSourceConnection, oh, "ls", basePath.concat(pentahoProperties.getHost().getMainResourcePrefix() + "*"));

        //Se asume que siempre habrá un fichero main* y que la ruta saldrá la última de las líneas volcadas
        String mainFileNamePath = oh.getOutputLines().get( oh.getOutputLines().size()-1 ).trim();
        
        OverthereFile sourceMainFile = sudoSourceConnection.getFile(mainFileNamePath);
        String result = new BufferedReader(new InputStreamReader(sourceMainFile.getInputStream(), "UTF-8"))
            .lines().collect(Collectors.joining("\n"));

        sudoSourceConnection.close();
        return result;
    }

    @Override
    public String getMainFileName(Etl etl) {
        OverthereConnection sudoSourceConnection = Overthere.getConnection("ssh", getSudoDestinationOptions(pentahoProperties.getHost()));

        CapturingOverthereExecutionOutputHandler oh = CapturingOverthereExecutionOutputHandler.capturingHandler();
        String basePath = pentahoProperties.getHost().getResourcesPath().concat("/").concat(etl.getCode()).concat("/" + REPOSITORY_FOLDER_NAME + "/");

        executeCommand(sudoSourceConnection, oh, "ls", basePath.concat(pentahoProperties.getHost().getMainResourcePrefix() + "*"));

      //Se asume que siempre habrá un fichero main* y que la ruta saldrá la última de las líneas volcadas
        String mainFileNamePath = oh.getOutputLines().get( oh.getOutputLines().size()-1 ).trim();
        
        sudoSourceConnection.close();

        return mainFileNamePath.substring(mainFileNamePath.lastIndexOf('/') + 1).split("\\.")[0];
    }

    private String getUrlRepositoryWithCredentials(String urlRepository) throws UnsupportedEncodingException, MalformedURLException {
        URL url = new URL(urlRepository);
        return url.getProtocol()
            .concat("://")
            .concat(URLEncoder.encode(gitProperties.getUsername(), "UTF-8"))
            .concat(":")
            .concat(URLEncoder.encode(gitProperties.getPassword(), "UTF-8"))
            .concat("@")
            .concat(url.getAuthority()).concat(url.getPath());
    }

    private String getFolderRepositoryName(Etl etl) {
        String folder = etl.getUriRepository().substring(etl.getUriRepository().lastIndexOf('/') + 1);
        folder = folder.replace(".git", "");
        return folder;
    }

    private void changeOwnerUnzippedFiles(OverthereConnection sudoConnection, String path) {
        String chownParameter = pentahoProperties.getHost().getOwnerUserResourcesPath().concat(":").concat(pentahoProperties.getHost().getOwnerGroupResourcesPath());
        executeCommand(sudoConnection, "chown", chownParameter, "-R", path);
    }
}
