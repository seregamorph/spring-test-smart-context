package com.github.seregamorph.testsmartcontext.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;

@SpringBootTest(classes = SampleIntegrationTest.Configuration.class)
@ExtendWith(OutputCaptureExtension.class)
public class ExtendWithTest {
    @Test
    public void test() {
    }
}
