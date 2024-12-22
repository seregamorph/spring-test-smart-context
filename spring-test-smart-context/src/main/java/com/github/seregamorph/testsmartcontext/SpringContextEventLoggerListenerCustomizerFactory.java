package com.github.seregamorph.testsmartcontext;

import java.util.List;
import java.util.ServiceLoader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;

public class SpringContextEventLoggerListenerCustomizerFactory implements ContextCustomizerFactory {

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
            context.addApplicationListener(getSpringContextEventLoggerListener());
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

    private static SpringContextEventLoggerListener getSpringContextEventLoggerListener() {
        // overridden logic in demo-test-kit
        ServiceLoader<SpringContextEventLoggerListener> loader = ServiceLoader.load(SpringContextEventLoggerListener.class,
            SmartDirtiesTestsSorter.class.getClassLoader());

        if (loader.iterator().hasNext()) {
            return loader.iterator().next();
        } else {
            return new SpringContextEventLoggerListener();
        }
    }
}
