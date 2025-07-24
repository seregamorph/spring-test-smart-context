package com.github.seregamorph.testsmartcontext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {
    SmartDirtiesTestsSupportTest.Configuration.class
})
class SmartDirtiesTestsSupportTest {

    @BeforeEach
    public void prepare() {
        SmartDirtiesTestsSupport.setTestClassesLists("SmartDirtiesTestsHolderTest",
            TestSortResult.singletonList(SmartDirtiesTestsSupportTest.class));
    }

    @Test
    public void nestedInheritShouldPass() {
        SmartDirtiesTestsSupport.verifyInnerClass(SmartDirtiesTestsSupportTest.NestedInheritTest.class);
    }

//    @Test
//    public void nestedCustomShouldFail() {
//        var ise = Assertions.assertThrows(IllegalStateException.class,
//            () -> SmartDirtiesTestsSupport.verifyInnerClass(SmartDirtiesTestsSupportTest.NestedCustomTest.class));
//        assertEquals("Nested inner class com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSupportTest$NestedCustomTest declares custom context configuration which differs from enclosing class com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSupportTest. This is not properly supported by the spring-test-smart-context ordering because of framework limitations. Please extract inner test class to upper level.", ise.getMessage());
//    }

    public static class Configuration {

    }

    @Nested
    public class NestedInheritTest {
    }

//    @Nested
//    @TestPropertySource(properties = {
//        "parameter = value"
//    })
//    public class NestedCustomTest {
//    }
}
