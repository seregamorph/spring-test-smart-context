package com.github.seregamorph.testsmartcontext.jupiter;

import com.github.seregamorph.testsmartcontext.SmartDirtiesContextTestExecutionListener;
import com.github.seregamorph.testsmartcontext.SpringContextEventTestLogger;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Base class for JUnit 5 Jupiter integration tests that create spring context. Supports
 * {@link SmartDirtiesContextTestExecutionListener} semantics to optimize IT suite execution.
 *
 * @see SmartDirtiesContextTestExecutionListener
 */
@Import(SpringContextEventTestLogger.class)
@TestExecutionListeners(listeners = {
    SmartDirtiesContextTestExecutionListener.class,
}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@ExtendWith(SpringExtension.class)
public abstract class AbstractJUnitSpringIntegrationTest {

}
