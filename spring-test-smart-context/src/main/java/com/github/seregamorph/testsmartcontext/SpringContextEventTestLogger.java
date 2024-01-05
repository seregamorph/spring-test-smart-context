package com.github.seregamorph.testsmartcontext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Helper bean that logs spring bootstrap and shutdown events.
 *
 * @see SmartDirtiesContextTestExecutionListener
 */
public class SpringContextEventTestLogger implements InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringContextEventTestLogger.class);

    private static final ThreadLocal<Class<?>> currentAfterClass = new ThreadLocal<>();

    public static void setCurrentAfterClass(Class<?> testClass) {
        currentAfterClass.set(testClass);
    }

    public static void resetCurrentAfterClass() {
        currentAfterClass.remove();
    }

    @Override
    public void afterPropertiesSet() {
        LOGGER.info("Creating context for {}", CurrentTestContext.getCurrentTestClass());
    }

    @Override
    public void destroy() {
        Class<?> afterClass = currentAfterClass.get();
        if (afterClass == null) {
            // system shutdown hook
            LOGGER.info("Destroying context (hook)");
        } else {
            // triggered from IntegrationTestRunner.springTestContextAfterTestClass via
            // SmartDirtiesContextTestExecutionListener or spring DirtiesContextTestExecutionListener
            LOGGER.info("Destroying context for {}", afterClass);
        }
    }
}
