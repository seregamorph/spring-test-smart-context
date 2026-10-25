package com.github.seregamorph.testsmartcontext;

import com.github.seregamorph.testsmartcontext.jupiter.SmartDirtiesClassOrderer;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class should only be used internally by the framework.
 *
 * @author Sergey Chernov
 */
public abstract class SmartDirtiesTestsSupport {

    private static final Logger log = LoggerFactory.getLogger(SmartDirtiesTestsSupport.class);

    protected static final String ENGINE_TESTNG = "testng";
    protected static final String ENGINE_JUNIT_JUPITER = "junit-jupiter";
    // note: same as KotestJunitPlatformTestEngine.EngineId = "kotest"
    protected static final String ENGINE_KOTEST = "kotest";

    /**
     * engine -> test class -> ClassGroupState
     */
    private static Map<String, Map<Class<?>, ClassGroupState>> engineClassOrderStateMap;

    @Nullable
    private static Throwable failureCause;

    /**
     * Not null if the effective Jupiter class orderer of the current test plan is not SmartDirtiesClassOrderer
     */
    @Nullable
    private static String classOrdererMisconfiguration;

    static class ClassGroupState {
        private final Set<Class<?>> completedItClasses = Collections.synchronizedSet(new LinkedHashSet<>());

        private final String engine;
        final IntegrationTestFilter integrationTestFilter;
        final Set<Class<?>> discoveredItClasses;

        private ClassGroupState(String engine, IntegrationTestFilter integrationTestFilter, List<Class<?>> discoveredItClasses) {
            this.engine = engine;
            this.integrationTestFilter = integrationTestFilter;
            this.discoveredItClasses = Collections.unmodifiableSet(new LinkedHashSet<>(discoveredItClasses));
        }
    }

    @Nullable
    static Map<Class<?>, ClassGroupState> getTestClasses(String engine) {
        return engineClassOrderStateMap == null ? null : engineClassOrderStateMap.get(engine);
    }

    protected static int classOrderStateMapSize(String engine) {
        Map<Class<?>, ClassGroupState> testClasses = getTestClasses(engine);
        return testClasses == null ? 0 : (int) testClasses.keySet().stream()
            .filter(testClass -> !isInnerClass(testClass))
            .count();
    }

    protected static boolean hasNestedTestClasses(Class<?> testClass) {
        return !NestedTestClasses.getNestedTestClasses(testClass).isEmpty();
    }

