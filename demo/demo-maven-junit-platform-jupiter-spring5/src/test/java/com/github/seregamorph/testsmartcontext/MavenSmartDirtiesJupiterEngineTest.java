package com.github.seregamorph.testsmartcontext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import com.github.seregamorph.testsmartcontext.demo.Integration1Test;
import com.github.seregamorph.testsmartcontext.demo.Integration2Test;
import com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextAfterClassTest;
import com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextBeforeClassTest;
import com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest;
import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

public class MavenSmartDirtiesJupiterEngineTest {

    private static final String ENGINE = "junit-jupiter";

    @BeforeEach
    public void before() {
        TestEventTracker.startTracking();
    }

    @AfterEach
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
            .started(11)
            .succeeded(11)
            .finished(11)
            .aborted(0)
            .failed(0));

        assertEquals(List.of(
            Integration1Test.class,
            Integration2Test.class,
            SampleDirtiesContextBeforeClassTest.class,
            SampleDirtiesContextAfterClassTest.class,
            SampleIntegrationTest.class
        ), new ArrayList<>(TestSmartDirtiesTestsHolder.getIntegrationTestClasses(ENGINE)));

        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration1Test.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration1Test.NestedTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration1Test.NestedTest.DeeplyNestedTest.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration2Test.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(SampleDirtiesContextBeforeClassTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(SampleDirtiesContextAfterClassTest.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(SampleIntegrationTest.class));

        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration1Test.NestedTest.DeeplyNestedTest.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration1Test.NestedTest.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration1Test.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration2Test.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(SampleDirtiesContextBeforeClassTest.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(SampleDirtiesContextAfterClassTest.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(SampleIntegrationTest.class));

        TestEventTracker.assertConsumedEvent("Started SmartDirtiesJupiterTestsSorterTest.shouldSortMostlyAlphabeticallyAndGroupSameConfigurations");
        TestEventTracker.assertConsumedEvent("Finished SmartDirtiesJupiterTestsSorterTest.shouldSortMostlyAlphabeticallyAndGroupSameConfigurations");
        TestEventTracker.assertConsumedEvent("Running com.github.seregamorph.testsmartcontext.demo.Unit1Test.test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextBeforeClassTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextBeforeClassTest");
        TestEventTracker.assertConsumedEvent("Running SampleDirtiesContextBeforeClassTest.test");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextAfterClassTest");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertEmpty();

        System.out.println("<<<EngineTestKit duplicating the suite<<<");
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }
}
