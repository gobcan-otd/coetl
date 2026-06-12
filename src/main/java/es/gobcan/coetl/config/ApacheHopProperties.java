package es.gobcan.coetl.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import es.gobcan.coetl.config.common.PlatformAuth;
import es.gobcan.coetl.config.common.PlatformHost;
import es.gobcan.coetl.config.common.PlatformProperties;

@Configuration
@ConfigurationProperties(prefix = "apache-hop", ignoreUnknownFields = false)
public class ApacheHopProperties implements PlatformProperties {

    private String endpoint = StringUtils.EMPTY;
    private String mainResourcePrefix = StringUtils.EMPTY;
    private String variables = StringUtils.EMPTY;
    private String hopFolder = StringUtils.EMPTY;
    private final PlatformAuth auth = new PlatformAuth();
    private final PlatformHost host = new PlatformHost();

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMainResourcePrefix() {
        return mainResourcePrefix;
    }

    public void setMainResourcePrefix(String mainResourcePrefix) {
        this.mainResourcePrefix = mainResourcePrefix;
    }

    public String getVariables() {
		return variables;
	}

	public void setVariables(String variables) {
		this.variables = variables;
	}

	public String getHopFolder() {
		return hopFolder;
	}

	public void setHopFolder(String hopFolder) {
		this.hopFolder = hopFolder;
	}

	public PlatformAuth getAuth() {
        return auth;
    }

    public PlatformHost getHost() {
        return host;
    }

}
