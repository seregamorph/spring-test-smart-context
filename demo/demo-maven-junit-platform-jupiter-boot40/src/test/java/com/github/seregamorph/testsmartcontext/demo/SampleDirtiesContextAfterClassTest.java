package com.github.seregamorph.testsmartcontext.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = SampleIntegrationTest.Configuration.class)
@DirtiesContext
public class SampleDirtiesContextAfterClassTest {

    @Test
    public void test() {
        System.out.println("Running " + getClass().getName() + ".test");
    }
}
