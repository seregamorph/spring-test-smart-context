package com.github.seregamorph.testsmartcontext;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.lang.Nullable;
import org.springframework.test.context.BootstrapUtilsHelper;
import org.springframework.test.context.MergedContextConfiguration;

public class SmartDirtiesTestsHolder {

    private static Map<Class<?>, ClassOrderState> classOrderStateMap;

    private static class ClassOrderState {
        private final boolean isFirst;
        private final boolean isLast;

        private ClassOrderState(boolean isFirst, boolean isLast) {
            this.isFirst = isFirst;
            this.isLast = isLast;
        }
    }

    protected static int classOrderStateMapSize() {
        return classOrderStateMap == null ? 0 : classOrderStateMap.size();
    }

    static boolean isFirstClassPerConfig(Class<?> testClass) {
        if (isInnerClass(testClass)) {
            // to support @Nested classes (without own context configuration)
            return false;
        }
        ClassOrderState classOrderState = getOrderState(testClass);
        return classOrderState != null && classOrderState.isFirst;
    }

    static boolean isLastClassPerConfig(Class<?> testClass) {
        if (isInnerClass(testClass)) {
            // to support @Nested classes (without own context configuration)
            return false;
        }
        ClassOrderState classOrderState = getOrderState(testClass);
        return classOrderState != null && classOrderState.isLast;
    }

    @Nullable
    private static ClassOrderState getOrderState(Class<?> testClass) {
        if (classOrderStateMap == null) {
            if (JUnitPlatformSupport.isJUnit4IdeaTestRunnerPresent()) {
                System.err.println("The test is started via IDEA old JUnit 4 runner (not vintage), " +
                    "the Smart DirtiesContext behaviour is disabled.");
                if (!JUnitPlatformSupport.isJunit5JupiterApiPresent()) {
                    System.err.println("If you add org.junit.jupiter:junit-jupiter-api test dependency, \n" +
                        "it will allow to run packages/modules with tests with Smart DirtiesContext semantics via IDEA. See \n" +
                        "https://youtrack.jetbrains.com/issue/IDEA-343605/junit-vintage-engine-is-not-preferred-by-default\n" +
                        "for details.");
                }
                return null;
            }
            throw new IllegalStateException("lastClassPerConfig is not initialized");
        }
        ClassOrderState classOrderState = classOrderStateMap.get(testClass);
        if (classOrderState == null) {
            throw new IllegalStateException("classOrderStateMap is not defined for class "
                + testClass + ", it means that it was skipped on initial analysis. " +
                "Discovered classes: " + classOrderStateMap.keySet());
        }
        return classOrderState;
    }

    protected static void setTestClassesLists(List<List<Class<?>>> testClassesLists) {
        Map<Class<?>, ClassOrderState> classOrderStateMap = new LinkedHashMap<>();
        for (List<Class<?>> testClasses : testClassesLists) {
            Iterator<Class<?>> iterator = testClasses.iterator();
            boolean isFirst = true;
            while (iterator.hasNext()) {
                Class<?> testClass = iterator.next();
                classOrderStateMap.put(testClass, new ClassOrderState(isFirst, !iterator.hasNext()));
                isFirst = false;
            }
        }
        SmartDirtiesTestsHolder.classOrderStateMap = classOrderStateMap;
    }

    protected static void verifyInnerClass(Class<?> innerTestClass) {
        // @ContextConfiguration, @Import, etc. on Nested class will lead to creation of separate spring context.
        // We can order enclosing classes, but tests of Nested test classes will always go sequentially is scope
        // of their enclosing class. And the spring context may be shared between these inner Nested classes of
        // different tests.
        Class<?> enclosingClass = getEnclosingClass(innerTestClass);
        MergedContextConfiguration enclosingContextConfiguration =
            BootstrapUtilsHelper.resolveTestContextBootstrapper(enclosingClass).buildMergedContextConfiguration();
        MergedContextConfiguration innerContextConfiguration =
            BootstrapUtilsHelper.resolveTestContextBootstrapper(innerTestClass).buildMergedContextConfiguration();

        // TODO find compromising solution for @Nested classes
        if (!enclosingContextConfiguration.equals(innerContextConfiguration)) {
            throw new IllegalStateException("Nested inner " + innerTestClass + " declares custom context " +
                "configuration which differs from enclosing " + enclosingClass + ". " +
                "This is not properly supported by the spring-test-smart-context ordering because of framework " +
                "limitations. Please extract inner test class to upper level.");
        }
    }

    protected static boolean isInnerClass(Class<?> clazz) {
        return !isStatic(clazz) && clazz.isMemberClass();
    }

    private static boolean isStatic(Class<?> clazz) {
        return Modifier.isStatic(clazz.getModifiers());
    }

    private static Class<?> getEnclosingClass(Class<?> clazz) {
        Class<?> enclosingClass = clazz.getEnclosingClass();
        // it can be deeply nesting - with recursion avoid possible infinite cycle
        // (see org.junit.platform.commons.util.ReflectionUtils.detectInnerClassCycle)
        return enclosingClass == null ? clazz : getEnclosingClass(enclosingClass);
    }

    //@TestOnly
    static void reset() {
        classOrderStateMap = null;
    }
}
