package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.junit4.AbstractJUnit4SpringIntegrationTest;
import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

// JUnit 4
@ContextConfiguration(classes = Integration1Test.Configuration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SampleDirtiesContextBeforeClassTest extends AbstractJUnit4SpringIntegrationTest {

    @Test
    public void test() {
        TestEventTracker.trackEvent("Running " + getClass().getSimpleName() + ".test");
    }
}
