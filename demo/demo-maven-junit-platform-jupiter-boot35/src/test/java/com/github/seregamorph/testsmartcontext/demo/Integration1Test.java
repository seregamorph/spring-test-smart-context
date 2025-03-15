package com.github.seregamorph.testsmartcontext.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Nested;
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

    @Nested
    public class NestedTest {

        @Autowired
        private SampleBean nestedBean;

        @Test
        public void nested() {
            System.out.println("Integration1Test.NestedTest.test " + nestedBean);
        }

        @Nested
        public class DeeplyNestedTest {

            @Autowired
            private SampleService sampleService;

            @Test
            public void deeplyNested() {
                assertEquals("default", sampleService.getValue());
                System.out.println("Integration1Test.NestedTest.DeeplyNestedTest.deeplyNested " + sampleService);
            }
        }
    }

    @Import({
        SampleBean.class,
        SampleService.class
    })
    public static class Configuration {

    }
}
