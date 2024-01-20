package com.github.seregamorph.testsmartcontext.testng;

import com.github.seregamorph.testsmartcontext.SmartDirtiesContextTestExecutionListener;
import com.github.seregamorph.testsmartcontext.SpringContextEventTestLogger;
import java.lang.reflect.Method;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;

/**
 * Base class for TestNG integration tests that create spring context. Supports
 * {@link SmartDirtiesContextTestExecutionListener} semantics to optimize IT suite execution.
 *
 * @see SmartDirtiesContextTestExecutionListener
 */
@Import(SpringContextEventTestLogger.class)
@TestExecutionListeners(listeners = {
        SmartDirtiesContextTestExecutionListener.class,
}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public abstract class AbstractTestNGSpringIntegrationTest extends AbstractTestNGSpringContextTests {

    @Override
    @AfterMethod(alwaysRun = true)
    protected void springTestContextAfterTestMethod(Method testMethod) throws Exception {
        SpringContextEventTestLogger.setCurrentAfterClass(getClass());
        try {
            super.springTestContextAfterTestMethod(testMethod);
        } finally {
            SpringContextEventTestLogger.resetCurrentAfterClass();
        }
    }

    @Override
    @AfterClass(alwaysRun = true)
    protected void springTestContextAfterTestClass() throws Exception {
        SpringContextEventTestLogger.setCurrentAfterClass(getClass());
        try {
            super.springTestContextAfterTestClass();
        } finally {
            SpringContextEventTestLogger.resetCurrentAfterClass();
        }
    }
}
