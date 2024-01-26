package com.github.seregamorph.testsmartcontext;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.seregamorph.testsmartcontext.demo.Integration1Test;
import com.github.seregamorph.testsmartcontext.demo.Integration2Test;
import com.github.seregamorph.testsmartcontext.demo.NoBaseClass1IntegrationTest;
import com.github.seregamorph.testsmartcontext.demo.NoBaseClass2IntegrationTest;
import com.github.seregamorph.testsmartcontext.demo.SampleIntegrationTest;
import com.github.seregamorph.testsmartcontext.demo.Unit1Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class SmartDirtiesJupiterTestsSorterTest {

    @Test
    public void shouldSortAlphabeticallyAndGroupSameConfigurations() {
        SmartDirtiesTestsSorter sorter = new SmartDirtiesTestsSorter();
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
        Map<Class<?>, Boolean> lastClassPerConfig = sorter.sort(testItems, testItem -> testItem.testClass);
        System.out.println("<<<shouldSortAlphabeticallyAndGroupSameConfigurations<<<");

        Map<Class<?>, Boolean> expectedLastClassPerConfig = new LinkedHashMap<>();
        expectedLastClassPerConfig.put(Integration1Test.class, true);
        expectedLastClassPerConfig.put(Integration2Test.class, true);
        expectedLastClassPerConfig.put(NoBaseClass1IntegrationTest.class, false);
        expectedLastClassPerConfig.put(NoBaseClass2IntegrationTest.class, true);
        expectedLastClassPerConfig.put(SampleIntegrationTest.class, true);
        assertEquals(expectedLastClassPerConfig, lastClassPerConfig);
        assertEquals(
            new ArrayList<>(expectedLastClassPerConfig.keySet()),
            new ArrayList<>(lastClassPerConfig.keySet())
        );

        assertEquals(Arrays.asList(
            SmartDirtiesJupiterTestsSorterTest.class,
            Unit1Test.class,
            Integration1Test.class,
            Integration2Test.class,
            NoBaseClass1IntegrationTest.class,
            NoBaseClass2IntegrationTest.class,
            SampleIntegrationTest.class
        ), testItems.stream().map(testItem -> testItem.testClass).collect(Collectors.toList()));
    }

    private record TestItem(Class<?> testClass) {
    }
}
