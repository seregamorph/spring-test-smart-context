package com.github.seregamorph.testsmartcontext;

import java.util.List;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;

public class SmartDirtiesContextTestExecutionContextCustomizerFactory implements ContextCustomizerFactory {

    @Override
    public ContextCustomizer createContextCustomizer(
        @NonNull Class<?> testClass,
        @NonNull List<ContextConfigurationAttributes> configAttributes
    ) {
        return new ContextCustomizerImpl();
    }

    private static class ContextCustomizerImpl implements ContextCustomizer {

        @Override
        public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            if (beanFactory instanceof BeanDefinitionRegistry) {
                RootBeanDefinition bd = new RootBeanDefinition(SpringContextEventTestLogger.class);
                bd.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                bd.setResourceDescription("registered by " + this.getClass().getSimpleName());
                ((BeanDefinitionRegistry) beanFactory).registerBeanDefinition(
                    SpringContextEventTestLogger.class.getName(), bd);
            }
        }

        @Override
        public boolean equals(Object obj) {
            // we need either static singleton ContextCustomizerImpl or equals like this to produce
            // equal org.springframework.test.context.MergedContextConfiguration
            return getClass() == obj.getClass();
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }
}
