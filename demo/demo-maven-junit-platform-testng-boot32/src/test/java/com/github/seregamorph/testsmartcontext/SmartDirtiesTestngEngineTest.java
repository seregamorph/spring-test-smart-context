package com.github.seregamorph.testsmartcontext;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import com.github.seregamorph.testsmartcontext.demo.Integration1IT;
import com.github.seregamorph.testsmartcontext.demo.Integration1SecondIT;
import com.github.seregamorph.testsmartcontext.demo.Integration2IT;
import com.github.seregamorph.testsmartcontext.demo.SampleIT;
import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import java.util.ArrayList;
import java.util.List;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SmartDirtiesTestngEngineTest {

    private static final String ENGINE = "testng";

    @BeforeMethod
    public void before() {
        TestEventTracker.startTracking();
    }

    @AfterMethod
    public void after() {
        TestEventTracker.stopTracking();
    }

    @Test
    public void testSuite() {
        // to avoid confusion of duplicated test execution output
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">>>EngineTestKit duplicating the suite>>>");
        SmartDirtiesTestsHolder.reset(ENGINE);

        var events = EngineTestKit.execute(ENGINE, request()
                .selectors(selectPackage("com.github.seregamorph.testsmartcontext.demo"))
                .build())
            .containerEvents();

        // 10 = 7 ITs + 2 UTs + 1 suite
        events.assertStatistics(stats -> stats
            .started(6)
            .succeeded(6)
            .finished(6)
            .aborted(0)
            .failed(0));

        assertEquals(List.of(
            Integration1SecondIT.class,
            Integration1IT.class,
            Integration2IT.class,
            SampleIT.class
        ), new ArrayList<>(SmartDirtiesTestsHolder.getTestClasses(ENGINE)));

        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration1SecondIT.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration1IT.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration2IT.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(SampleIT.class));

        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration1SecondIT.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration1IT.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration2IT.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(SampleIT.class));

        TestEventTracker.assertConsumedEvent("Running Unit1Test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration1SecondIT");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration1SecondIT");
        TestEventTracker.assertConsumedEvent("Running Integration1SecondIT");
        TestEventTracker.assertConsumedEvent("Running Integration1IT");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.Integration1IT");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration2IT");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration2IT");
        TestEventTracker.assertConsumedEvent("Running Integration2IT");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.Integration2IT");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.SampleIT");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.SampleIT");
        TestEventTracker.assertConsumedEvent("Running SampleIT");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.SampleIT");
        TestEventTracker.assertEmpty();

        System.out.println("<<<EngineTestKit duplicating the suite<<<");
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }
}
