package com.github.seregamorph.testsmartcontext;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(SpringContextEventLoggerListener.class);

    private final long createdNanos = System.nanoTime();

    public SpringContextEventLoggerListener() {
        onCreated();
    }

    protected void onCreated() {
        String currentTestClassName = CurrentTestContext.getCurrentTestClassName();
        if (currentTestClassName == null) {
            logger.error("Could not resolve current class name, ensure that SmartDirtiesContextTestExecutionListener " +
                "is registered for test class. Hint:\n" +
                "Maybe you should set @TestExecutionListeners.mergeMode to MERGE_WITH_DEFAULTS for your test class.");
        } else {
            logger.info("Creating context for {}", currentTestClassName);
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
        boolean isChild = event.getApplicationContext().getParent() != null;
        logger.info("Created {} in {} for {}",
            isChild ? "child context" : "context", contextInitFormatted, CurrentTestContext.getCurrentTestClassName());
    }

    protected void onContextClosedEvent(ContextClosedEvent event) {
        String testClassIdentifier = CurrentTestContext.getCurrentTestClassName();
        boolean isChild = event.getApplicationContext().getParent() != null;
        if (testClassIdentifier == null) {
            // system shutdown hook
            logger.info("Destroying {} (hook)", isChild ? "child context" : "context");
        } else {
            // triggered via SmartDirtiesContextTestExecutionListener or spring DirtiesContextTestExecutionListener
            logger.info("Destroying {} for {}", isChild ? "child context" : "context", testClassIdentifier);
        }
    }

    static String formatNanos(long timeNanos) {
        long millis = TimeUnit.NANOSECONDS.toMillis(timeNanos);
        return String.format(Locale.ROOT, "%.3f", millis / 1000.0d) + "s";
    }
}
