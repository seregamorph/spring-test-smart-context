package com.github.seregamorph.testsmartcontext;

import com.github.seregamorph.testsmartcontext.jupiter.JupiterIntegrationTestFilter;
import java.util.Map;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.springframework.test.context.TestContextManager;

/**
 * Completes skipped Jupiter classes which never receive Spring's afterTestClass callback.
 */
public class SmartDirtiesSkippedClassListener implements TestExecutionListener {

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        if (!SmartDirtiesTestsSupport.ENGINE_JUNIT_JUPITER.equals(
            UniqueId.parse(testIdentifier.getUniqueId()).getEngineId().orElse(null))) {
            return;
        }
        TestSource testSource = testIdentifier.getSource().orElse(null);
        if (!testIdentifier.isContainer() || !(testSource instanceof ClassSource)) {
            return;
        }
        Class<?> testClass = ((ClassSource) testSource).getJavaClass();
        Map<Class<?>, SmartDirtiesTestsSupport.ClassGroupState> testClasses =
            SmartDirtiesTestsSupport.getTestClasses(SmartDirtiesTestsSupport.ENGINE_JUNIT_JUPITER);
        SmartDirtiesTestsSupport.ClassGroupState classGroupState =
            testClasses == null ? null : testClasses.get(testClass);
        if (classGroupState == null || !classGroupState.discoveredItClasses.contains(testClass)
            || SmartDirtiesTestsSupport.isInnerClass(testClass)
            || !JupiterIntegrationTestFilter.getInstance().isIntegrationTest(testClass)) {
            return;
        }

        CurrentTestContext.pushCurrentTestClass(testClass);
        try {
            // Build metadata only when this completes the group. hasApplicationContext()
            // in the shared close path avoids loading a context for an entirely skipped group.
            SmartDirtiesContextTestExecutionListener.completeTestClass(testClass,
                () -> new TestContextManager(testClass).getTestContext());
        } finally {
            CurrentTestContext.popCurrentTestClass();
        }
    }
}
