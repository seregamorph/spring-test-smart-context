package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import com.github.seregamorph.testsmartcontext.testng.AbstractTestNGSpringIntegrationTest;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractIT extends AbstractTestNGSpringIntegrationTest {

    @BeforeMethod
    public void beforeMethod() {
        TestEventTracker.trackEvent("Running " + getClass().getSimpleName());
    }
}
