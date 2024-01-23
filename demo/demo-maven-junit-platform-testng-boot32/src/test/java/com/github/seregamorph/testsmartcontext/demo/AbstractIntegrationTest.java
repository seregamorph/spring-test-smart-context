package com.github.seregamorph.testsmartcontext.demo;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractIntegrationTest extends AbstractTestNGSpringContextTests {

    @BeforeMethod
    public void beforeMethod() {
        System.out.println("Running " + getClass());
    }
}
