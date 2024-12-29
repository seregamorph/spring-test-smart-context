package com.github.seregamorph.testsmartcontext;

import static com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSupport.isInnerClass;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log LOG = LogFactory.getLog(SmartDirtiesContextTestExecutionListener.class);

    @Override
    public int getOrder() {
        // DirtiesContextTestExecutionListener.getOrder() + 1
        //noinspection MagicNumber
        return 3001;
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
        try {
            Class<?> testClass = testContext.getTestClass();
            if (SmartDirtiesTestsSupport.isLastClassPerConfig(testClass)) {
                LOG.info("markDirty (closing context) after " + testClass.getName());
                testContext.markApplicationContextDirty(null);
            } else {
                LOG.debug("Reusing context after " + testClass.getName());
            }
        } finally {
            // pop Nested classes
            CurrentTestContext.popCurrentTestClass();
        }
    }
}
