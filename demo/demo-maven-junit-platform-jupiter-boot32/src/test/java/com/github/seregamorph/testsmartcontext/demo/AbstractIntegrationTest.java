package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.jupiter.AbstractJUnitSpringIntegrationTest;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractIntegrationTest extends AbstractJUnitSpringIntegrationTest {

    @BeforeEach
    public void beforeMethod() {
        System.out.println("Running " + getClass());
    }
}
