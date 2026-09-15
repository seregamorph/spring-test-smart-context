package com.github.seregamorph.testsmartcontext;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

class SmartDirtiesSkippedClassListenerTest {

    private static final List<Marker> createdMarkers = new ArrayList<>();

    private Map<String, Map<Class<?>, SmartDirtiesTestsSupport.ClassGroupState>> prevEngineClassOrderStateMap;

    @BeforeEach
    public void reset() {
        prevEngineClassOrderStateMap = SmartDirtiesTestsSupport.setEngineClassOrderStateMap(null);
        createdMarkers.clear();
    }

    @AfterEach
    public void restore() {
        new TestContextManager(BRunning.class).getTestContext().markApplicationContextDirty(null);
        SmartDirtiesTestsSupport.setEngineClassOrderStateMap(prevEngineClassOrderStateMap);
    }

    @Test
    public void normalClassesStillShareAndCloseContext() {
        run(2, 0, BRunning.class, YRunning.class);
        assertClosedOnce();
    }

    @Test
    public void skippedFirstClassDoesNotCloseContextBeforeRunningClass() {
        run(1, 1, ADisabled.class, BRunning.class);
        assertClosedOnce();
    }

    @Test
    public void skippedMiddleClassDoesNotCloseContextWhileAnotherClassNeedsIt() {
        run(2, 1, BRunning.class, MDisabled.class, YRunning.class);
        assertClosedOnce();
    }

    @Test
    public void skippedLastClassClosesContextWithoutItsSpringCallbacks() {
        run(1, 1, BRunning.class, ZDisabled.class);
        assertClosedOnce();
    }

    @Test
    public void runtimeDisabledConditionAlsoCompletesTheGroup() {
        run(1, 1, BRunning.class, ZConditional.class);
        assertClosedOnce();
    }

    @Test
    public void allSkippedClassesDoNotLoadAContext() {
        run(0, 2, ADisabled.class, ZDisabled.class);
        assertTrue(createdMarkers.isEmpty());
    }

    @Test
    public void skippedUnitClassIsIgnored() {
        run(0, 1, DisabledUnit.class);
        assertTrue(createdMarkers.isEmpty());
    }

    private void assertClosedOnce() {
        assertEquals(1, createdMarkers.size(), "Running classes must share a single context");
        assertEquals(1, createdMarkers.get(0).closeCount, "Close the context before Launcher returns");
    }

    private void run(long expectedSucceededCount, long expectedSkippedCount, Class<?>... testClasses) {
        LauncherDiscoveryRequestBuilder request = LauncherDiscoveryRequestBuilder.request()
            .filters(EngineFilter.includeEngines("junit-jupiter"));
        for (Class<?> testClass : testClasses) {
            request.selectors(DiscoverySelectors.selectClass(testClass));
        }
        SummaryGeneratingListener summary = new SummaryGeneratingListener();
        LauncherFactory.create().execute(request.build(), summary);
        assertAll(
            () -> assertEquals(0, summary.getSummary().getTotalFailureCount(),
                summary.getSummary().getFailures().toString()),
            () -> assertEquals(expectedSucceededCount, summary.getSummary().getTestsSucceededCount()),
            () -> assertEquals(expectedSkippedCount, summary.getSummary().getTestsSkippedCount())
        );
    }

    @Configuration
    public static class Config {

        @Bean
        public Marker marker() {
            Marker marker = new Marker();
            createdMarkers.add(marker);
            return marker;
        }
    }

    public static class Marker implements DisposableBean {

        private int closeCount;

        @Override
        public void destroy() {
            closeCount++;
        }
    }

    @SpringJUnitConfig(Config.class)
    public abstract static class Shared {

        @Autowired
        private Marker marker;

        @Test
        public void contextIsUsable() {
            assertEquals(0, marker.closeCount);
        }
    }

    @Disabled
    public static class ADisabled extends Shared {
    }

    public static class BRunning extends Shared {
    }

    @Disabled
    public static class MDisabled extends Shared {
    }

    public static class YRunning extends Shared {
    }

    @Disabled
    public static class ZDisabled extends Shared {
    }

    @DisabledIf("disabled")
    public static class ZConditional extends Shared {

        static boolean disabled() {
            return true;
        }
    }

    @Disabled
    public static class DisabledUnit {

        @Test
        public void unused() {
            fail("Must be skipped");
        }
    }
}
