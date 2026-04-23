package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SampleIntegrationTest.Configuration.class)
public class NoBaseClass1IntegrationTest {

    @Test
    public void test() {
        TestEventTracker.trackEvent("Running " + getClass().getSimpleName() + ".test");
    }

    @AfterAll
    public static void afterAll() {
        TestEventTracker.trackEvent("AfterAll " + NoBaseClass1IntegrationTest.class.getSimpleName());
    }
}
