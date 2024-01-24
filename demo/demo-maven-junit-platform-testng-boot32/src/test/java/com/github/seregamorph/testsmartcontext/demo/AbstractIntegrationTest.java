package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.testng.AbstractTestNGSpringIntegrationTest;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractIntegrationTest extends AbstractTestNGSpringIntegrationTest {

    @BeforeMethod
    public void beforeMethod() {
        System.out.println("Running " + getClass());
    }
}
