package com.homefit.server.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class FlywayRepairPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Flyway) {
            try {
                ((Flyway) bean).repair();
            } catch (Exception ignored) {
            }
        }
        return bean;
    }
}
