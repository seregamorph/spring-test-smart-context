package com.github.seregamorph.testsmartcontext.demo;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@ContextConfiguration(classes = SampleIntegrationTest.Configuration.class)
@DirtiesContext
public class SampleDirtiesContextAfterClassTest extends AbstractIntegrationTest {

    @Test
    public void test() {
        System.out.println("Running " + getClass().getName() + ".test");
    }
}
