package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {
    Integration2Test.Configuration.class
})
public class Integration2Test extends AbstractIntegrationTest {

    @Test
    public void test() {
        TestEventTracker.trackEvent("Running " + getClass().getSimpleName() + ".test");
    }

    @Nested
    public class NestedTest {

        @Test
        public void nested() {
            TestEventTracker.trackEvent("Running " + getClass().getSimpleName() + ".test");
        }
    }

    public static class Configuration {

    }
}
