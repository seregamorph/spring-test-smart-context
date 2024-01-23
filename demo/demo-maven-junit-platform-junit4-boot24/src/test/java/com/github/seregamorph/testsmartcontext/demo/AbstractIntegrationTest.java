package com.github.seregamorph.testsmartcontext.demo;

import org.junit.Before;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

public abstract class AbstractIntegrationTest extends AbstractJUnit4SpringContextTests {

    @Before
    public void beforeMethod() {
        System.out.println("Running " + getClass());
    }
}
