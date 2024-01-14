package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.testng.AbstractTestNGSpringIntegrationTest;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {
    Integration2Test.Configuration.class
})
public class Integration2Test extends AbstractTestNGSpringIntegrationTest {

    @Test
    public void test() {
    }

    public static class Configuration {

    }
}
