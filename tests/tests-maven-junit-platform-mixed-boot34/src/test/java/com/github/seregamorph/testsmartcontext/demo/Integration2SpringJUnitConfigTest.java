package com.github.seregamorph.testsmartcontext.demo;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

// JUnit 5 Jupiter
@SpringJUnitConfig(classes = {
    Integration2SpringJUnitConfigTest.Configuration.class
})
public class Integration2SpringJUnitConfigTest {

    @Test
    public void test() {
        System.out.println("Integration2SpringJUnitConfigTest.test");
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
