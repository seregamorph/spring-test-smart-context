package com.github.seregamorph.testsmartcontext.testng;

import com.github.seregamorph.testsmartcontext.CurrentTestContextTestExecutionListener;
import com.github.seregamorph.testsmartcontext.SmartDirtiesContextTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

/**
 * Base class for TestNG integration tests that create spring context. Supports
 * {@link SmartDirtiesContextTestExecutionListener} semantics to optimize IT suite execution.
 *
 * @author Sergey Chernov
 * @see SmartDirtiesContextTestExecutionListener
 */
@TestExecutionListeners(listeners = {
    CurrentTestContextTestExecutionListener.class,
    SmartDirtiesContextTestExecutionListener.class,
}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public abstract class AbstractTestNGSpringIntegrationTest extends AbstractTestNGSpringContextTests {

}
