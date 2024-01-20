package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.junit4.AbstractJUnit4SpringIntegrationTest;
import org.junit.Before;

public abstract class AbstractIntegrationTest extends AbstractJUnit4SpringIntegrationTest {

    @Before
    public void beforeMethod() {
        System.out.println("Running " + getClass());
    }
}
