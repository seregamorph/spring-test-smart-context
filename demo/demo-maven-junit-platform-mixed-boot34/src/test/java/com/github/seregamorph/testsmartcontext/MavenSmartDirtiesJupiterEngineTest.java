package com.github.seregamorph.testsmartcontext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.ClassNameFilter.excludeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import com.github.seregamorph.testsmartcontext.demo.ExtendWithTest;
import com.github.seregamorph.testsmartcontext.demo.Integration2SpringJUnitConfigTest;
import com.github.seregamorph.testsmartcontext.demo.NoBaseClass1IntegrationTest;
import com.github.seregamorph.testsmartcontext.demo.NoBaseClass2IntegrationTest;
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
                .filters(excludeClassNamePatterns(getClass().getName()))
                .build())
            .containerEvents();

        // 7 = 5 ITs + 1 UTs + 1 suite
        events.assertStatistics(stats -> stats
            .started(7)
            .succeeded(7)
            .finished(7)
            .aborted(0)
            .failed(0));

        assertEquals(List.of(
            ExtendWithTest.class,
            NoBaseClass1IntegrationTest.class,
            NoBaseClass2IntegrationTest.class,
            Integration2SpringJUnitConfigTest.class,
            SampleIntegrationTest.class
        ), new ArrayList<>(SmartDirtiesTestsHolder.getTestClasses(ENGINE)));

        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(ExtendWithTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(NoBaseClass1IntegrationTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(NoBaseClass2IntegrationTest.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration2SpringJUnitConfigTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration2SpringJUnitConfigTest.NestedTest.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(SampleIntegrationTest.class));

        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(ExtendWithTest.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(NoBaseClass1IntegrationTest.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(NoBaseClass2IntegrationTest.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration2SpringJUnitConfigTest.NestedTest.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration2SpringJUnitConfigTest.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(SampleIntegrationTest.class));

        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.ExtendWithTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.ExtendWithTest");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.NoBaseClass2IntegrationTest");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration2SpringJUnitConfigTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration2SpringJUnitConfigTest");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.Integration2SpringJUnitConfigTest");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest");
        TestEventTracker.assertEmpty();

        System.out.println("<<<EngineTestKit duplicating the suite<<<");
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }
}