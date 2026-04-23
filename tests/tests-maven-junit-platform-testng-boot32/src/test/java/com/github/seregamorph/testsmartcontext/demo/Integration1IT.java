package com.github.seregamorph.testsmartcontext.demo;

import static org.testng.Assert.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {
    Integration1IT.Configuration.class
})
public class Integration1IT extends AbstractIT {

    @Autowired
    private String string;

    @Test
    public void test() {
        assertEquals(string, "value1");
    }

    public static class Configuration {

        @Bean
        public String string() {
            return "value1";
        }
    }
}
