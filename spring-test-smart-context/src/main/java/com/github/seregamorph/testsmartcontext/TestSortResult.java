package com.github.seregamorph.testsmartcontext;

import static java.util.Collections.emptySet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Sergey Chernov
 */
public class TestSortResult {

    private final IntegrationTestFilter integrationTestFilter;
    private final List<List<Class<?>>> sortedConfigToTests;
    private final Set<Class<?>> nonItClasses;

    TestSortResult(IntegrationTestFilter integrationTestFilter, List<List<Class<?>>> sortedConfigToTests, Set<Class<?>> nonItClasses) {
        this.integrationTestFilter = integrationTestFilter;
        this.sortedConfigToTests = sortedConfigToTests;
        this.nonItClasses = nonItClasses;
    }

    public static TestSortResult singletonList(Class<?> testClass) {
        // This single test is either integration or not, it will be checked only in case if it's integration.
        // So we can skip IntegrationTestFilter
        return new TestSortResult(new IntegrationTestFilter.NoOpIntegrationTestFilter(),
            Collections.singletonList(Collections.singletonList(testClass)), emptySet());
    }

    public IntegrationTestFilter getIntegrationTestFilter() {
        return integrationTestFilter;
    }

    public List<List<Class<?>>> getSortedConfigToTests() {
        return sortedConfigToTests;
    }

    public Set<Class<?>> getNonItClasses() {
        return nonItClasses;
    }

    @Override
    public String toString() {
        return "TestSortResult{" +
            "sortedConfigToTests=" + sortedConfigToTests +
            ", nonItClasses=" + nonItClasses +
            '}';
    }
}
