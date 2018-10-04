package org.adorsys.docusafe.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Created by peter on 04.10.18.
 */
@Component
@ConfigurationProperties(prefix = "docusafe.storeconnection")
@Validated
public class UseDocusafeProperties {
    @Nullable
    private String fulldescription;

    public String getFulldescription() {
        return fulldescription;
    }

    public void setFulldescription(String fulldescription) {
        this.fulldescription = fulldescription;
    }
}
