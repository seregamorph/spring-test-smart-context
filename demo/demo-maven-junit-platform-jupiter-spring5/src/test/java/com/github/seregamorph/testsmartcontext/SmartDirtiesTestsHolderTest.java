package com.github.seregamorph.testsmartcontext;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

class SmartDirtiesTestsHolderTest {

    @Test
    public void nestedInheritShouldPass() {
        SmartDirtiesTestsHolder.verifyInnerClass(TestRootTest.NestedInheritTest.class);
    }

    @Test
    public void nestedCustomShouldFail() {
        var ise = Assertions.assertThrows(IllegalStateException.class,
            () -> SmartDirtiesTestsHolder.verifyInnerClass(TestRootTest.NestedCustomTest.class));
        assertEquals("Nested inner class com.github.seregamorph.testsmartcontext.TestRootTest$NestedCustomTest " +
            "declares custom context configuration which differs from enclosing class com.github.seregamorph" +
            ".testsmartcontext.TestRootTest. This is not properly supported by the spring-test-smart-context ordering" +
            " because of framework limitations. Please extract inner test class to upper level.", ise.getMessage());
    }
}

@ContextConfiguration(classes = {
    TestRootTest.Configuration.class
})
class TestRootTest {

    public static class Configuration {

    }

    @Nested
    public class NestedInheritTest {
    }

    @Nested
    @TestPropertySource(properties = {
        "parameter = value"
    })
    public class NestedCustomTest {

    }
}
