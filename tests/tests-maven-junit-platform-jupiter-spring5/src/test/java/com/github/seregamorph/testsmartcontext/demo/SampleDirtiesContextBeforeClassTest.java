package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@ContextConfiguration(classes = SampleIntegrationTest.Configuration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SampleDirtiesContextBeforeClassTest extends AbstractIntegrationTest {

    @Test
    public void test() {
        TestEventTracker.trackEvent("Running " + getClass().getSimpleName() + ".test");
    }
}
