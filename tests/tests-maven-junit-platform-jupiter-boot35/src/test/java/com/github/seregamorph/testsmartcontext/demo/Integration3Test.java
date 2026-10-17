package com.github.seregamorph.testsmartcontext.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {
    Integration2Test.Configuration.class,
    Integration1Test.Configuration.class
})
public class Integration3Test extends AbstractIntegrationTest {

    @Autowired
    private SampleService sampleService;

    @Test
    public void test() {
        System.out.println("Integration3Test.test " + sampleService);
    }
}
