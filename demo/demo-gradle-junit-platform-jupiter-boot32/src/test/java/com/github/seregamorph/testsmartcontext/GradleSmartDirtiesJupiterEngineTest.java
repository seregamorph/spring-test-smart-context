package com.github.seregamorph.testsmartcontext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import com.github.seregamorph.testsmartcontext.demo.Integration1Test;
import com.github.seregamorph.testsmartcontext.demo.Integration2Test;
import com.github.seregamorph.testsmartcontext.demo.NoBaseClass1IntegrationTest;
import com.github.seregamorph.testsmartcontext.demo.NoBaseClass2IntegrationTest;
import com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest;
import com.github.seregamorph.testsmartcontext.demo.Unit1Test;
import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

public class GradleSmartDirtiesJupiterEngineTest {

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

        // 9 = 5 ITs + 2 Nested + 1 UTs + 1 suite
        events.assertStatistics(stats -> stats
            .started(9)
            .succeeded(9)
            .finished(9)
            .aborted(0)
            .failed(0));

        assertEquals(List.of(
            Integration1Test.class,
            Integration2Test.class,
            NoBaseClass2IntegrationTest.class,
            NoBaseClass1IntegrationTest.class,
            SampleIntegrationTest.class,
            Unit1Test.class
        ), new ArrayList<>(SmartDirtiesTestsSupport.getTestClasses(ENGINE)));

        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(Integration1Test.class));
        assertFalse(SmartDirtiesTestsSupport.isFirstClassPerConfig(Integration1Test.NestedTest.class));
        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(Integration2Test.class));
        assertFalse(SmartDirtiesTestsSupport.isFirstClassPerConfig(NoBaseClass1IntegrationTest.class));
        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(NoBaseClass2IntegrationTest.class));
        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(SampleIntegrationTest.class));

        assertFalse(SmartDirtiesTestsSupport.isLastClassPerConfig(Integration1Test.NestedTest.class));
        assertTrue(SmartDirtiesTestsSupport.isLastClassPerConfig(Integration1Test.class));
        assertTrue(SmartDirtiesTestsSupport.isLastClassPerConfig(Integration2Test.class));
        assertTrue(SmartDirtiesTestsSupport.isLastClassPerConfig(NoBaseClass1IntegrationTest.class));
        assertFalse(SmartDirtiesTestsSupport.isLastClassPerConfig(NoBaseClass2IntegrationTest.class));
        assertTrue(SmartDirtiesTestsSupport.isLastClassPerConfig(SampleIntegrationTest.class));

        TestEventTracker.assertConsumedEvent("Running Unit1Test.test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.NoBaseClass2IntegrationTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.NoBaseClass2IntegrationTest");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.NoBaseClass1IntegrationTest");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertEmpty();

        System.out.println("<<<EngineTestKit duplicating the suite<<<");
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }
}
