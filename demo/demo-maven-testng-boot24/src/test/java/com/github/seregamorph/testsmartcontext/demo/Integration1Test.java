package com.github.seregamorph.testsmartcontext.demo;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {
    Integration1Test.Configuration.class
})
public class Integration1Test extends AbstractTestNGSpringContextTests {

    @Test
    public void test() {
    }

    public static class Configuration {

    }
}
