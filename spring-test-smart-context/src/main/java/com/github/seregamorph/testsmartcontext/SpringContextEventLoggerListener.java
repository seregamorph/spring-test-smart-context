package com.github.seregamorph.testsmartcontext;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Helper bean that logs spring bootstrap and shutdown events.
 *
 * @author Sergey Chernov
 * @see SmartDirtiesContextTestExecutionListener
 */
public class SpringContextEventLoggerListener implements ApplicationListener<ApplicationContextEvent> {

    private static final Log LOG = LogFactory.getLog(SpringContextEventLoggerListener.class);

    private final long createdNanos = System.nanoTime();

    public SpringContextEventLoggerListener() {
        onCreated();
    }

    protected void onCreated() {
        String currentTestClassName = CurrentTestContext.getCurrentTestClassName();
        if (currentTestClassName == null) {
            LOG.error("Could not resolve current class name, ensure that SmartDirtiesContextTestExecutionListener " +
                "is registered for test class. Hint:\n" +
                "Maybe you should set @TestExecutionListeners.mergeMode to MERGE_WITH_DEFAULTS for your test class.");
        } else {
            LOG.info("Creating context for " + currentTestClassName);
        }
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            onContextRefreshedEvent((ContextRefreshedEvent) event);
        } else if (event instanceof ContextClosedEvent) {
            onContextClosedEvent((ContextClosedEvent) event);
        }
    }

    protected void onContextRefreshedEvent(ContextRefreshedEvent event) {
        long nowNanos = System.nanoTime();
        String contextInitFormatted = formatNanos(nowNanos - createdNanos);
        LOG.info("Created context in " + contextInitFormatted + " for " + CurrentTestContext.getCurrentTestClassName());
    }

    protected void onContextClosedEvent(ContextClosedEvent event) {
        String testClassIdentifier = CurrentTestContext.getCurrentTestClassName();
        if (testClassIdentifier == null) {
            // system shutdown hook
            LOG.info("Destroying context (hook)");
        } else {
            // triggered via SmartDirtiesContextTestExecutionListener or spring DirtiesContextTestExecutionListener
            LOG.info("Destroying context for " + testClassIdentifier);
        }
    }

    static String formatNanos(long timeNanos) {
        long millis = TimeUnit.NANOSECONDS.toMillis(timeNanos);
        return String.format(Locale.ROOT, "%.3f", millis / 1000.0d) + "s";
    }
}
