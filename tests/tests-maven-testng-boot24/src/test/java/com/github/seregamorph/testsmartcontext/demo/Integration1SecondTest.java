package com.github.seregamorph.testsmartcontext.demo;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import com.github.seregamorph.testsmartcontext.testng.AbstractTestNGSpringIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {
    Integration1Test.Configuration.class
})
@DirtiesContext(classMode = BEFORE_CLASS)
public class Integration1SecondTest extends AbstractTestNGSpringIntegrationTest {

    @Test
    public void test() {
        TestEventTracker.trackEvent("Running " + getClass().getSimpleName());
    }
}
