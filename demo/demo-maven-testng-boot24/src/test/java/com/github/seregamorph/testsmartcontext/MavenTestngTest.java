package com.github.seregamorph.testsmartcontext;

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class MavenTestngTest {

    @BeforeSuite
    public static void beforeSuite() {
        TestEventTracker.startTracking();
    }

    @AfterSuite
    public static void afterSuite() {
        // hint: this test can fail if executed with test filtering (like run single test)
        TestEventTracker.assertConsumedEvent("Running Unit1Test.test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration1SecondTest");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration1SecondTest");
        TestEventTracker.assertConsumedEvent("Running Integration1SecondTest");
        TestEventTracker.assertConsumedEvent("Running Integration1Test");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.Integration1Test");
        TestEventTracker.assertConsumedEvent("Creating context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Created context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertConsumedEvent("Running Integration2Test");
        TestEventTracker.assertConsumedEvent("Destroying context for com.github.seregamorph.testsmartcontext.demo.Integration2Test");
        TestEventTracker.assertEmpty();

        TestEventTracker.stopTracking();
    }
}
