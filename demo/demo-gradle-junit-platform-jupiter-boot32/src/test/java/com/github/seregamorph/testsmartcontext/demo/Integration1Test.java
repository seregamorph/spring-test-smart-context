package com.github.seregamorph.testsmartcontext.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@ContextConfiguration(classes = {
    Integration1Test.Configuration.class
})
public class Integration1Test extends AbstractIntegrationTest {

    @Autowired
    private SampleBean rootBean;

    @Test
    public void test() {
        System.out.println("Integration1Test.test " + rootBean);
    }

    @Import(SampleBean.class)
    public static class Configuration {

    }
}