    static boolean markCompleteAndIsLastClassPerConfig(Class<?> testClass) {
        if (isInnerClass(testClass) && !isRegistered(testClass)) {
            return false;
        }

        List<ClassGroupState> classGroupStates = getOrderStates(testClass);
        for (ClassGroupState classGroupState : classGroupStates) {
            if (!classGroupState.discoveredItClasses.contains(testClass)) {
                // This can be a result of custom extension or listener.
                // To fix this implement own IntegrationTestFilter and declare via META-INF SPI
                log.warn("Test {} in suite of {} engine was not recognized as spring integration test by {}, "
                        + "it's recommended to override the IntegrationTestFilter accordingly",
                    testClass, classGroupState.engine, classGroupState.integrationTestFilter);
            }
        }

        if (classGroupStates.isEmpty()) {
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
                + "Discovered classes by engine: " + classes + (failureCause == null ? "" : ": " + failureCause)
                + (classOrdererMisconfiguration == null ? "" : ". " + classOrdererMisconfiguration),
                failureCause);
        } else if (classGroupStates.size() == 1) {
            ClassGroupState classGroupState = classGroupStates.get(0);
            classGroupState.completedItClasses.add(testClass);
            return classGroupState.discoveredItClasses.equals(classGroupState.completedItClasses);
        } else {
            // In the common case it's theoretically possible that the same class is discovered by more than one
            // test engine. And at this point we don't know which engine is running current test,
            // so we do a failover logic
            Set<Boolean> isLasts = classGroupStates.stream()
                .map(classGroupState -> {
                    classGroupState.completedItClasses.add(testClass);
                    return classGroupState.discoveredItClasses.equals(classGroupState.completedItClasses);
                })
                .collect(Collectors.toSet());

            if (isLasts.size() == 1) {
                // no ambiguity
                return isLasts.iterator().next();
            } else {
                assert isLasts.size() == 2;
                log.warn("Test {} was discovered by more than one test engine with different ordering {}", testClass,
                    classGroupStates.stream()
                        .map(state -> state.engine)
                        .collect(Collectors.toList()));
                // at least one engine considers it as last: do close the context
                return true;
            }
        }
    }

    static List<ClassGroupState> getOrderStates(Class<?> testClass) {
        if (engineClassOrderStateMap == null) {
            if (classOrdererMisconfiguration != null) {
                throw new IllegalStateException("Test ordering is not initialized. " + classOrdererMisconfiguration,
                    failureCause);
            }
            if (failureCause != null) {
                throw new IllegalStateException("Test ordering is not initialized or failed", failureCause);
            }
            if (ClasspathPlatformSupport.isJunitJupiterApiPresent()) {
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
                                + junitPlatformConfigUrls + ". JUnit supports only one configuration file "
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
            if (ClasspathPlatformSupport.isJunit4Present() && isIntegrationTestJUnit4(testClass)) {
                System.err.println(testClass.getName() + " is a JUnit 4 test class, "
                    + "the Smart DirtiesContext behaviour is not supported. The last version supporting JUnit 4 is 0.15");
                return Collections.emptyList();
            }
            throw new IllegalStateException("Test ordering is not initialized or failed");
        }

        List<ClassGroupState> classGroupStates = new ArrayList<>();
        for (Map<Class<?>, ClassGroupState> classOrderStateMap : engineClassOrderStateMap.values()) {
            ClassGroupState classGroupState = classOrderStateMap.get(testClass);
            if (classGroupState != null) {
                classGroupStates.add(classGroupState);
            }
        }
        return classGroupStates;
    }

    protected static void setTestClassesLists(String engine, TestSortResult testSortResult) {
        Map<Class<?>, ClassGroupState> classOrderStateMap = new LinkedHashMap<>();
        for (List<Class<?>> testClasses : testSortResult.getSortedConfigToTests()) {
            ClassGroupState classGroupState = new ClassGroupState(engine,
                testSortResult.getIntegrationTestFilter(), testClasses);
            for (Class<?> testClass : testClasses) {
                classOrderStateMap.put(testClass, classGroupState);
            }
        }
        for (Class<?> nonItClass : testSortResult.getNonItClasses()) {
            classOrderStateMap.put(nonItClass, new ClassGroupState(engine,
                testSortResult.getIntegrationTestFilter(), Collections.emptyList()));
        }

        if (SmartDirtiesTestsSupport.engineClassOrderStateMap == null) {
            SmartDirtiesTestsSupport.engineClassOrderStateMap = new LinkedHashMap<>();
        }
        SmartDirtiesTestsSupport.engineClassOrderStateMap.put(engine, classOrderStateMap);
    }

    protected static void setFailureCause(Throwable failureCause) {
        SmartDirtiesTestsSupport.failureCause = failureCause;
    }

    /**
     * Validates the effective {@value ClassOrderer#DEFAULT_ORDER_PROPERTY_NAME} configuration parameter of the
     * Jupiter test plan which is about to be executed. It can differ from the bundled junit-platform.properties,
     * because explicit configuration parameters of the launcher discovery request take precedence, e.g. Maven
     * Surefire/Failsafe 3.6.0 overrides it for runOrder=alphabetical, random or reversealphabetical.
     *
     * @param classOrderer effective class orderer or null if not defined
     * @return error message if the class orderer is misconfigured, otherwise null
     */
    @Nullable
    static String checkJupiterClassOrderer(@Nullable String classOrderer) {
        String expectedClassOrderer = SmartDirtiesClassOrderer.class.getName();
        if (classOrderer != null && expectedClassOrderer.equals(classOrderer.trim())) {
            classOrdererMisconfiguration = null;
        } else {
            //@formatter:off
            classOrdererMisconfiguration = "The " + ClassOrderer.DEFAULT_ORDER_PROPERTY_NAME + " configuration "
                + "parameter of the test plan "
                + (classOrderer == null ? "is not defined" : "is " + classOrderer)
                + ", but should be " + expectedClassOrderer + ". "
                + "If you use Maven Surefire/Failsafe 3.6.0 with runOrder, remove runOrder "
                + "(the test classes are sorted alphabetically within the groups anyway) or declare "
                + "<properties><configurationParameters>" + ClassOrderer.DEFAULT_ORDER_PROPERTY_NAME + "="
                + expectedClassOrderer + "</configurationParameters></properties> "
                + "(fixed in Surefire by https://github.com/apache/maven-surefire/pull/3530 and to be released in 3.6.1)";
            //@formatter:on
        }
        return classOrdererMisconfiguration;
    }

    static void resetClassOrdererMisconfiguration() {
        classOrdererMisconfiguration = null;
    }

    static void completeNestedClassesMissingInTestPlan(String engine, Set<Class<?>> testPlanClasses) {
        Map<Class<?>, ClassGroupState> testClasses = getTestClasses(engine);
        if (testClasses == null) {
            return;
        }
        for (Class<?> testPlanClass : testPlanClasses) {
            if (isInnerClass(testPlanClass)) {
                continue;
            }
            for (Class<?> nestedClass : NestedTestClasses.getNestedTestClasses(testPlanClass)) {
                ClassGroupState classGroupState = testClasses.get(nestedClass);
                if (classGroupState != null && !testPlanClasses.contains(nestedClass)) {
                    log.debug("Nested {} is not included in the test plan", nestedClass);
                    classGroupState.completedItClasses.add(nestedClass);
                }
            }
        }
    }

    private static boolean isRegistered(Class<?> testClass) {
        return engineClassOrderStateMap != null && engineClassOrderStateMap.values().stream()
            .anyMatch(classOrderStateMap -> classOrderStateMap.containsKey(testClass));
    }

    protected static boolean isInnerClass(Class<?> clazz) {
        return !isStatic(clazz) && clazz.isMemberClass();
    }

    private static boolean isStatic(Class<?> clazz) {
        return Modifier.isStatic(clazz.getModifiers());
    }

    //@TestOnly
    static Map<String, Map<Class<?>, ClassGroupState>> setEngineClassOrderStateMap(
        Map<String, Map<Class<?>, ClassGroupState>> engineClassOrderStateMap
    ) {
        Map<String, Map<Class<?>, ClassGroupState>> prev = SmartDirtiesTestsSupport.engineClassOrderStateMap;
        SmartDirtiesTestsSupport.engineClassOrderStateMap = engineClassOrderStateMap;
        return prev;
    }

    private static boolean isIntegrationTestJUnit4(Class<?> testClass) {
        // can be inherited, but cannot be meta-annotation
        RunWith runWith = testClass.getAnnotation(RunWith.class);
        if (runWith == null) {
            return false;
        }
        Class<? extends Runner> runner = runWith.value();
        // includes org.springframework.test.context.junit4.SpringRunner
        return SpringJUnit4ClassRunner.class.isAssignableFrom(runner);
    }
}
