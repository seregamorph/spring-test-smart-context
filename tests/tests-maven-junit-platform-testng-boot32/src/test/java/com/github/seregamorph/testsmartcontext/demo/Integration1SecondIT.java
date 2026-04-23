package com.github.seregamorph.testsmartcontext.demo;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;
import static org.testng.Assert.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {
    Integration1IT.Configuration.class
})
@DirtiesContext(classMode = BEFORE_CLASS)
public class Integration1SecondIT extends AbstractIT {

    @Autowired
    private String string;

    @Test
    public void test() {
        assertEquals(string, "value1");
    }
}
