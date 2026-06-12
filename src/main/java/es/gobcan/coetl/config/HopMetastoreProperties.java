package es.gobcan.coetl.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

@Component
@PropertySource(value = "classpath:config/hop-metastore.json", factory = JsonPropertyFactory.class)
@ConfigurationProperties
public class HopMetastoreProperties {

    private String metastore;

    public String getMetastore() {
        return metastore;
    }

    public void setMetastore(List<?> metastore) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String metastoreText = metastore != null ? objectMapper.writeValueAsString(metastore.get(0)) : StringUtils.EMPTY;
        this.metastore = metastoreText;
    }

}
