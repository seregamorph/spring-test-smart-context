package com.github.seregamorph.testsmartcontext.testng;

import com.github.seregamorph.testsmartcontext.SmartDirtiesContextTestExecutionListener;
import com.github.seregamorph.testsmartcontext.SpringContextEventTestLogger;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

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

}
