package com.github.seregamorph.testsmartcontext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import com.github.seregamorph.testsmartcontext.demo.Integration1Test;
import com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextAfterClassTest;
import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

public class MavenSmartDirtiesTestngEngineTest {

    private static final String ENGINE = "testng";

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

        // 3 = 2 ITs + 0 UTs + 1 suite
        events.assertStatistics(stats -> stats
            .started(3)
            .succeeded(3)
            .finished(3)
            .aborted(0)
            .failed(0));

        assertEquals(List.of(
            Integration1Test.class,
            SampleDirtiesContextAfterClassTest.class
        ), new ArrayList<>(TestSmartDirtiesTestsHolder.getIntegrationTestClasses(ENGINE)));

        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration1Test.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(SampleDirtiesContextAfterClassTest.class));

        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration1Test.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(SampleDirtiesContextAfterClassTest.class));

        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextAfterClassTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextAfterClassTest");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextAfterClassTest");
        TestEventTracker.assertEmpty();

        System.out.println("<<<EngineTestKit duplicating the suite<<<");
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }
}
