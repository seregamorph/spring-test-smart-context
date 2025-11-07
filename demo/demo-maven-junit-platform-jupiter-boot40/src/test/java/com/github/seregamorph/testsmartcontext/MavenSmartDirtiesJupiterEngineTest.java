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
import com.github.seregamorph.testsmartcontext.demo.WebIntegrationTest;
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

    private static Map<String, Map<Class<?>, SmartDirtiesTestsSupport.ClassOrderState>> prevEngineClassOrderStateMap;

    @BeforeAll
    public static void beforeClass() {
        prevEngineClassOrderStateMap = SmartDirtiesTestsSupport.setEngineClassOrderStateMap(null);
        TestEventTracker.startTracking();
    }

    @AfterAll
    public static void afterClass() {
        TestEventTracker.stopTracking();
        SmartDirtiesTestsSupport.setEngineClassOrderStateMap(prevEngineClassOrderStateMap);
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
            .started(17)
            .succeeded(17)
            .finished(17)
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
            SampleIntegrationTest.class,
            WebIntegrationTest.class
        ), new ArrayList<>(TestSmartDirtiesTestsHolder.getIntegrationTestClasses(ENGINE)));

        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(SampleDirtiesContextBeforeClassTest.class));
        assertFalse(SmartDirtiesTestsSupport.isFirstClassPerConfig(ExtendWithTest.class));
        assertFalse(SmartDirtiesTestsSupport.isFirstClassPerConfig(NoBaseClass1IntegrationTest.class));
        assertFalse(SmartDirtiesTestsSupport.isFirstClassPerConfig(NoBaseClass2IntegrationTest.class));
        assertFalse(SmartDirtiesTestsSupport.isFirstClassPerConfig(SampleDirtiesContextAfterClassTest.class));
        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(Integration1MockBeanTest.class));
        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(Integration1Test.class));
        assertFalse(SmartDirtiesTestsSupport.isFirstClassPerConfig(Integration1Test.NestedTest.class));
        assertFalse(SmartDirtiesTestsSupport.isFirstClassPerConfig(Integration1Test.NestedTest.DeeplyNestedTest.class));
        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(Integration2SpringJUnitConfigTest.class));
        assertFalse(SmartDirtiesTestsSupport.isFirstClassPerConfig(Integration2Test.class));
        assertFalse(SmartDirtiesTestsSupport.isFirstClassPerConfig(Integration2Test.NestedTest.class));
        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(SampleIntegrationTest.class));
        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(WebIntegrationTest.class));

        assertFalse(SmartDirtiesTestsSupport.isLastClassPerConfig(SampleDirtiesContextBeforeClassTest.class));
        assertFalse(SmartDirtiesTestsSupport.isLastClassPerConfig(ExtendWithTest.class));
        assertFalse(SmartDirtiesTestsSupport.isLastClassPerConfig(NoBaseClass1IntegrationTest.class));
        assertFalse(SmartDirtiesTestsSupport.isLastClassPerConfig(NoBaseClass2IntegrationTest.class));
        assertTrue(SmartDirtiesTestsSupport.isLastClassPerConfig(SampleDirtiesContextAfterClassTest.class));
        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(Integration1MockBeanTest.class));
        assertFalse(SmartDirtiesTestsSupport.isLastClassPerConfig(Integration1Test.NestedTest.DeeplyNestedTest.class));
        assertFalse(SmartDirtiesTestsSupport.isLastClassPerConfig(Integration1Test.NestedTest.class));
        assertTrue(SmartDirtiesTestsSupport.isLastClassPerConfig(Integration1Test.class));
        assertFalse(SmartDirtiesTestsSupport.isLastClassPerConfig(Integration2SpringJUnitConfigTest.class));
        assertFalse(SmartDirtiesTestsSupport.isFirstClassPerConfig(Integration2Test.NestedTest.class));
        assertTrue(SmartDirtiesTestsSupport.isLastClassPerConfig(Integration2Test.class));
        assertTrue(SmartDirtiesTestsSupport.isLastClassPerConfig(SampleIntegrationTest.class));
        assertTrue(SmartDirtiesTestsSupport.isLastClassPerConfig(WebIntegrationTest.class));

        TestEventTracker.assertConsumedEvent("Started SmartDirtiesJupiterTestsSorterTest.shouldSortMostlyAlphabeticallyAndGroupSameConfigurations");
        TestEventTracker.assertConsumedEvent("Finished SmartDirtiesJupiterTestsSorterTest.shouldSortMostlyAlphabeticallyAndGroupSameConfigurations");
        TestEventTracker.assertConsumedEvent("Running com.github.seregamorph.testsmartcontext.demo.Unit1Test.test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextBeforeClassTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextBeforeClassTest");
        TestEventTracker.assertConsumedEvent("Running SampleDirtiesContextBeforeClassTest.test");
        TestEventTracker.assertConsumedEvent("AfterAll SampleDirtiesContextBeforeClassTest");
        TestEventTracker.assertConsumedEvent("Context paused for null");
        TestEventTracker.assertConsumedEvent("Context restarted for com.github.seregamorph.testsmartcontext.demo.ExtendWithTest");
        TestEventTracker.assertConsumedEvent("Running ExtendWithTest.test");
        TestEventTracker.assertConsumedEvent("AfterAll ExtendWithTest");
        TestEventTracker.assertConsumedEvent("Context paused for null");
        TestEventTracker.assertConsumedEvent("Context restarted for com.github.seregamorph.testsmartcontext.demo.NoBaseClass1IntegrationTest");
        TestEventTracker.assertConsumedEvent("Running NoBaseClass1IntegrationTest.test");
        TestEventTracker.assertConsumedEvent("AfterAll NoBaseClass1IntegrationTest");
        TestEventTracker.assertConsumedEvent("Context paused for null");
        TestEventTracker.assertConsumedEvent("Context restarted for com.github.seregamorph.testsmartcontext.demo.NoBaseClass2IntegrationTest");
        TestEventTracker.assertConsumedEvent("Running NoBaseClass2IntegrationTest.test");
        TestEventTracker.assertConsumedEvent("AfterAll NoBaseClass2IntegrationTest");
        TestEventTracker.assertConsumedEvent("Context paused for null");
        TestEventTracker.assertConsumedEvent("Context restarted for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextAfterClassTest");
        TestEventTracker.assertConsumedEvent("Running SampleDirtiesContextAfterClassTest.test");
        TestEventTracker.assertConsumedEvent("AfterAll SampleDirtiesContextAfterClassTest");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextAfterClassTest");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration1MockBeanTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration1MockBeanTest");
        TestEventTracker.assertConsumedEvent("Running Integration1MockBeanTest.test");
        TestEventTracker.assertConsumedEvent("Auto-destroying context for com.github.seregamorph.testsmartcontext.demo.Integration1MockBeanTest");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Running Integration1Test.test");
        TestEventTracker.assertConsumedEvent("Auto-destroying context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration2SpringJUnitConfigTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration2SpringJUnitConfigTest");
        TestEventTracker.assertConsumedEvent("Running Integration2SpringJUnitConfigTest.test");
        TestEventTracker.assertConsumedEvent("Context paused for null");
        TestEventTracker.assertConsumedEvent("Context restarted for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Running Integration2Test.test");
        TestEventTracker.assertConsumedEvent("Running NestedTest.test");
        TestEventTracker.assertConsumedEvent("Auto-destroying context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertConsumedEvent("Auto-destroying context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.WebIntegrationTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.WebIntegrationTest");
        TestEventTracker.assertConsumedEvent("Auto-destroying context for com.github.seregamorph.testsmartcontext.demo.WebIntegrationTest");
        TestEventTracker.assertEmpty();

        System.out.println("<<<EngineTestKit duplicating the suite<<<");
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }
}
