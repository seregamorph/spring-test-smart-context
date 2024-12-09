package com.github.seregamorph.testsmartcontext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Helper bean that logs spring bootstrap and shutdown events.
 *
 * @see SmartDirtiesContextTestExecutionListener
 */
public class SpringContextEventTestLogger implements InitializingBean, DisposableBean {

    private static final Log LOG = LogFactory.getLog(SpringContextEventTestLogger.class);

    @Override
    public void afterPropertiesSet() {
        LOG.info("Creating context for " + CurrentTestContext.getCurrentTestClassName());
    }

    @Override
    public void destroy() {
        String testClassIdentifier = CurrentTestContext.getCurrentTestClassName();
        if (testClassIdentifier == null) {
            // system shutdown hook
            LOG.info("Destroying context (hook)");
        } else {
            // triggered via SmartDirtiesContextTestExecutionListener or spring DirtiesContextTestExecutionListener
            LOG.info("Destroying context for " + testClassIdentifier);
        }
    }
}
