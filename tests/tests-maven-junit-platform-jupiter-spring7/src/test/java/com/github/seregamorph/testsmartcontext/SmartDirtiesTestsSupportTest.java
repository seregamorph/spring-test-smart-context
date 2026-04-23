package com.github.seregamorph.testsmartcontext;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

class SmartDirtiesTestsSupportTest {

    @BeforeEach
    public void prepare() {
        SmartDirtiesTestsSupport.setTestClassesLists("SmartDirtiesTestsHolderTest",
            TestSortResult.singletonList(TestRootTest.class));
    }

    @Test
    public void nestedInheritShouldPass() {
        SmartDirtiesTestsSupport.verifyInnerClass(TestRootTest.NestedInheritTest.class);
    }

    @Test
    public void nestedCustomShouldFail() {
        var ise = Assertions.assertThrows(IllegalStateException.class,
            () -> SmartDirtiesTestsSupport.verifyInnerClass(TestRootTest.NestedCustomTest.class));
        assertEquals("Nested inner class com.github.seregamorph.testsmartcontext.TestRootTest$NestedCustomTest " +
            "declares custom context configuration which differs from enclosing class com.github.seregamorph" +
            ".testsmartcontext.TestRootTest. This is not properly supported by the spring-test-smart-context ordering" +
            " because of framework limitations. Please extract inner test class to upper level.", ise.getMessage());
    }
}

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    TestRootTest.Configuration.class
})
class TestRootTest {

    public static class Configuration {

    }

    @Nested // not marking as nested to avoid Maven failure since JUnit 6
    public class NestedInheritTest {
    }

    // @Nested // not marking as nested to avoid Maven failure since JUnit 6
    @TestPropertySource(properties = {
        "parameter = value"
    })
    public class NestedCustomTest {

    }
}
