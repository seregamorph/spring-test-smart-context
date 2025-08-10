package com.github.seregamorph.testsmartcontext;

import com.github.seregamorph.testsmartcontext.jupiter.SmartDirtiesClassOrderer;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.ClassOrderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.test.context.BootstrapUtilsHelper;
import org.springframework.test.context.MergedContextConfiguration;

/**
 * This class should only be used internally by the framework.
 *
 * @author Sergey Chernov
 */
public class SmartDirtiesTestsSupport {

    private static final Logger log = LoggerFactory.getLogger(SmartDirtiesTestsSupport.class);

    protected static final String ENGINE_TESTNG = "testng";
    protected static final String ENGINE_JUNIT_JUPITER = "junit-jupiter";

    /**
     * engine -> test class -> ClassOrderState
     */
    private static Map<String, Map<Class<?>, ClassOrderState>> engineClassOrderStateMap;

    @Nullable
    private static Throwable failureCause;

    static class ClassOrderState {
        private final boolean isIntegrationTest;
        private final String testEngine;
        private final boolean isFirst;
        private final boolean isLast;

        private ClassOrderState(boolean isIntegrationTest, String testEngine, boolean isFirst, boolean isLast) {
            this.isIntegrationTest = isIntegrationTest;
            this.testEngine = testEngine;
            this.isFirst = isFirst;
            this.isLast = isLast;
        }
    }

    @Nullable
    static Set<Class<?>> getTestClasses(String engine) {
        Map<Class<?>, ClassOrderState> classOrderStateMap = engineClassOrderStateMap == null ? null
            : engineClassOrderStateMap.get(engine);
        return classOrderStateMap == null ? null : classOrderStateMap.keySet();
    }

    protected static int classOrderStateMapSize(String engine) {
        Set<Class<?>> testClasses = getTestClasses(engine);
        return testClasses == null ? 0 : testClasses.size();
    }

    //@TestOnly
    static boolean isFirstClassPerConfig(Class<?> testClass) {
        if (isInnerClass(testClass)) {
            // to support @Nested classes (without own context configuration)
            return false;
        }

        // this method is only used in tests, so we don't need to be lenient there
        List<ClassOrderState> classOrderStates = getOrderStates(testClass);
        if (classOrderStates.size() == 1) {
            ClassOrderState classOrderState = classOrderStates.get(0);
            if (!classOrderState.isIntegrationTest) {
                throw new IllegalStateException("Test " + testClass + " is not recognized as integration test");
            }
            return classOrderState.isFirst;
        } else {
            throw new IllegalStateException("Unexpected more than one matching test engine for " + testClass);
        }
    }

    static boolean isLastClassPerConfig(Class<?> testClass) {
        if (isInnerClass(testClass)) {
            // to support @Nested classes (without own context configuration)
            return false;
        }

        List<ClassOrderState> classOrderStates = getOrderStates(testClass);
        for (ClassOrderState classOrderState : classOrderStates) {
            if (!classOrderState.isIntegrationTest) {
                // This can be a result of custom extension or listener.
                // To fix this implement own IntegrationTestFilter and declare via META-INF SPI
                log.warn("Test {} in suite of {} engine was not recognized as spring integration test by {}, "
                        + "it's recommended to override the IntegrationTestFilter accordingly",
                    testClass, classOrderState.testEngine, IntegrationTestFilter.getInstance().getClass());
            }
        }

        if (classOrderStates.isEmpty()) {
            List<String> classes = engineClassOrderStateMap.entrySet().stream()
                .map(entry -> {
                    String engineClassNames = entry.getValue().keySet().stream()
                        .map(Class::getName)
                        .collect(Collectors.joining(", "));
                    return entry.getKey() + ": " + engineClassNames;
                })
                .collect(Collectors.toList());
            throw new IllegalStateException("engineClassOrderStateMap is not defined for "
                + testClass + ", it means that it was skipped on initial analysis or failed. "
                + "Discovered classes by engine: " + classes + (failureCause == null ? "" : ": " + failureCause),
                failureCause);
        } else if (classOrderStates.size() == 1) {
            return classOrderStates.get(0).isLast;
        } else {
            // In the common case it's theoretically possible that the same class is discovered by more than one
            // test engine. And at this point we don't know which engine is running current test,
            // so we do a failover logic
            Set<Boolean> isLasts = classOrderStates.stream()
                .map(state -> state.isLast)
                .collect(Collectors.toSet());

            if (isLasts.size() == 1) {
                // no ambiguity
                return isLasts.iterator().next();
            } else {
                assert isLasts.size() == 2;
                log.warn("Test {} was discovered by more than one test engine with different ordering {}", testClass,
                    classOrderStates.stream()
                        .map(state -> state.testEngine)
                        .collect(Collectors.toList()));
                // at least one engine considers it as last: do close the context
                return true;
            }
        }
    }

