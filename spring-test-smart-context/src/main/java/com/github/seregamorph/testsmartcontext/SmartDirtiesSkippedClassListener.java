package com.github.seregamorph.testsmartcontext;

import com.github.seregamorph.testsmartcontext.jupiter.JupiterIntegrationTestFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.BootstrapUtilsHelper;

/**
 * Completes skipped Jupiter classes which never receive Spring's afterTestClass callback.
 */
public class SmartDirtiesSkippedClassListener implements TestExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(SmartDirtiesSkippedClassListener.class);

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        Set<Class<?>> testPlanClasses = new HashSet<>();
        boolean hasIntegrationTests = false;
        for (TestIdentifier root : testPlan.getRoots()) {
            if (isJupiter(root)) {
                for (TestIdentifier testIdentifier : testPlan.getDescendants(root)) {
                    TestSource testSource = testIdentifier.getSource().orElse(null);
                    if (testSource instanceof ClassSource) {
                        Class<?> testClass = ((ClassSource) testSource).getJavaClass();
                        testPlanClasses.add(testClass);
                        hasIntegrationTests = hasIntegrationTests
                            || JupiterIntegrationTestFilter.getInstance().isIntegrationTest(testClass);
                    }
                }
            }
        }
        checkClassOrderer(testPlan, hasIntegrationTests);
        SmartDirtiesTestsSupport.completeNestedClassesMissingInTestPlan(SmartDirtiesTestsSupport.ENGINE_JUNIT_JUPITER,
            testPlanClasses);
    }

    private static void checkClassOrderer(TestPlan testPlan, boolean hasIntegrationTests) {
        if (!hasIntegrationTests) {
            SmartDirtiesTestsSupport.resetClassOrdererMisconfiguration();
            return;
        }
        String classOrderer = testPlan.getConfigurationParameters()
            .get(ClassOrderer.DEFAULT_ORDER_PROPERTY_NAME)
            .orElse(null);
        String misconfiguration = SmartDirtiesTestsSupport.checkJupiterClassOrderer(classOrderer);
        if (misconfiguration != null) {
            logger.warn(misconfiguration);
        }
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        if (!isJupiter(testIdentifier)) {
            return;
        }
        TestSource testSource = testIdentifier.getSource().orElse(null);
        if (!testIdentifier.isContainer() || !(testSource instanceof ClassSource)) {
            return;
        }
        Class<?> testClass = ((ClassSource) testSource).getJavaClass();
        Map<Class<?>, SmartDirtiesTestsSupport.ClassGroupState> testClasses =
            SmartDirtiesTestsSupport.getTestClasses(SmartDirtiesTestsSupport.ENGINE_JUNIT_JUPITER);
        if (testClasses == null) {
            return;
        }

        List<Class<?>> classesToComplete = new ArrayList<>();
        for (Class<?> nestedClass : NestedTestClasses.getNestedTestClasses(testClass)) {
            if (isDiscovered(testClasses, nestedClass)) {
                classesToComplete.add(nestedClass);
            }
        }
        if (isDiscovered(testClasses, testClass) && (SmartDirtiesTestsSupport.isInnerClass(testClass)
            || JupiterIntegrationTestFilter.getInstance().isIntegrationTest(testClass))) {
            classesToComplete.add(testClass);
        }

        for (Class<?> classToComplete : classesToComplete) {
            CurrentTestContext.pushCurrentTestClass(classToComplete);
            try {
                logger.debug("Completing skipped test class {}", classToComplete.getName());
                // Build metadata only when this completes the group. hasApplicationContext()
                // in the shared close path avoids loading a context for an entirely skipped group.
                SmartDirtiesContextTestExecutionListener.completeTestClass(classToComplete,
                    () -> BootstrapUtilsHelper.resolveTestContextBootstrapper(classToComplete).buildTestContext());
            } finally {
                CurrentTestContext.popCurrentTestClass();
            }
        }
    }

    private static boolean isJupiter(TestIdentifier testIdentifier) {
        return SmartDirtiesTestsSupport.ENGINE_JUNIT_JUPITER.equals(
            UniqueId.parse(testIdentifier.getUniqueId()).getEngineId().orElse(null));
    }

    private static boolean isDiscovered(
        Map<Class<?>, SmartDirtiesTestsSupport.ClassGroupState> testClasses,
        Class<?> testClass
    ) {
        SmartDirtiesTestsSupport.ClassGroupState classGroupState = testClasses.get(testClass);
        return classGroupState != null && classGroupState.discoveredItClasses.contains(testClass);
    }
}
