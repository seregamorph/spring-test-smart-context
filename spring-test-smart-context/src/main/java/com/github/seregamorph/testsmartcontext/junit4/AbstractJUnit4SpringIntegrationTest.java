package com.github.seregamorph.testsmartcontext.junit4;

import com.github.seregamorph.testsmartcontext.SmartDirtiesContextTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Base class for JUnit 4 integration tests that create spring context. Supports
 * {@link SmartDirtiesContextTestExecutionListener} semantics to optimize IT suite execution.
 *
 * @see SmartDirtiesContextTestExecutionListener
 * @deprecated support of JUnit 4 will be removed in 1.0 release
 */
@Deprecated
@TestExecutionListeners(listeners = {
    SmartDirtiesContextTestExecutionListener.class,
}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public abstract class AbstractJUnit4SpringIntegrationTest extends AbstractJUnit4SpringContextTests {

}
