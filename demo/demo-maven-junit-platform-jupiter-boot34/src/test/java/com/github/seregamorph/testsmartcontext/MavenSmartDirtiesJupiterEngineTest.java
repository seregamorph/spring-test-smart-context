package com.github.seregamorph.testsmartcontext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import com.github.seregamorph.testsmartcontext.demo.ExtendWithTest;
import com.github.seregamorph.testsmartcontext.demo.Integration1MockBeanTest;
import com.github.seregamorph.testsmartcontext.demo.Integration1Test;
import com.github.seregamorph.testsmartcontext.demo.Integration2SpringJUnitConfigTest;
import com.github.seregamorph.testsmartcontext.demo.Integration2Test;
import com.github.seregamorph.testsmartcontext.demo.NoBaseClass1IntegrationTest;
import com.github.seregamorph.testsmartcontext.demo.NoBaseClass2IntegrationTest;
import com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextAfterClassTest;
import com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextBeforeClassTest;
import com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest;
import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

public class MavenSmartDirtiesJupiterEngineTest {

    private static final String ENGINE = "junit-jupiter";

    private static Map<String, Map<Class<?>, SmartDirtiesTestsHolder.ClassOrderState>> prevEngineClassOrderStateMap;

    @BeforeAll
    public static void beforeClass() {
        prevEngineClassOrderStateMap = SmartDirtiesTestsHolder.setEngineClassOrderStateMap(null);
        TestEventTracker.startTracking();
    }

    @AfterAll
    public static void afterClass() {
        TestEventTracker.stopTracking();
        SmartDirtiesTestsHolder.setEngineClassOrderStateMap(prevEngineClassOrderStateMap);
    }

    @Test
    public void testSuite() {
        // to avoid confusion of duplicated test execution output
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">>>EngineTestKit duplicating the suite>>>");

        var events = EngineTestKit.execute(ENGINE, request()
                .selectors(selectPackage("com.github.seregamorph.testsmartcontext.demo"))
                .build())
            .containerEvents();

        // 16 = 10 ITs + 3 Nested + 2 UTs + 1 suite
        events.assertStatistics(stats -> stats
            .started(16)
            .succeeded(16)
            .finished(16)
            .aborted(0)
            .failed(0));

        assertEquals(List.of(
            SampleDirtiesContextBeforeClassTest.class,
            ExtendWithTest.class,
            NoBaseClass1IntegrationTest.class,
            NoBaseClass2IntegrationTest.class,
            SampleDirtiesContextAfterClassTest.class,
            Integration1MockBeanTest.class,
            Integration1Test.class,
            Integration2SpringJUnitConfigTest.class,
            Integration2Test.class,
            SampleIntegrationTest.class
        ), new ArrayList<>(TestSmartDirtiesTestsHolder.getIntegrationTestClasses(ENGINE)));

        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(SampleDirtiesContextBeforeClassTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(ExtendWithTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(NoBaseClass1IntegrationTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(NoBaseClass2IntegrationTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(SampleDirtiesContextAfterClassTest.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration1MockBeanTest.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration1Test.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration1Test.NestedTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration1Test.NestedTest.DeeplyNestedTest.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration2SpringJUnitConfigTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration2Test.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration2Test.NestedTest.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(SampleIntegrationTest.class));

        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(SampleDirtiesContextBeforeClassTest.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(ExtendWithTest.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(NoBaseClass1IntegrationTest.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(NoBaseClass2IntegrationTest.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(SampleDirtiesContextAfterClassTest.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration1MockBeanTest.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration1Test.NestedTest.DeeplyNestedTest.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration1Test.NestedTest.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration1Test.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration2SpringJUnitConfigTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration2Test.NestedTest.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration2Test.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(SampleIntegrationTest.class));

        TestEventTracker.assertConsumedEvent("Started SmartDirtiesJupiterTestsSorterTest.shouldSortMostlyAlphabeticallyAndGroupSameConfigurations");
        TestEventTracker.assertConsumedEvent("Finished SmartDirtiesJupiterTestsSorterTest.shouldSortMostlyAlphabeticallyAndGroupSameConfigurations");
        TestEventTracker.assertConsumedEvent("Running com.github.seregamorph.testsmartcontext.demo.Unit1Test.test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextBeforeClassTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextBeforeClassTest");
        TestEventTracker.assertConsumedEvent("Running SampleDirtiesContextBeforeClassTest.test");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextAfterClassTest");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration1MockBeanTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration1MockBeanTest");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.Integration1MockBeanTest");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration2SpringJUnitConfigTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration2SpringJUnitConfigTest");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertEmpty();

        System.out.println("<<<EngineTestKit duplicating the suite<<<");
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }
}
