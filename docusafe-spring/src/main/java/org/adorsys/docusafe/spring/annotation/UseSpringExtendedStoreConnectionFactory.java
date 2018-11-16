package org.adorsys.docusafe.spring.annotation;

import org.adorsys.docusafe.spring.config.UseSpringExtendedStoreConnectionFactoryConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Created by peter on 14.11.18 14:24.
 */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = {java.lang.annotation.ElementType.TYPE})
@Documented
// @Import({UseSpringExtendedStoreConnectionFactoryConfiguration.class})
public @interface UseSpringExtendedStoreConnectionFactory {
}
