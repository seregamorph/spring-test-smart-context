package com.github.seregamorph.testsmartcontext;

import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * Auto-discovered JUnit platform {@link PostDiscoveryFilter} which reorders and groups integration test classes
 * according to their configuration. Note: this class sorts only JUnit 4 and Kotest tests executed via
 * <a href="https://junit.org/junit5/docs/current/user-guide/#migrating-from-junit4-running">vintage-engine</a>
 * or Kotest Engine.
 * <p>
 * For TestNG test classes - see {@link com.github.seregamorph.testsmartcontext.testng.SmartDirtiesSuiteListener}, for
 * Jupiter test classes - see {@link com.github.seregamorph.testsmartcontext.jupiter.SmartDirtiesClassOrderer}.
 *
 * @author Sergey Chernov
 */
public class SmartDirtiesPostDiscoveryFilter implements PostDiscoveryFilter {

    private static final List<String> skippedEngines = Arrays.asList("junit-jupiter", "testng");

    @Override
    public FilterResult apply(TestDescriptor testDescriptor) {
        String engine = testDescriptor.getUniqueId().getEngineId().orElse("undefined");
        if (skippedEngines.contains(engine)) {
            // JUnit 5 Jupiter have their own test ordering solutions, skip it
            return FilterResult.included("Skipping engine " + engine);
        }

        List<TestDescriptor> childrenToReorder = testDescriptor.getChildren().stream()
            .filter(childTestDescriptor -> {
                // If it is a testng-engine running TestNG test, rely on SmartDirtiesSuiteListener, because
                // TestNG will alphabetically reorder it first anyway.
                // Jupiter engine has its own sorting via SmartDirtiesClassOrderer, so skip them as well.
                // Reorder only JUnit4 or Kotest here:
                return getTestClassOrNull(childTestDescriptor) != null;
            })
            .collect(Collectors.toList());

        if (childrenToReorder.isEmpty()) {
            return FilterResult.included("Empty list");
        }

        Set<Class<?>> uniqueClasses = childrenToReorder.stream()
            .map(SmartDirtiesPostDiscoveryFilter::getTestClass)
            .collect(Collectors.toSet());
        if (uniqueClasses.size() == 1) {
            // This filter is executed several times during discover and execute phases and
            // it's not possible to distinguish them here. Sometimes per single test is sent as argument,
            // sometimes - the whole suite. If it's a suite more than 1, we can save it and never update.
            // If it's 1 - we should also distinguish single test execution.
            if (SmartDirtiesTestsSupport.classOrderStateMapSize(engine) <= 1) {
                Class<?> testClass = getTestClass(childrenToReorder.get(0));
                SmartDirtiesTestsSupport.setTestClassesLists(engine, singletonList(singletonList(testClass)));
            }

            // the logic here may differ for JUnit 4 via Maven vs IntelliJ:
            // Maven calls this filter several times (first per each test, then with all tests)
            return FilterResult.included("Skipping single element");
        }

        childrenToReorder.forEach(testDescriptor::removeChild);

        SmartDirtiesTestsSorter sorter = SmartDirtiesTestsSorter.getInstance();
        List<List<Class<?>>> testClassesLists;
        try {
            testClassesLists = sorter.sort(childrenToReorder,
                TestClassExtractor.ofClass(SmartDirtiesPostDiscoveryFilter::getTestClass));
        } catch (Throwable e) {
            SmartDirtiesTestsSupport.setFailureCause(e);
            throw e;
        }

        childrenToReorder.forEach(testDescriptor::addChild);

        SmartDirtiesTestsSupport.setTestClassesLists(engine, testClassesLists);

        return FilterResult.included("sorted");
    }

    @NonNull
    private static Class<?> getTestClass(TestDescriptor testDescriptor) {
        Class<?> testClass = getTestClassOrNull(testDescriptor);
        if (testClass == null) {
            throw new UnsupportedOperationException("Unsupported TestDescriptor type " + testDescriptor.getClass()
                + ", failed to obtain test class");
        }
        return testClass;
    }

    @Nullable
    private static Class<?> getTestClassOrNull(TestDescriptor testDescriptor) {
        TestSource testSource = testDescriptor.getSource().orElse(null);
        if (testSource instanceof ClassSource) {
            ClassSource classSource = (ClassSource) testSource;
            return classSource.getJavaClass();
        }

        return null;
    }
}
