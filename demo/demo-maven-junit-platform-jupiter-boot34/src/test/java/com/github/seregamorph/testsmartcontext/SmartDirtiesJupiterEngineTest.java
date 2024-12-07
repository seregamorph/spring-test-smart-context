package com.github.seregamorph.testsmartcontext;

import com.github.seregamorph.testsmartcontext.demo.ExtendWithTest;
import com.github.seregamorph.testsmartcontext.demo.Integration1Test;
import com.github.seregamorph.testsmartcontext.demo.Integration2Test;
import com.github.seregamorph.testsmartcontext.demo.NoBaseClass1IntegrationTest;
import com.github.seregamorph.testsmartcontext.demo.NoBaseClass2IntegrationTest;
import com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextAfterClassTest;
import com.github.seregamorph.testsmartcontext.demo.SampleDirtiesContextBeforeClassTest;
import com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.ClassNameFilter.excludeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

public class SmartDirtiesJupiterEngineTest {

    @Test
    public void testSuite() {
        // to avoid confusion of duplicated test execution output
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(">>>EngineTestKit duplicating the suite>>>");
        SmartDirtiesTestsHolder.reset();

        var events = EngineTestKit.execute("junit-jupiter", request()
                .selectors(selectPackage("com.github.seregamorph.testsmartcontext.demo"))
                .filters(excludeClassNamePatterns(getClass().getName()))
                .build())
            .containerEvents();

        // 10 = 7 ITs + 2 UTs + 1 suite
        events.assertStatistics(stats -> stats
            .started(11)
            .succeeded(11)
            .finished(11)
            .aborted(0)
            .failed(0));

        assertEquals(8, SmartDirtiesTestsHolder.classOrderStateMapSize());

        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration1Test.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration2Test.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(ExtendWithTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(NoBaseClass1IntegrationTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(NoBaseClass2IntegrationTest.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(SampleDirtiesContextBeforeClassTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(SampleDirtiesContextAfterClassTest.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(SampleIntegrationTest.class));

        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration1Test.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration2Test.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(ExtendWithTest.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(NoBaseClass1IntegrationTest.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(NoBaseClass2IntegrationTest.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(SampleDirtiesContextAfterClassTest.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(SampleDirtiesContextBeforeClassTest.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(SampleIntegrationTest.class));

        System.out.println("<<<EngineTestKit duplicating the suite<<<");
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }
}
