package org.ephi.eip.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class RedcapConfig {

    @Value("${eip.redcap.api-url}")
    private String redcapApiUrl;

    @Value("${eip.redcap.api-token}")
    private String redcapToken;

    public void validateRedcapConfig() {
        if (redcapApiUrl.isEmpty() || redcapApiUrl.isBlank()) {
            throw new IllegalStateException("The REDCap server URL property is not set");
        }
        if (redcapToken.isEmpty() || redcapToken.isBlank()) {
            throw new IllegalStateException("The REDCap API token property is not set");
        }
    }
}
