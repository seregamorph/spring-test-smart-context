package com.github.seregamorph.testsmartcontext;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.lang.Nullable;

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

    public static int classOrderStateMapSize() {
        return classOrderStateMap == null ? 0 : classOrderStateMap.size();
    }

    static boolean isFirstClassPerConfig(Class<?> testClass) {
        ClassOrderState classOrderState = getOrderState(testClass);
        return classOrderState != null && classOrderState.isFirst;
    }

    static boolean isLastClassPerConfig(Class<?> testClass) {
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

    //@TestOnly
    static void reset() {
        classOrderStateMap = null;
    }
}
