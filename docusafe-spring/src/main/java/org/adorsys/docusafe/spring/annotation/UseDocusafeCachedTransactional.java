package org.adorsys.docusafe.spring.annotation;

import org.adorsys.docusafe.spring.config.UseDocusafeCachedTransactionalConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Created by peter on 02.10.18.
 */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = {java.lang.annotation.ElementType.TYPE})
@Documented
@Import({
        UseDocusafeCachedTransactionalConfiguration.class
})
public @interface UseDocusafeCachedTransactional {
}
