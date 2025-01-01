package com.github.seregamorph.testsmartcontext;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Listener that sets current test class to the {@link CurrentTestContext} for further usage.
 *
 * @author Sergey Chernov
 */
public class CurrentTestContextTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public int getOrder() {
        return 500;
    }

    @Override
    public void beforeTestClass(TestContext testContext) {
        // stack Nested classes
        CurrentTestContext.pushCurrentTestClass(testContext.getTestClass());
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        // pop Nested classes
        CurrentTestContext.popCurrentTestClass();
    }
}
