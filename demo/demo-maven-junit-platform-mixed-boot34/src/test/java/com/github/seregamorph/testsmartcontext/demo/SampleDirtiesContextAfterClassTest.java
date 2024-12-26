package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.testng.AbstractTestNGSpringIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

// TestNG
@ContextConfiguration(classes = Integration1Test.Configuration.class)
@DirtiesContext
public class SampleDirtiesContextAfterClassTest extends AbstractTestNGSpringIntegrationTest {

    @Test
    public void test() {
        System.out.println("Running " + getClass().getName() + ".test");
    }
}
