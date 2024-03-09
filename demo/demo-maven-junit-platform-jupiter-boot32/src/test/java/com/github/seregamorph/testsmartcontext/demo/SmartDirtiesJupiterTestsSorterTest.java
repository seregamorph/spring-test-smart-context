package com.github.seregamorph.testsmartcontext.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSorter;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SmartDirtiesJupiterTestsSorterTest {

    @Test
    public void shouldSortAlphabeticallyAndGroupSameConfigurations() {
        SmartDirtiesTestsSorter sorter = SmartDirtiesTestsSorter.getInstance();
        System.out.println(">>>shouldSortAlphabeticallyAndGroupSameConfigurations>>>");
        var testItems = Arrays.asList(
            Integration2Test.class,
            SampleIntegrationTest.class,
            Integration1Test.class,
            Unit1Test.class,
            NoBaseClass2IntegrationTest.class,
            NoBaseClass1IntegrationTest.class,
            SmartDirtiesJupiterTestsSorterTest.class
        );
        var itClassesLists = sorter.sort(testItems, testClass -> testClass);
        System.out.println("<<<shouldSortAlphabeticallyAndGroupSameConfigurations<<<");

        assertEquals(Arrays.asList(
            // UT
            SmartDirtiesJupiterTestsSorterTest.class,
            Unit1Test.class,
            // IT 1
            Integration1Test.class,
            // IT 2
            Integration2Test.class,
            // IT 3
            NoBaseClass1IntegrationTest.class,
            NoBaseClass2IntegrationTest.class,
            // IT 4
            SampleIntegrationTest.class
        ), testItems);

        assertEquals(List.of(
            List.of(Integration1Test.class),
            List.of(Integration2Test.class),
            List.of(NoBaseClass1IntegrationTest.class, NoBaseClass2IntegrationTest.class),
            List.of(SampleIntegrationTest.class)
        ), itClassesLists);
    }
}
