package com.github.seregamorph.testsmartcontext.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public abstract class AbstractIntegrationTest {

    @BeforeEach
    public void beforeMethod() {
        System.out.println("Running " + getClass());
    }
}
