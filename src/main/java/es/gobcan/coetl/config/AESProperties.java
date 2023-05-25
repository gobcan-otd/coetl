package es.gobcan.coetl.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aes", ignoreUnknownFields = false)
public class AESProperties {

    private String secretKeySalt = StringUtils.EMPTY;
    private String secretKeyPassword = StringUtils.EMPTY;

    public String getSecretKeySalt() {
        return secretKeySalt;
    }

    public void setSecretKeySalt(String secretKeySalt) {
        this.secretKeySalt = secretKeySalt;
    }

    public String getSecretKeyPassword() {
        return secretKeyPassword;
    }

    public void setSecretKeyPassword(String secretKeyPassword) {
        this.secretKeyPassword = secretKeyPassword;
    }
}
