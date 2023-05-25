package es.gobcan.coetl.config;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "git", ignoreUnknownFields = false)
public class GitProperties {

    private static final Logger LOG = LoggerFactory.getLogger(GitProperties.class);

    private String username = StringUtils.EMPTY;
    private String password = StringUtils.EMPTY;
    private String branch = StringUtils.EMPTY;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
