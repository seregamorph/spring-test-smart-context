package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;

@SpringBootTest(classes = SampleIntegrationTest.Configuration.class)
@ExtendWith(OutputCaptureExtension.class)
public class ExtendWithTest {

    @Test
    public void test() {
        TestEventTracker.trackEvent("Running " + getClass().getSimpleName() + ".test");
    }

    @AfterAll
    public static void afterAll() {
        TestEventTracker.trackEvent("AfterAll " + ExtendWithTest.class.getSimpleName());
    }
}
