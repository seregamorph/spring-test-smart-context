package com.github.seregamorph.testsmartcontext.demo;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {
    Integration2Test.Configuration.class
})
public class Integration2SpringJUnitConfigTest {

    @Test
    public void test() {
        System.out.println("Integration2SpringJUnitConfigTest.test");
    }

}
