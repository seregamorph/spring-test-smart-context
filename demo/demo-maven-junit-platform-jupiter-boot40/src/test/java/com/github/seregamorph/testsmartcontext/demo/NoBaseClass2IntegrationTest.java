package com.github.seregamorph.testsmartcontext.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SampleIntegrationTest.Configuration.class)
public class NoBaseClass2IntegrationTest {

    @Test
    public void test() {
        System.out.println("Running " + getClass().getName() + ".test");
    }
}
