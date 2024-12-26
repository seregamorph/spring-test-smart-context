package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.junit4.AbstractJUnit4SpringIntegrationTest;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

// JUnit 4
@ContextConfiguration(classes = {
    Integration2Test.Configuration.class
})
public class Integration2Test extends AbstractJUnit4SpringIntegrationTest {

    @Test
    public void test() {
        System.out.println("Integration2Test.test");
    }

    public static class Configuration {

    }
}
