package com.github.seregamorph.testsmartcontext.jupiter;

import static java.util.Collections.singletonList;

import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsHolder;
import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSorter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;
import org.junit.jupiter.api.Nested;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Auto-discovered Jupiter {@link ClassOrderer} which reorders and groups the integration test classes per their
 * configuration. Also stores information about last integration class per configuration, which is used by
 * {@link com.github.seregamorph.testsmartcontext.SmartDirtiesContextTestExecutionListener}.
 * <p>
 * For TestNG test classes - see {@link com.github.seregamorph.testsmartcontext.testng.SmartDirtiesSuiteListener}, for
 * JUnit 4 test classes - see {@link com.github.seregamorph.testsmartcontext.SmartDirtiesPostDiscoveryFilter}.
 *
 * @author Sergey Chernov
 */
public class SmartDirtiesClassOrderer extends SmartDirtiesTestsHolder implements ClassOrderer {

    @Override
    public void orderClasses(ClassOrdererContext context) {
        List<? extends ClassDescriptor> classDescriptors = context.getClassDescriptors();
        if (classDescriptors.isEmpty()) {
            return;
        }

        // Special notes: Maven has different behavior in comparison with IDEA and Gradle, it calls orderClasses method
        // for each test class with a single element of classDescriptors list. That's why we need to handle single-size
        // list separately.

        // If Jupiter Test class has @Nested inner classes, for each of them (if there is only one inner class)
        // orderClasses will be called

        Set<Class<?>> uniqueClasses = new LinkedHashSet<>();
        for (ClassDescriptor classDescriptor : classDescriptors) {
            Class<?> testClass = classDescriptor.getTestClass();
            if (isInnerClass(testClass)) {
                if (!uniqueClasses.isEmpty()) {
                    // this should not happen, they should be never mixed in one call
                    throw new IllegalStateException("Unexpected mix of inner " + testClass + " and " + uniqueClasses);
                }
                Nested nested = AnnotationUtils.getAnnotation(testClass, Nested.class);
                if (nested == null) {
                    // this should not happen
                    throw new IllegalStateException("Missing @Nested annotation for inner " + testClass);
                }
                // implementation notice: if the exception is thrown from this block, it does not break the
                // test execution as it's suppressed in
                // org.junit.jupiter.engine.discovery.AbstractOrderingVisitor.doWithMatchingDescriptor
                // So this validation will be repeated at BeforeClass
                verifyInnerClass(testClass);
            } else {
                // regular test class
                uniqueClasses.add(testClass);
            }
        }

        if (uniqueClasses.isEmpty()) {
            // All are internal (@Nested), we do not reorder them.
            // The enclosing classes are already in the SmartDirtiesTestsHolder from previous call
            if (SmartDirtiesTestsHolder.classOrderStateMapSize() == 0) {
                throw new IllegalStateException("orderClasses is called with inner classes list " + classDescriptors
                    + " before being called with enclosing class list");
            }
            return;
        }

        if (uniqueClasses.size() == 1) {
            // This filter is executed several times during discover and execute phases and
            // it's not possible to distinguish them here. Sometimes per single test is sent as argument,
            // sometimes - the whole suite. If it's a suite more than 1, we can save it and never update.
            // If it's 1 - we should also distinguish single test execution.
            if (SmartDirtiesTestsHolder.classOrderStateMapSize() <= 1) {
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
