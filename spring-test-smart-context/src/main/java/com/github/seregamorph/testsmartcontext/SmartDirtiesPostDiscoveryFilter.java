package com.github.seregamorph.testsmartcontext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;
import org.junit.vintage.engine.descriptor.VintageTestDescriptor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Auto-discovered JUnit platform {@link PostDiscoveryFilter} which reorders and groups integration test classes
 * according to their configuration. Note: this class sorts only JUnit 4 tests executed via
 * <a href="https://junit.org/junit5/docs/current/user-guide/#migrating-from-junit4-running">vintage-engine</a>.
 * <p>
 * For TestNG test classes - see {@link com.github.seregamorph.testsmartcontext.testng.SmartDirtiesSuiteListener}, For
 * Jupiter test classes - see {@link com.github.seregamorph.testsmartcontext.jupiter.SmartDirtiesClassOrderer},
 */
public class SmartDirtiesPostDiscoveryFilter implements PostDiscoveryFilter {

    @Override
    public FilterResult apply(TestDescriptor testDescriptor) {
        List<TestDescriptor> childrenToReorder = testDescriptor.getChildren().stream()
            .filter(childTestDescriptor -> {
                // If it is a testng-engine running TestNG test, rely on SmartDirtiesSuiteListener, because
                // TestNG will alphabetically reorder it first anyway.
                // Jupiter engine has its own sorting via SmartDirtiesClassOrderer, so skip them as well.
                // Reorder only JUnit4 here:
                return getTestClassOrNull(childTestDescriptor) != null;
            })
            .collect(Collectors.toList());

        if (childrenToReorder.isEmpty()) {
            return FilterResult.included("Empty list");
        }

        Set<Class<?>> uniqueClasses = childrenToReorder.stream()
            .map(this::getTestClass)
            .collect(Collectors.toSet());
        if (uniqueClasses.size() == 1) {
            // This filter is executed several times during discover and execute phases and
            // it's not possible to distinguish them here. Sometimes per single test is sent as argument,
            // sometimes - the whole suite. If it's a suite more than 1, we can save it and never update.
            // If it's 1 - we should also distinguish single test execution.
            if (SmartDirtiesTestsHolder.lastClassPerConfigSize() <= 1) {
                Class<?> testClass = getTestClass(childrenToReorder.get(0));
                SmartDirtiesTestsHolder.setLastClassPerConfig(Collections.singletonMap(testClass, true));
            }

            // the logic here may differ for JUnit 4 via Maven vs IntelliJ:
            // Maven calls this filter several times (first per each test, then with all tests)
            return FilterResult.included("Skipping single element");
        }

        childrenToReorder.forEach(testDescriptor::removeChild);

        SmartDirtiesTestsSorter sorter = SmartDirtiesTestsSorter.getInstance();
        Map<Class<?>, Boolean> lastClassPerConfig = sorter.sort(childrenToReorder, this::getTestClass);

        childrenToReorder.forEach(testDescriptor::addChild);

        SmartDirtiesTestsHolder.setLastClassPerConfig(lastClassPerConfig);

        return FilterResult.included("sorted");
    }

    @NonNull
    private Class<?> getTestClass(TestDescriptor testDescriptor) {
        Class<?> testClass = getTestClassOrNull(testDescriptor);
        if (testClass == null) {
            throw new UnsupportedOperationException("Unsupported TestDescriptor type " + testDescriptor.getClass()
                + ", failed to obtain test class");
        }
        return testClass;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Nullable
    private Class<?> getTestClassOrNull(TestDescriptor testDescriptor) {
        if (JUnitPlatformSupport.isJUnitVintageEnginePresent()) {
            Class<?> testClass = getTestClassJUnitVintageEngine(testDescriptor);
            if (testClass != null) {
                return testClass;
            }
        }

        return null;
    }

    @Nullable
    private Class<?> getTestClassJUnitVintageEngine(TestDescriptor testDescriptor) {
        if (testDescriptor instanceof RunnerTestDescriptor) {
            RunnerTestDescriptor runnerTestDescriptor = (RunnerTestDescriptor) testDescriptor;
            return runnerTestDescriptor.getDescription().getTestClass();
        }
        if (testDescriptor instanceof VintageTestDescriptor) {
            VintageTestDescriptor vintageTestDescriptor = (VintageTestDescriptor) testDescriptor;
            return vintageTestDescriptor.getDescription().getTestClass();
        }
        return null;
    }
}
