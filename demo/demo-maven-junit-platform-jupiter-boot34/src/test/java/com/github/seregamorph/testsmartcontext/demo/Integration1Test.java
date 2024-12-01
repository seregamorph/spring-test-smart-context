package com.github.seregamorph.testsmartcontext.demo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@ContextConfiguration(classes = {
    Integration1Test.Configuration.class
})
public class Integration1Test extends AbstractIntegrationTest {

    @BeforeAll
    public static void beforeClass() {
        System.out.println("Integration1Test beforeClass");
    }

    @AfterAll
    public static void afterClass() {
        System.out.println("Integration1Test afterClass");
    }

    @Test
    public void test() {
        System.out.println("Integration1Test.test");
    }

    @Nested
    public class NestedTest {

        @Test
        public void nested() {
            System.out.println("Integration1Test.NestedTest.test");
        }
    }

    public static class Configuration {

    }
}
