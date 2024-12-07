package com.github.seregamorph.testsmartcontext.demo;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {
    Integration2Test.Configuration.class
})
public class Integration2Test extends AbstractIntegrationTest {

    @Test
    public void test() {
        System.out.println("Integration2Test.test");
    }

    @Nested
    public class NestedTest {

        @Test
        public void nested() {
            System.out.println("Integration2Test.NestedTest.test");
        }
    }

    public static class Configuration {

    }
}
