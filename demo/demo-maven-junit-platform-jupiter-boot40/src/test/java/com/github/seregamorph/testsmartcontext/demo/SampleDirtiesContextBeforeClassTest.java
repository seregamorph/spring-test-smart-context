package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = SampleIntegrationTest.Configuration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SampleDirtiesContextBeforeClassTest {

    @Test
    public void test() {
        TestEventTracker.trackEvent("Running " + getClass().getSimpleName() + ".test");
    }

    @AfterAll
    public static void afterAll() {
        TestEventTracker.trackEvent("AfterAll " + SampleDirtiesContextBeforeClassTest.class.getSimpleName());
    }
}
