package com.github.seregamorph.testsmartcontext.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSorter;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SmartDirtiesJupiterTestsSorterTest {

    @Test
    public void shouldSortMostlyAlphabeticallyAndGroupSameConfigurations() {
        // mostly: @DirtiesContext should go last.
        SmartDirtiesTestsSorter sorter = SmartDirtiesTestsSorter.getInstance();
        System.out.println(">>>shouldSortMostlyAlphabeticallyAndGroupSameConfigurations>>>");
        var testItems = Arrays.asList(
            Integration2Test.class,
            SampleIntegrationTest.class,
            Integration1Test.class,
            Unit1Test.class,
            SampleDirtiesContextAfterClassTest.class,
            SampleDirtiesContextBeforeClassTest.class,
            NoBaseClass2IntegrationTest.class,
            NoBaseClass1IntegrationTest.class,
            ExtendWithTest.class,
            SmartDirtiesJupiterTestsSorterTest.class
        );
        var itClassesLists = sorter.sort(testItems, testClass -> testClass);
        System.out.println("<<<shouldSortMostlyAlphabeticallyAndGroupSameConfigurations<<<");

        assertEquals(Arrays.asList(
            // UT
            SmartDirtiesJupiterTestsSorterTest.class,
            Unit1Test.class,
            // IT 1
            SampleDirtiesContextBeforeClassTest.class,
            ExtendWithTest.class,
            NoBaseClass1IntegrationTest.class,
            NoBaseClass2IntegrationTest.class,
            SampleDirtiesContextAfterClassTest.class,
            // IT 2
            Integration1Test.class,
            // IT 3
            Integration2Test.class,
            // IT 4
            SampleIntegrationTest.class
        ), testItems);

        assertEquals(List.of(
            List.of(SampleDirtiesContextBeforeClassTest.class, ExtendWithTest.class,
                NoBaseClass1IntegrationTest.class, NoBaseClass2IntegrationTest.class,
                SampleDirtiesContextAfterClassTest.class),
            List.of(Integration1Test.class),
            List.of(Integration2Test.class),
            List.of(SampleIntegrationTest.class)
        ), itClassesLists);
    }

    @Test
    public void test() {
        System.out.println("Running " + getClass() + ".test");
    }
}
