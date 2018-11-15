package org.adorsys.docusafe.spring.annotation;

import org.adorsys.docusafe.spring.config.UseSpringCachedTransactionalDocusafeServiceFactoryConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Created by peter on 14.11.18 20:25.
 */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = {java.lang.annotation.ElementType.TYPE})
@Documented
// @Import({UseSpringCachedTransactionalDocusafeServiceFactoryConfiguration.class})
public @interface UseSpringCachedTransactionalDocusafeServiceFactory {
}
