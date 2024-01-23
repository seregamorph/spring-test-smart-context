package com.github.seregamorph.testsmartcontext;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import java.util.List;

public class SmartDirtiesContextTestExecutionContextCustomizer implements ContextCustomizerFactory {
    @Override
    public ContextCustomizer createContextCustomizer(@NonNull Class<?> testClass, @NonNull List<ContextConfigurationAttributes> configAttributes) {
        return (context, mergedConfig) -> {
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            if (beanFactory instanceof BeanDefinitionRegistry) {
                RootBeanDefinition bd = new RootBeanDefinition(SpringContextEventTestLogger.class);
                bd.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                bd.setResourceDescription("registered by " + this.getClass().getSimpleName());
                ((BeanDefinitionRegistry) beanFactory).registerBeanDefinition(SpringContextEventTestLogger.class.getName(), bd);
            }
        };
    }
}
