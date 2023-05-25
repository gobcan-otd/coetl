package es.gobcan.coetl.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties are configured in the application.yml file.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final Cas cas = new Cas();
    private final Installation installation = new Installation();
    private String enviroment = StringUtils.EMPTY;

    public Cas getCas() {
        return cas;
    }

    public static class Cas {

        // Required
        private String endpoint;
        private String service;

        // Optional
        private String login;
        private String logout;
        private String validate;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getLogin() {
            if (StringUtils.isEmpty(login)) {
                return StringUtils.removeEnd(endpoint, "/") + "/login";
            }
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getLogout() {
            if (StringUtils.isEmpty(logout)) {
                return StringUtils.removeEnd(endpoint, "/") + "/logout";
            }
            return logout;
        }

        public void setLogout(String logout) {
            this.logout = logout;
        }

        public String getValidate() {
            if (StringUtils.isEmpty(logout)) {
                return endpoint;
            }
            return validate;
        }

        public void setValidate(String validate) {
            this.validate = validate;
        }

    }

    public Installation getInstallation() {
        return installation;
    }

    public static class Installation {

        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

	public String getEnviroment() {
		return enviroment;
	}

	public void setEnviroment(String enviroment) {
		this.enviroment = enviroment;
	}

}
