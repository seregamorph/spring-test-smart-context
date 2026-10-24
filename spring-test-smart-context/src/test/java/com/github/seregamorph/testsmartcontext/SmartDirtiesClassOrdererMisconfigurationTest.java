package com.github.seregamorph.testsmartcontext;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Maven Surefire/Failsafe 3.6.0 with runOrder=alphabetical passes the ClassOrderer$ClassName configuration parameter
 * which overrides the bundled junit-platform.properties, so SmartDirtiesClassOrderer is never called.
 */
class SmartDirtiesClassOrdererMisconfigurationTest {

    private Map<String, Map<Class<?>, SmartDirtiesTestsSupport.ClassGroupState>> prevEngineClassOrderStateMap;

    @BeforeEach
    public void reset() {
        prevEngineClassOrderStateMap = SmartDirtiesTestsSupport.setEngineClassOrderStateMap(null);
    }

    @AfterEach
    public void restore() {
        // the context is not closed by SmartDirtiesContextTestExecutionListener in case of misconfiguration
        new TestContextManager(FirstTest.class).getTestContext().markApplicationContextDirty(null);
        SmartDirtiesTestsSupport.setEngineClassOrderStateMap(prevEngineClassOrderStateMap);
        SmartDirtiesTestsSupport.resetClassOrdererMisconfiguration();
    }

    @Test
    public void overriddenClassOrdererIsReported() {
        TestExecutionSummary summary = run(ClassOrderer.ClassName.class.getName());

        assertEquals(2, summary.getTestsSucceededCount());
        assertEquals(2, summary.getTotalFailureCount());
        for (TestExecutionSummary.Failure failure : summary.getFailures()) {
            String message = failure.getException().getMessage();
            assertAll(
                () -> assertTrue(message.contains("junit.jupiter.testclass.order.default configuration parameter "
                    + "of the test plan is org.junit.jupiter.api.ClassOrderer$ClassName, but should be "
                    + "com.github.seregamorph.testsmartcontext.jupiter.SmartDirtiesClassOrderer"), message),
                () -> assertTrue(message.contains("remove runOrder"), message)
            );
        }
    }

    @Test
    public void bundledClassOrdererIsAccepted() {
        TestExecutionSummary summary = run(null);

        assertEquals(2, summary.getTestsSucceededCount());
        assertEquals(0, summary.getTotalFailureCount(), summary.getFailures().toString());
    }

    private static TestExecutionSummary run(String classOrderer) {
        LauncherDiscoveryRequestBuilder request = LauncherDiscoveryRequestBuilder.request()
            .filters(EngineFilter.includeEngines("junit-jupiter"))
            .selectors(DiscoverySelectors.selectClass(FirstTest.class),
                DiscoverySelectors.selectClass(SecondTest.class));
        if (classOrderer != null) {
            request.configurationParameter(ClassOrderer.DEFAULT_ORDER_PROPERTY_NAME, classOrderer);
        }
        SummaryGeneratingListener summary = new SummaryGeneratingListener();
        LauncherFactory.create().execute(request.build(), summary);
        return summary.getSummary();
    }

    @Configuration
    public static class Config {
    }

    @SpringJUnitConfig(Config.class)
    public abstract static class Shared {

        @Test
        public void test() {
        }
    }

    public static class FirstTest extends Shared {
    }

    public static class SecondTest extends Shared {
    }
}
