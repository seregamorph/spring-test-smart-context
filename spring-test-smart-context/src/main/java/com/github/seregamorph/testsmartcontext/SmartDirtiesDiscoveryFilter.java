package com.github.seregamorph.testsmartcontext;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.support.testng.engine.ClassDescriptorHelper;
import org.junit.support.testng.engine.MethodDescriptorHelper;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;
import org.junit.vintage.engine.descriptor.VintageTestDescriptor;
import org.springframework.lang.Nullable;

public class SmartDirtiesDiscoveryFilter implements PostDiscoveryFilter {

    @Override
    public FilterResult apply(TestDescriptor testDescriptor) {
        List<TestDescriptor> childrenToReorder = testDescriptor.getChildren().stream()
            .filter(this::isReorder)
            .collect(Collectors.toList());

        if (childrenToReorder.isEmpty()) {
            return FilterResult.included("Empty list");
        }

        if (SmartDirtiesTestsHolder.isLastClassPerConfigDefined()) {
            return FilterResult.included("already sorted");
        }

        childrenToReorder.forEach(testDescriptor::removeChild);

        SmartDirtiesTestsSorter sorter = SmartDirtiesTestsSorter.getInstance();
        List<TestDescriptor> sortedList = sorter.sort(childrenToReorder, this::getTestClass);

        sortedList.forEach(testDescriptor::addChild);
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

        return true;
    }

    private Class<?> getTestClass(TestDescriptor testDescriptor) {
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

        throw new UnsupportedOperationException("Unsupported TestDescriptor type " + testDescriptor.getClass()
            + ", failed to obtain test class");
    }

    @Nullable
    private Class<?> getTestClassJUnitJupiterEngine(TestDescriptor testDescriptor) {
        if (testDescriptor instanceof ClassTestDescriptor) {
            ClassTestDescriptor classTestDescriptor = (ClassTestDescriptor) testDescriptor;
            return classTestDescriptor.getTestClass();
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
