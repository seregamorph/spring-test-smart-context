package com.github.seregamorph.testsmartcontext.demo;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;
import static org.testng.Assert.assertTrue;

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import com.github.seregamorph.testsmartcontext.testng.AbstractTestNGSpringIntegrationTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {
    Integration1Test.Configuration.class
})
@DirtiesContext(classMode = BEFORE_CLASS)
public class Integration1SecondTest extends AbstractTestNGSpringIntegrationTest {

    @Test
    public void test() throws Exception {
        Thread.sleep(1500L);
        TestEventTracker.trackEvent("Running " + getClass().getSimpleName());
        assertTrue(((ConfigurableApplicationContext) applicationContext).isActive(), "Context should be active");
    }
}
