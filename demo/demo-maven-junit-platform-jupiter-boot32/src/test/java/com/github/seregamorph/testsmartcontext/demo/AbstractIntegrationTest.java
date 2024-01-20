package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.SmartDirtiesContextTestExecutionListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestExecutionListeners({
    SmartDirtiesContextTestExecutionListener.class
})
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@ExtendWith(SpringExtension.class)
public abstract class AbstractIntegrationTest {

    @BeforeEach
    public void beforeMethod() {
        System.out.println("Running " + getClass());
    }
}
