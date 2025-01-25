package com.github.seregamorph.testsmartcontext.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSorter;
import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SmartDirtiesJupiterTestsSorterTest {

    @Test
    public void shouldSortMostlyAlphabeticallyAndGroupSameConfigurations() {
        TestEventTracker.trackEvent("Started " + getClass().getSimpleName() +
            ".shouldSortMostlyAlphabeticallyAndGroupSameConfigurations");
        // mostly: @DirtiesContext should go last.
        SmartDirtiesTestsSorter sorter = SmartDirtiesTestsSorter.getInstance();
        var testItems = Arrays.asList(
            Integration2Test.class,
            SampleIntegrationTest.class,
            Integration1Test.class,
            Unit1Test.class,
            SampleDirtiesContextAfterClassTest.class,
            SampleDirtiesContextBeforeClassTest.class,
            SmartDirtiesJupiterTestsSorterTest.class
        );
        var itClassesLists = sorter.sort(testItems, testClass -> testClass);

        assertEquals(Arrays.asList(
            // UT
            SmartDirtiesJupiterTestsSorterTest.class,
            Unit1Test.class,
            // IT 1
            Integration1Test.class,
            // IT 2
            Integration2Test.class,
            // IT 3
            SampleDirtiesContextBeforeClassTest.class,
            SampleDirtiesContextAfterClassTest.class,
            // IT 4
            SampleIntegrationTest.class
        ), testItems);

        assertEquals(List.of(
            List.of(Integration1Test.class),
            List.of(Integration2Test.class),
            List.of(SampleDirtiesContextBeforeClassTest.class,
                SampleDirtiesContextAfterClassTest.class),
            List.of(SampleIntegrationTest.class)
        ), itClassesLists);

        TestEventTracker.trackEvent("Finished " + getClass().getSimpleName() +
            ".shouldSortMostlyAlphabeticallyAndGroupSameConfigurations");
    }
}
