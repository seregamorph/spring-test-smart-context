package com.github.seregamorph.testsmartcontext;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

class SmartDirtiesNestedClassTest {

    private static final List<Marker> createdMarkers = new ArrayList<>();

    private Map<String, Map<Class<?>, SmartDirtiesTestsSupport.ClassGroupState>> prevEngineClassOrderStateMap;

    @BeforeEach
    public void reset() {
        prevEngineClassOrderStateMap = SmartDirtiesTestsSupport.setEngineClassOrderStateMap(null);
        createdMarkers.clear();
    }

    @AfterEach
    public void restore() {
        new TestContextManager(Shared.class).getTestContext().markApplicationContextDirty(null);
        new TestContextManager(Outer.class).getTestContext().markApplicationContextDirty(null);
        new TestContextManager(PlainOuter.Other.class).getTestContext().markApplicationContextDirty(null);
        SmartDirtiesTestsSupport.setEngineClassOrderStateMap(prevEngineClassOrderStateMap);
    }

    @Test
    public void nestedClassWithCustomConfigurationSharesContextWithTopLevelClass() {
        run(3, 0, DiscoverySelectors.selectClass(Outer.class), DiscoverySelectors.selectClass(Shared.class));
        assertClosedOnce();
    }

    @Test
    public void nestedClassWithCustomConfigurationClosesContextInSingleClassRun() {
        run(2, 0, DiscoverySelectors.selectClass(Outer.class));
        assertClosedOnce();
    }

    @Test
    public void skippedNestedClassDoesNotPreventClosingSharedContext() {
        run(2, 1, DiscoverySelectors.selectClass(OuterWithDisabledNested.class),
            DiscoverySelectors.selectClass(Shared.class));
        assertClosedOnce();
    }

    @Test
    public void nestedClassExcludedFromTestPlanDoesNotPreventClosingSharedContext() {
        run(2, 0, DiscoverySelectors.selectMethod(Outer.class, "test"), DiscoverySelectors.selectClass(Shared.class));
        assertClosedOnce();
    }

    @Test
    public void nestedClassInheritingConfigurationIsNotRegistered() {
        run(2, 0, DiscoverySelectors.selectClass(OuterWithInheritingNested.class),
            DiscoverySelectors.selectClass(Shared.class));
        assertTrue(SmartDirtiesTestsSupport.getOrderStates(OuterWithInheritingNested.Inheriting.class).isEmpty());
        assertClosedOnce();
    }

    @Test
    public void plainOuterNestedClassSharesContextWithTopLevelClass() {
        run(4, 0, DiscoverySelectors.selectClass(PlainOuter.class), DiscoverySelectors.selectClass(Shared.class));
        assertTrue(SmartDirtiesTestsSupport.getOrderStates(PlainOuter.NonSpring.class).isEmpty());
        assertClosedOnce();
    }

    @Test
    public void plainOuterNestedClassClosesContextInSingleClassRun() {
        run(3, 0, DiscoverySelectors.selectClass(PlainOuter.class));
        assertClosedOnce();
    }

    @Test
    public void skippedPlainOuterDoesNotPreventClosingSharedContext() {
        run(1, 1, DiscoverySelectors.selectClass(DisabledPlainOuter.class),
            DiscoverySelectors.selectClass(Shared.class));
        assertClosedOnce();
    }

    private void assertClosedOnce() {
        assertEquals(1, createdMarkers.size(), "Classes with the same configuration must share a single context");
        assertEquals(1, createdMarkers.get(0).closeCount, "Close the context before Launcher returns");
    }

    private void run(long expectedSucceededCount, long expectedSkippedCount, DiscoverySelector... selectors) {
        LauncherDiscoveryRequestBuilder request = LauncherDiscoveryRequestBuilder.request()
            .filters(EngineFilter.includeEngines("junit-jupiter"))
            .selectors(selectors);
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
    public static class BaseConfig {
    }

    @Configuration
    public static class MarkerConfig {

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

    public abstract static class MarkerUser {

        @Autowired
        private Marker marker;

        @Test
        public void contextIsUsable() {
            assertEquals(0, marker.closeCount);
        }
    }

    @SpringJUnitConfig({BaseConfig.class, MarkerConfig.class})
    public static class Shared extends MarkerUser {
    }

    @SpringJUnitConfig(BaseConfig.class)
    public static class Outer {

        @Test
        public void test() {
        }

        @Nested
        @ContextConfiguration(classes = MarkerConfig.class)
        public class Custom extends MarkerUser {
        }
    }

    @SpringJUnitConfig(BaseConfig.class)
    public static class OuterWithDisabledNested {

        @Test
        public void test() {
        }

        @Disabled
        @Nested
        @ContextConfiguration(classes = MarkerConfig.class)
        public class Custom extends MarkerUser {
        }
    }

    @SpringJUnitConfig({BaseConfig.class, MarkerConfig.class})
    public static class OuterWithInheritingNested {

        @Nested
        public class Inheriting extends MarkerUser {
        }
    }

    public static class PlainOuter {

        @Nested
        @SpringJUnitConfig({BaseConfig.class, MarkerConfig.class})
        public class Custom extends MarkerUser {
        }

        @Nested
        public class NonSpring {

            @Test
            public void test() {
            }
        }

        @Nested
        @SpringJUnitConfig(BaseConfig.class)
        public class Other {

            @Test
            public void test() {
            }
        }
    }

    @Disabled
    public static class DisabledPlainOuter {

        @Nested
        @SpringJUnitConfig({BaseConfig.class, MarkerConfig.class})
        public class Custom extends MarkerUser {
        }
    }
}
