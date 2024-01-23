package com.github.seregamorph.testsmartcontext.demo;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {
    Integration2Test.Configuration.class
})
public class Integration2Test extends AbstractTestNGSpringContextTests {

    @Test
    public void test() {
    }

    public static class Configuration {

    }
}
