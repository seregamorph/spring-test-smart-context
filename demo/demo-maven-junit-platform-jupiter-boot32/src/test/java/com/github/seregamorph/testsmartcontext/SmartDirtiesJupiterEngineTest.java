package com.github.seregamorph.testsmartcontext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.ClassNameFilter.excludeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import com.github.seregamorph.testsmartcontext.demo.DirtiesContextTest;
import com.github.seregamorph.testsmartcontext.demo.ExtendWithTest;
import com.github.seregamorph.testsmartcontext.demo.Integration1Test;
import com.github.seregamorph.testsmartcontext.demo.Integration2Test;
import com.github.seregamorph.testsmartcontext.demo.NoBaseClass1IntegrationTest;
import com.github.seregamorph.testsmartcontext.demo.NoBaseClass2IntegrationTest;
import com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

public class SmartDirtiesJupiterEngineTest {

    @Test
    public void testSuite() {
        var events = EngineTestKit.execute("junit-jupiter", request()
                .selectors(selectPackage("com.github.seregamorph.testsmartcontext.demo"))
                .filters(excludeClassNamePatterns(getClass().getName()))
                .build())
            .containerEvents();

        // 10 = 7 ITs + 2 UTs + 1 suite
        events.assertStatistics(stats -> stats
            .started(10)
            .succeeded(10)
            .finished(10)
            .aborted(0)
            .failed(0));

        assertEquals(7, SmartDirtiesTestsHolder.classOrderStateMapSize());

        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration1Test.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(Integration2Test.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(ExtendWithTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(NoBaseClass1IntegrationTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(NoBaseClass2IntegrationTest.class));
        assertFalse(SmartDirtiesTestsHolder.isFirstClassPerConfig(DirtiesContextTest.class));
        assertTrue(SmartDirtiesTestsHolder.isFirstClassPerConfig(SampleIntegrationTest.class));

        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration1Test.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(Integration2Test.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(ExtendWithTest.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(NoBaseClass1IntegrationTest.class));
        assertFalse(SmartDirtiesTestsHolder.isLastClassPerConfig(NoBaseClass2IntegrationTest.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(DirtiesContextTest.class));
        assertTrue(SmartDirtiesTestsHolder.isLastClassPerConfig(SampleIntegrationTest.class));
    }
}
