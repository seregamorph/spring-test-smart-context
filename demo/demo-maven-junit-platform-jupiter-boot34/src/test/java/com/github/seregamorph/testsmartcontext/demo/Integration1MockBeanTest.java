package com.github.seregamorph.testsmartcontext.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.github.seregamorph.testsmartcontext.mockbean.SmartMockBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@ContextConfiguration(classes = {
    Integration1Test.Configuration.class
})
public class Integration1MockBeanTest extends AbstractIntegrationTest {

    @Autowired
    private SampleService sampleService;

    @SmartMockBean
    private SampleBean sampleBean;

    @BeforeEach
    public void setUp() {
        reset(sampleBean);
        when(sampleBean.getValue()).thenReturn("mock");
    }

    @Test
    public void test() {
        assertEquals("mock", sampleBean.getValue());
    }
}
