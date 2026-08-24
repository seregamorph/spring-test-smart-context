package com.github.seregamorph.testtimeline;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;

/**
 * @author Sergey Chernov
 */
public class SpringContextEventTrackerListenerCustomizerFactory implements ContextCustomizerFactory {

    private static final AtomicInteger contextCounter = new AtomicInteger();

    @Nullable
    @Override
    public ContextCustomizer createContextCustomizer(
        Class<?> testClass,
        List<ContextConfigurationAttributes> configAttributes
    ) {
        return new ContextCustomizerImpl();
    }

    private static class ContextCustomizerImpl implements ContextCustomizer {

        private static final String TRACKING_BEAN_NAME = SpringLifecycleTrackingBean.class.getName();

        @Override
        public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
            String contextId = Integer.toString(contextCounter.incrementAndGet());
            context.addApplicationListener(new SpringContextEventTrackerListener(contextId));
            // the definition is registered before the definitions of the application beans, so this singleton is
            // destroyed after all of them and can track the moment when the context is completely destroyed
            RootBeanDefinition beanDefinition = new RootBeanDefinition(SpringLifecycleTrackingBean.class);
            beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            beanDefinition.setInstanceSupplier(() -> new SpringLifecycleTrackingBean(contextId));
            ((BeanDefinitionRegistry) context).registerBeanDefinition(TRACKING_BEAN_NAME, beanDefinition);
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
