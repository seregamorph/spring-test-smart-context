package com.github.seregamorph.testsmartcontext;

import static com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSupport.isInnerClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Listener that works in more tricky way than spring
 * {@link org.springframework.test.context.support.DirtiesContextTestExecutionListener}. Based on known list (ordered)
 * of tests to execute (reordered via {@link com.github.seregamorph.testsmartcontext.jupiter.SmartDirtiesClassOrderer}
 * for Jupiter classes, {@link com.github.seregamorph.testsmartcontext.testng.SmartDirtiesSuiteListener} for TestNG
 * classes or {@link SmartDirtiesPostDiscoveryFilter} for JUnit 4 classes), the last test in each group that shares the
 * same configuration (=share the same spring context) will automatically close the ApplicationContext on after-class,
 * which will release resources as well (like Docker containers defined as spring beans). See detailed explanation <a
 * href="https://github.com/seregamorph/spring-test-smart-context/blob/master/README.md">README</a>.
 *
 * @author Sergey Chernov
 * @see com.github.seregamorph.testsmartcontext.jupiter.SmartDirtiesClassOrderer
 * @see com.github.seregamorph.testsmartcontext.testng.SmartDirtiesSuiteListener
 * @see SpringContextEventLoggerListener
 */
public class SmartDirtiesContextTestExecutionListener extends AbstractTestExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(SmartDirtiesContextTestExecutionListener.class);

    private static final ThreadLocal<Boolean> currentAutoClosingContext = new ThreadLocal<>();

    @Override
    public int getOrder() {
        // DirtiesContextTestExecutionListener.getOrder() - 10
        // (executes afterTestClass later than DirtiesContextTestExecutionListener as
        // listeners are reverse ordered for after-calls)
        //noinspection MagicNumber
        return 2990;
    }

    @Override
    public void beforeTestClass(TestContext testContext) {
        // stack Nested classes
        CurrentTestContext.pushCurrentTestClass(testContext.getTestClass());
        Class<?> testClass = testContext.getTestClass();
        if (isInnerClass(testClass)) {
            SmartDirtiesTestsSupport.verifyInnerClass(testClass);
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        currentAutoClosingContext.set(true);
        try {
            Class<?> testClass = testContext.getTestClass();
            if (SmartDirtiesTestsSupport.isLastClassPerConfig(testClass)) {
                if (testContext.hasApplicationContext()) {
                    logger.info("Auto-closing context after {}", testClass.getName());
                    testContext.markApplicationContextDirty(null);
                } else {
                    logger.info("Skipping auto-closing context after {} (already closed or failed to create)",
                        testClass.getName());
                }
            } else {
                logger.debug("Reusing context after {}", testClass.getName());
            }
        } finally {
            currentAutoClosingContext.remove();
            // pop Nested classes
            CurrentTestContext.popCurrentTestClass();
        }
    }

    static boolean isCurrentAutoClosingContext() {
        Boolean autoClosingContext = currentAutoClosingContext.get();
        return autoClosingContext != null && autoClosingContext;
    }
}
