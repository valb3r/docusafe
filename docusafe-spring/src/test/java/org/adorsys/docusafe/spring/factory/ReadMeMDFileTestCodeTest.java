/*
Ich bekomme das nicht ans laufen....
package org.adorsys.docusafe.spring.factory;

import org.adorsys.docusafe.business.types.UserID;
import org.adorsys.docusafe.business.types.complex.UserIDAuth;
import org.adorsys.docusafe.cached.transactional.CachedTransactionalDocumentSafeService;
import org.adorsys.docusafe.spring.annotation.UseDocusafeSpringConfiguration;
import org.adorsys.encobject.domain.ReadKeyPassword;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootConfiguration
public class ReadMeMDFileTestCodeTest {

    @Autowired
    CachedTransactionalDocumentSafeService cachedTransactionalDocumentSafeService;

    @Test
    public void run() {
        // create service
        // nothing to do

        // create user
        UserIDAuth userIDAuth = new UserIDAuth(new UserID("peter"), new ReadKeyPassword("passwordOfPeter"));
        cachedTransactionalDocumentSafeService.createUser(userIDAuth);

    }

    @Configuration
    @EnableConfigurationProperties
    @UseDocusafeSpringConfiguration
    public static class Config {

        @Bean
        CachedTransactionalDocumentSafeService getCachedTransactionalDocumentSafeService(SpringCachedTransactionalDocusafeServiceFactory factory) {
            return factory.getCachedTransactionalDocumentSafeServiceWithSubdir(null);
        }

    }
}
*/


