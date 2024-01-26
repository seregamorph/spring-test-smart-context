package com.github.seregamorph.testsmartcontext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.support.testng.engine.ClassDescriptorHelper;
import org.junit.support.testng.engine.MethodDescriptorHelper;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;
import org.junit.vintage.engine.descriptor.VintageTestDescriptor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class SmartDirtiesDiscoveryFilter implements PostDiscoveryFilter {

    @Override
    public FilterResult apply(TestDescriptor testDescriptor) {
        List<TestDescriptor> childrenToReorder = testDescriptor.getChildren().stream()
            .filter(this::isReorder)
            .collect(Collectors.toList());
        Set<Class<?>> uniqueClasses = childrenToReorder.stream()
            .map(this::getTestClass)
            .collect(Collectors.toSet());

        if (childrenToReorder.isEmpty()) {
            return FilterResult.included("Empty list");
        }

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

    private boolean isReorder(TestDescriptor testDescriptor) {
        if (JUnitPlatformSupport.isJUnitTestNGEnginePresent()) {
            // if it is a testng-engine running TestNG test, rely on SmartDirtiesSuiteListener, because
            // TestNG will alphabetically reorder it first anyway
            if (ClassDescriptorHelper.isClassDescriptor(testDescriptor)
                || MethodDescriptorHelper.isMethodDescriptor(testDescriptor)) {
                return false;
            }
        }

        return getTestClassOrNull(testDescriptor) != null;
    }

    @Nullable
    private Class<?> getTestClassOrNull(TestDescriptor testDescriptor) {
        if (JUnitPlatformSupport.isJUnitJupiterEnginePresent()) {
            Class<?> testClass = getTestClassJUnitJupiterEngine(testDescriptor);
            if (testClass != null) {
                return testClass;
            }
        }

        if (JUnitPlatformSupport.isJUnitVintageEnginePresent()) {
            Class<?> testClass = getTestClassJUnitVintageEngine(testDescriptor);
            if (testClass != null) {
                return testClass;
            }
        }

        return null;
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

    @Nullable
    private Class<?> getTestClassJUnitJupiterEngine(TestDescriptor testDescriptor) {
        if (testDescriptor instanceof ClassTestDescriptor) {
            ClassTestDescriptor classTestDescriptor = (ClassTestDescriptor) testDescriptor;
            return classTestDescriptor.getTestClass();
        }
        if (testDescriptor instanceof TestMethodTestDescriptor) {
            TestMethodTestDescriptor testMethodTestDescriptor = (TestMethodTestDescriptor) testDescriptor;
            return testMethodTestDescriptor.getTestClass();
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
