package com.github.seregamorph.testsmartcontext;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SmartDirtiesTestsHolder {

    private static Map<Class<?>, Boolean> lastClassPerConfig;

    public static int lastClassPerConfigSize() {
        return lastClassPerConfig == null ? 0 : lastClassPerConfig.size();
    }

    static boolean isLastClassPerConfig(Class<?> testClass) {
        if (lastClassPerConfig == null) {
            if (JUnitPlatformSupport.isJUnit4IdeaTestRunnerPresent()) {
                System.err.println("The test is started via IDEA old JUnit 4 runner (not vintage), " +
                    "the Smart DirtiesContext behaviour is disabled.");
                if (!JUnitPlatformSupport.isJunit5JupiterApiPresent()) {
                    System.err.println("If you add org.junit.jupiter:junit-jupiter-api test dependency, \n" +
                        "it will allow to run packages/modules with tests with Smart DirtiesContext semantics via IDEA. See \n" +
                        "https://youtrack.jetbrains.com/issue/IDEA-343605/junit-vintage-engine-is-not-preferred-by-default\n" +
                        "for details.");
                }
                return false;
            }
            throw new IllegalStateException("lastClassPerConfig is not initialized");
        }
        Boolean isLastClassPerConfig = lastClassPerConfig.get(testClass);
        if (isLastClassPerConfig == null) {
            throw new IllegalStateException("lastClassPerConfig is not defined for class "
                + testClass + ", it means that it was skipped on initial analysis. " +
                "Discovered classes: " + lastClassPerConfig.keySet());
        }
        return isLastClassPerConfig;
    }

    protected static void setTestClassesLists(List<List<Class<?>>> testClassesLists) {
        Map<Class<?>, Boolean> lastClassPerConfig = new LinkedHashMap<>();
        for (List<Class<?>> testClasses : testClassesLists) {
            Iterator<Class<?>> iterator = testClasses.iterator();
            while (iterator.hasNext()) {
                Class<?> testClass = iterator.next();
                boolean isLast = !iterator.hasNext();
                lastClassPerConfig.put(testClass, isLast);
            }
        }
        SmartDirtiesTestsHolder.lastClassPerConfig = lastClassPerConfig;
    }
}
