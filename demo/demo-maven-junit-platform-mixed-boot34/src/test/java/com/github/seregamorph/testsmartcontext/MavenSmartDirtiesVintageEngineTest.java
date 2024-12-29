package com.github.seregamorph.testsmartcontext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import com.github.seregamorph.testsmartcontext.demo.Integration2Test;
import com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextBeforeClassTest;
import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

public class MavenSmartDirtiesVintageEngineTest {

    private static final String ENGINE = "junit-vintage";

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
                .filters(new SmartDirtiesPostDiscoveryFilter())
                .build())
            .containerEvents();

        // 4 = 2 ITs + 1 UTs + 1 suite
        events.assertStatistics(stats -> stats
            .started(4)
            .succeeded(4)
            .finished(4)
            .aborted(0)
            .failed(0));

        assertEquals(List.of(
            Integration2Test.class,
            SampleDirtiesContextBeforeClassTest.class
        ), new ArrayList<>(TestSmartDirtiesTestsHolder.getIntegrationTestClasses(ENGINE)));

        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(Integration2Test.class));
        assertTrue(SmartDirtiesTestsSupport.isFirstClassPerConfig(SampleDirtiesContextBeforeClassTest.class));

        assertTrue(SmartDirtiesTestsSupport.isLastClassPerConfig(Integration2Test.class));
        assertTrue(SmartDirtiesTestsSupport.isLastClassPerConfig(SampleDirtiesContextBeforeClassTest.class));

        TestEventTracker.assertConsumedEvent("Running com.github.seregamorph.testsmartcontext.demo.Unit1Test.test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextBeforeClassTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextBeforeClassTest");
        TestEventTracker.assertConsumedEvent("Running SampleDirtiesContextBeforeClassTest.test");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextBeforeClassTest");
        TestEventTracker.assertEmpty();

        System.out.println("<<<EngineTestKit duplicating the suite<<<");
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }
}
