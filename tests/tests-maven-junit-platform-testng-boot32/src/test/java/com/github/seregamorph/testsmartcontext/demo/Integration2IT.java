package com.github.seregamorph.testsmartcontext.demo;

import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {
    Integration2IT.Configuration.class
})
public class Integration2IT extends AbstractIT {

    @Test
    public void test() {
    }

    public static class Configuration {

    }
}