    private static List<ClassOrderState> getOrderStates(Class<?> testClass) {
        if (engineClassOrderStateMap == null) {
            if (failureCause != null) {
                throw new IllegalStateException("Test ordering is not initialized or failed", failureCause);
            }
            if (JUnitPlatformSupport.isJunit5JupiterApiPresent()) {
                try {
                    ClassLoader classLoader = SmartDirtiesTestsSupport.class.getClassLoader();
                    List<URL> junitPlatformConfigUrls = Collections.list(classLoader.getResources(
                        "junit-platform.properties"));
                    for (URL junitPlatformConfigUrl : junitPlatformConfigUrls) {
                        Properties properties = new Properties();
                        try (InputStream in = junitPlatformConfigUrl.openStream()) {
                            properties.load(in);
                        }
                        String configClassOrderer = properties.getProperty(ClassOrderer.DEFAULT_ORDER_PROPERTY_NAME);
                        if (!SmartDirtiesClassOrderer.class.getName().equals(configClassOrderer)) {
                            //@formatter:off
                            throw new IllegalStateException("engineClassOrderStateMap is not initialized, "
                                + "because more than one junit-platform.properties was found in the classpath: "
                                + junitPlatformConfigUrls + ". JUnit 5 supports only one configuration file "
                                + "https://github.com/junit-team/junit5/issues/2794\n"
                                + "The " + junitPlatformConfigUrl + " "
                                + (configClassOrderer == null ? "does not declare the "
                                    + ClassOrderer.DEFAULT_ORDER_PROPERTY_NAME + " property" : "declares\n"
                                    + ClassOrderer.DEFAULT_ORDER_PROPERTY_NAME + "=" + configClassOrderer + "\n")
                                + " (should have value " + SmartDirtiesClassOrderer.class.getName()
                                + " to address the issue)", failureCause);
                            //@formatter:on
                        }
                        //@formatter:off

                        // Pass via system property
                        // -Djunit.jupiter.testclass.order.default=com.github.seregamorph.testsmartcontext.jupiter.SmartDirtiesClassOrderer
                        // (don't forget about Maven/Gradle and IDEA default configuration)
                        // or add line to your junit-platform.properties
                        // junit.jupiter.testclass.order.default=com.github.seregamorph.testsmartcontext.jupiter.SmartDirtiesClassOrderer

                        //@formatter:on
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            if (JUnitPlatformSupport.isJunit4Present() && JUnitPlatformSupport.isJUnit4IdeaTestRunnerPresent()) {
                System.err.println("The test is started via IDEA old JUnit 4 runner (not vintage), "
                    + "the Smart DirtiesContext behaviour is disabled.");
                if (!JUnitPlatformSupport.isJunit5JupiterApiPresent()) {
                    //@formatter:off
                    System.err.println("If you add org.junit.jupiter:junit-jupiter-api test dependency, \n"
                        + "it will allow to run packages/modules with tests with Smart DirtiesContext semantics via "
                        + "IDEA. See \n"
                        + "https://youtrack.jetbrains.com/issue/IDEA-343605/junit-vintage-engine-is-not-preferred-by-default\n"
                        + "for details.");
                    //@formatter:on
                }
                return Collections.emptyList();
            }
            throw new IllegalStateException("Test ordering is not initialized or failed");
        }

        List<ClassOrderState> classOrderStates = new ArrayList<>();
        for (Map<Class<?>, ClassOrderState> classOrderStateMap : engineClassOrderStateMap.values()) {
            ClassOrderState classOrderState = classOrderStateMap.get(testClass);
            if (classOrderState != null) {
                classOrderStates.add(classOrderState);
            }
        }
        return classOrderStates;
    }

    protected static void setTestClassesLists(String engine, TestSortResult testSortResult) {
        Map<Class<?>, ClassOrderState> classOrderStateMap = new LinkedHashMap<>();
        for (List<Class<?>> testClasses : testSortResult.getSortedConfigToTests()) {
            Iterator<Class<?>> iterator = testClasses.iterator();
            boolean isFirst = true;
            while (iterator.hasNext()) {
                Class<?> testClass = iterator.next();
                classOrderStateMap.put(testClass,
                    new ClassOrderState(true, engine, isFirst, !iterator.hasNext()));
                isFirst = false;
            }
        }
        for (Class<?> nonItClass : testSortResult.getNonItClasses()) {
            ClassOrderState prev = classOrderStateMap.put(nonItClass,
                new ClassOrderState(false, engine, false, false));
            assert prev == null;
        }

        if (SmartDirtiesTestsSupport.engineClassOrderStateMap == null) {
            SmartDirtiesTestsSupport.engineClassOrderStateMap = new LinkedHashMap<>();
        }
        SmartDirtiesTestsSupport.engineClassOrderStateMap.put(engine, classOrderStateMap);
    }

    protected static void setFailureCause(Throwable failureCause) {
        SmartDirtiesTestsSupport.failureCause = failureCause;
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
            throw new IllegalStateException("Nested inner " + innerTestClass + " declares custom context "
                + "configuration which differs from enclosing " + enclosingClass + ". "
                + "This is not properly supported by the spring-test-smart-context ordering because of framework "
                + "limitations. Please extract inner test class to upper level.");
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
    static Map<String, Map<Class<?>, ClassOrderState>> setEngineClassOrderStateMap(
        Map<String, Map<Class<?>, ClassOrderState>> engineClassOrderStateMap
    ) {
        Map<String, Map<Class<?>, ClassOrderState>> prev = SmartDirtiesTestsSupport.engineClassOrderStateMap;
        SmartDirtiesTestsSupport.engineClassOrderStateMap = engineClassOrderStateMap;
        return prev;
    }
}
