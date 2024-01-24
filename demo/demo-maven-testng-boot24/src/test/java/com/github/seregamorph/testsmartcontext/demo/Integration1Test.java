package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.testng.AbstractTestNGSpringIntegrationTest;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {
    Integration1Test.Configuration.class
})
public class Integration1Test extends AbstractTestNGSpringIntegrationTest {

    @Test
    public void test() {
    }

    public static class Configuration {

    }
}
