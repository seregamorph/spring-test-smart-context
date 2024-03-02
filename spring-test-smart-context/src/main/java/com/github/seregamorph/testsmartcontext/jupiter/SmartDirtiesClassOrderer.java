package com.github.seregamorph.testsmartcontext.jupiter;

import static java.util.Collections.singletonList;

import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsHolder;
import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSorter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

/**
 * Auto-discovered Jupiter {@link ClassOrderer} which reorders and groups the integration test classes per their
 * configuration. Also stores information about last integration class per configuration, which is used by
 * {@link com.github.seregamorph.testsmartcontext.SmartDirtiesContextTestExecutionListener}.
 * <p>
 * For TestNG test classes - see {@link com.github.seregamorph.testsmartcontext.testng.SmartDirtiesSuiteListener}, for
 * JUnit 4 test classes - see {@link com.github.seregamorph.testsmartcontext.SmartDirtiesPostDiscoveryFilter}.
 */
public class SmartDirtiesClassOrderer extends SmartDirtiesTestsHolder implements ClassOrderer {

    @Override
    public void orderClasses(ClassOrdererContext context) {
        List<? extends ClassDescriptor> classDescriptors = context.getClassDescriptors();
        if (classDescriptors.isEmpty()) {
            return;
        }

        Set<Class<?>> uniqueClasses = classDescriptors.stream()
            .map(ClassDescriptor::getTestClass)
            .collect(Collectors.toSet());
        if (uniqueClasses.size() == 1) {
            // This filter is executed several times during discover and execute phases and
            // it's not possible to distinguish them here. Sometimes per single test is sent as argument,
            // sometimes - the whole suite. If it's a suite more than 1, we can save it and never update.
            // If it's 1 - we should also distinguish single test execution.
            if (SmartDirtiesTestsHolder.lastClassPerConfigSize() <= 1) {
                Class<?> testClass = classDescriptors.get(0).getTestClass();
                SmartDirtiesTestsHolder.setTestClassesLists(singletonList(singletonList(testClass)));
            }
            return;
        }

        SmartDirtiesTestsSorter sorter = SmartDirtiesTestsSorter.getInstance();
        List<List<Class<?>>> testClassesLists = sorter.sort(classDescriptors, ClassDescriptor::getTestClass);

        SmartDirtiesTestsHolder.setTestClassesLists(testClassesLists);
    }
}
