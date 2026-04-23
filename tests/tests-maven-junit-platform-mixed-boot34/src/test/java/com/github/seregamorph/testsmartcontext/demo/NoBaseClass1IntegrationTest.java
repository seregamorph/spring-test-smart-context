package com.github.seregamorph.testsmartcontext.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// JUnit 5 Jupiter
@SpringBootTest(classes = SampleIntegrationTest.Configuration.class)
public class NoBaseClass1IntegrationTest {

    @Test
    public void test() {
        System.out.println("Running " + getClass().getName() + ".test");
    }
}
