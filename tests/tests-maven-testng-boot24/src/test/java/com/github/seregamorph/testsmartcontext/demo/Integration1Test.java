package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import com.github.seregamorph.testsmartcontext.testng.AbstractTestNGSpringIntegrationTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {
    Integration1Test.Configuration.class
})
public class Integration1Test extends AbstractTestNGSpringIntegrationTest {

    @Test
    public void test() {
        TestEventTracker.trackEvent("Running " + getClass().getSimpleName());
    }

    public static class Configuration {

        @Bean
        public String slowlyCreatedBean() throws Exception {
            TestEventTracker.trackEvent("Creating slowlyCreatedBean");
            Thread.sleep(1000L);
            TestEventTracker.trackEvent("Created slowlyCreatedBean");
            return "slowlyCreatedBean";
        }
    }
}
