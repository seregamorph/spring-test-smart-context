package com.github.seregamorph.testsmartcontext;

import java.util.Collections;
import java.util.List;

/**
 * @author Sergey Chernov
 */
public class TestSortResult {

    private final List<List<Class<?>>> sortedConfigToTests;

    TestSortResult(List<List<Class<?>>> sortedConfigToTests) {
        this.sortedConfigToTests = sortedConfigToTests;
    }

    public static TestSortResult singletonList(Class<?> testClass) {
        return new TestSortResult(Collections.singletonList(Collections.singletonList(testClass)));
    }

    public List<List<Class<?>>> getSortedConfigToTests() {
        return sortedConfigToTests;
    }
}
