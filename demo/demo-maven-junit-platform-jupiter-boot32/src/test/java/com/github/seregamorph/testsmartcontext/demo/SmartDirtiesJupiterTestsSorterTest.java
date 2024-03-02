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
        List<TestItem> testItems = Arrays.asList(
            new TestItem(Integration2Test.class),
            new TestItem(SampleIntegrationTest.class),
            new TestItem(Integration1Test.class),
            new TestItem(Unit1Test.class),
            new TestItem(NoBaseClass2IntegrationTest.class),
            new TestItem(NoBaseClass1IntegrationTest.class),
            new TestItem(SmartDirtiesJupiterTestsSorterTest.class)
        );
        var itClassesLists = sorter.sort(testItems, testItem -> testItem.testClass);
        System.out.println("<<<shouldSortAlphabeticallyAndGroupSameConfigurations<<<");

        assertEquals(List.of(
            List.of(Integration1Test.class),
            List.of(Integration2Test.class),
            List.of(NoBaseClass1IntegrationTest.class, NoBaseClass2IntegrationTest.class),
            List.of(SampleIntegrationTest.class)
        ), itClassesLists);
    }

    private record TestItem(Class<?> testClass) {
    }
}
