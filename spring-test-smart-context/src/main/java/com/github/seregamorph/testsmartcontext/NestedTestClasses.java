package com.github.seregamorph.testsmartcontext;

import static java.util.Comparator.comparing;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * This class should only be used internally by the framework.
 *
 * @author Sergey Chernov
 */
public final class NestedTestClasses {

    private static final String NESTED_ANNOTATION_NAME = "org.junit.jupiter.api.Nested";

    @Nullable
    private static final Class<? extends Annotation> NESTED_ANNOTATION = loadNestedAnnotation();

    public static List<Class<?>> getNestedTestClasses(Class<?> testClass) {
        List<Class<?>> nestedTestClasses = new ArrayList<>();
        if (NESTED_ANNOTATION != null) {
            Set<Class<?>> visiting = new HashSet<>();
            visiting.add(testClass);
            collectNestedTestClasses(testClass, nestedTestClasses, visiting);
        }
        return nestedTestClasses;
    }

    private static void collectNestedTestClasses(
        Class<?> testClass,
        List<Class<?>> nestedTestClasses,
        Set<Class<?>> visiting
    ) {
        for (Class<?> nestedTestClass : getDirectNestedTestClasses(testClass)) {
            if (visiting.add(nestedTestClass)) {
                collectNestedTestClasses(nestedTestClass, nestedTestClasses, visiting);
                visiting.remove(nestedTestClass);
                if (!nestedTestClasses.contains(nestedTestClass)) {
                    nestedTestClasses.add(nestedTestClass);
                }
            }
        }
    }

    private static List<Class<?>> getDirectNestedTestClasses(Class<?> testClass) {
        Set<Class<?>> nestedTestClasses = new LinkedHashSet<>();
        for (Class<?> clazz = testClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Class<?> declaredClass : clazz.getDeclaredClasses()) {
                if (SmartDirtiesTestsSupport.isInnerClass(declaredClass)
                    && AnnotatedElementUtils.hasAnnotation(declaredClass, NESTED_ANNOTATION)) {
                    nestedTestClasses.add(declaredClass);
                }
            }
        }
        List<Class<?>> sortedNestedTestClasses = new ArrayList<>(nestedTestClasses);
        sortedNestedTestClasses.sort(comparing(Class::getName));
        return sortedNestedTestClasses;
    }

    @Nullable
    private static Class<? extends Annotation> loadNestedAnnotation() {
        ClassLoader classLoader = NestedTestClasses.class.getClassLoader();
        if (!ClassUtils.isPresent(NESTED_ANNOTATION_NAME, classLoader)) {
            return null;
        }
        return ClassUtils.resolveClassName(NESTED_ANNOTATION_NAME, classLoader).asSubclass(Annotation.class);
    }

    private NestedTestClasses() {
    }
}
