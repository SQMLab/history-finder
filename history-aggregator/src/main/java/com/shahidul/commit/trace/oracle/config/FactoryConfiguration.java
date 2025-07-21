package com.shahidul.commit.trace.oracle.config;

import com.shahidul.commit.trace.oracle.core.factory.TracerFactory;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @since 2/6/2024
 */
@Configuration
public class FactoryConfiguration {
    @Bean
    ServiceLocatorFactoryBean tracerFactory() {
        ServiceLocatorFactoryBean tracerFactory = new ServiceLocatorFactoryBean();
        tracerFactory.setServiceLocatorInterface(TracerFactory.class);
        return tracerFactory;
    }
}
