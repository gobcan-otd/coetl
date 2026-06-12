package es.gobcan.coetl.config.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import es.gobcan.coetl.config.ApacheHopProperties;
import es.gobcan.coetl.platform.hop.enumeration.ServerMethodsEnum;
import es.gobcan.coetl.platform.hop.service.util.HopUtil;
import es.gobcan.coetl.platform.hop.web.rest.dto.ServerStatusDTO;

@Component
public class ApacheHopHealthIndicator extends AbstractHealthIndicator {

    private final Logger logger = LoggerFactory.getLogger(ApacheHopHealthIndicator.class);
    
    @Autowired
    private ApacheHopProperties apacheHopProperties;
    
    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        final String pentahoUrl = HopUtil.getUrl(apacheHopProperties);
        builder.withDetail("endpoint", pentahoUrl);
        ResponseEntity<ServerStatusDTO> response = executeApacheHopUrl(pentahoUrl);
        if (HttpStatus.OK.equals(response.getStatusCode()) && response.getBody().isOnline()) {
            builder.up();
        } else {
            logger.warn("Apache Hop Server Integration not available");
            builder.down();
        }
    }
    
    public final ResponseEntity<ServerStatusDTO> executeApacheHopUrl(String url) {
        final String user = HopUtil.getUser(apacheHopProperties);
        final String password = HopUtil.getPassword(apacheHopProperties);
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("xml", "y");
        return HopUtil.execute(user, password, url, ServerMethodsEnum.STATUS, HttpMethod.GET, null, queryParams, ServerStatusDTO.class);
    }

}
