package com.github.seregamorph.testsmartcontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import com.github.seregamorph.testsmartcontext.demo.Integration1Test;
import com.github.seregamorph.testsmartcontext.demo.Integration2Test;
import com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest;
import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

public class MavenSmartDirtiesVintageEngineTest {

    private static final String ENGINE = "junit-vintage";

    private static Map<String, Map<Class<?>, SmartDirtiesTestsSupport.ClassOrderState>> prevEngineClassOrderStateMap;

    @BeforeClass
    public static void beforeClass() {
        prevEngineClassOrderStateMap = SmartDirtiesTestsSupport.setEngineClassOrderStateMap(null);
        TestEventTracker.startTracking();
    }

    @AfterClass
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
                .filters(new SmartDirtiesPostDiscoveryFilter())
                .build())
            .containerEvents();

        // 5 = 3 ITs + 1 UTs + 1 suite
        events.assertStatistics(stats -> stats
            .started(5)
            .succeeded(5)
            .finished(5)
            .aborted(0)
            .failed(0));

        assertEquals(List.of(
            Integration1Test.class,
            Integration2Test.class,
            SampleIntegrationTest.class
        ), new ArrayList<>(TestSmartDirtiesTestsHolder.getIntegrationTestClasses(ENGINE)));

        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(Integration1Test.class));
        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(Integration2Test.class));
        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(SampleIntegrationTest.class));

        assertTrue(SmartDirtiesTestsSupport.isLastClassPerConfig(Integration1Test.class));
        assertTrue(SmartDirtiesTestsSupport.isLastClassPerConfig(Integration2Test.class));
        assertTrue(SmartDirtiesTestsSupport.isLastClassPerConfig(SampleIntegrationTest.class));

        TestEventTracker.assertConsumedEvent("Running Unit1Test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Auto-destroying context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Auto-destroying context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertConsumedEvent("Auto-destroying context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertEmpty();

        System.out.println("<<<EngineTestKit duplicating the suite<<<");
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }
}
