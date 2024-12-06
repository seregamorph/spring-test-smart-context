package com.github.seregamorph.testsmartcontext;

import static java.util.Comparator.comparing;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapUtilsHelper;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextBootstrapper;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The logic of this class can be customized via
 * <pre>
 * META-INF/services/com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSorter
 * </pre>
 * defining subtype of this class overriding methods.
 */
public class SmartDirtiesTestsSorter {

    private final Log log = LogFactory.getLog(getClass());

    private static final SmartDirtiesTestsSorter instance = initInstance();

    private static SmartDirtiesTestsSorter initInstance() {
        // subtypes can override methods for customization
        ServiceLoader<SmartDirtiesTestsSorter> loader = ServiceLoader.load(SmartDirtiesTestsSorter.class,
            SmartDirtiesTestsSorter.class.getClassLoader());

        if (loader.iterator().hasNext()) {
            return loader.iterator().next();
        } else {
            return new SmartDirtiesTestsSorter();
        }
    }

    public static SmartDirtiesTestsSorter getInstance() {
        return instance;
    }

    protected SmartDirtiesTestsSorter() {
    }

    /**
     * Sorts passed testItems, all tests are sequentially grouped by {@link MergedContextConfiguration} calculated per
     * each test class.
     *
     * @param testItems
     * @param testClassExtractor
     * @return integration test classes lists grouped by config (non-integration tests classes not included)
     */
    public <T> List<List<Class<?>>> sort(List<T> testItems, TestClassExtractor<T> testClassExtractor) {
        initialSort(testItems, testClassExtractor);

        Set<Class<?>> itClasses = filterAndLogItClasses(testItems, testClassExtractor);

        Map<MergedContextConfiguration, TestClasses> configToTests = new LinkedHashMap<>();
        Map<Class<?>, Integer> classToOrder = new LinkedHashMap<>();
        AtomicInteger orderCounter = new AtomicInteger();
        for (Class<?> itClass : itClasses) {
            TestContextBootstrapper bootstrapper = BootstrapUtilsHelper.resolveTestContextBootstrapper(itClass);
            MergedContextConfiguration mergedContextConfiguration = bootstrapper.buildMergedContextConfiguration();
            // Sequentially each unique mergedContextConfiguration will have own order
            // via orderCounter. initial order values all have a gap to allow for moving
            // @DirtiesContext annotated classes to the back later.
            TestClasses testClasses = configToTests.computeIfAbsent(mergedContextConfiguration,
                $ -> new TestClasses(orderCounter.addAndGet(2), new LinkedHashSet<>()));
            testClasses.classes.add(itClass);
            classToOrder.put(itClass, testClasses.order);
        }

        testItems.sort(comparing(testItem -> {
            Class<?> realClass = testClassExtractor.getTestClass(testItem);
            Integer order = classToOrder.get(realClass);
            if (order == null) {
                // all non-IT tests go first (other returned values are non-zero)
                // this logic can be changed via override
                return getNonItOrder();
            } else {
                // this sorting is stable - most of the tests will preserve alphabetical ordering where possible
                // we only sort classes that shut down the context to the end.
                return isDirtiesContextAfterClass(realClass) ? order + 1 : order;
            }
        }));

        List<List<Class<?>>> sortedConfigToTests = configToTests.values().stream()
            .map(testClasses -> testClasses.classes.stream()
                .sorted(comparing(testItem -> isDirtiesContextAfterClass(testItem) ? 1 : 0))
                .collect(Collectors.toList()))
            .collect(Collectors.toList());

        if (!sortedConfigToTests.isEmpty()) {
            printSuiteTestsPerConfig(sortedConfigToTests);
        }

        return sortedConfigToTests;
    }

    private <T> Set<Class<?>> filterAndLogItClasses(List<T> testItems, TestClassExtractor<T> testClassExtractor) {
        Set<Class<?>> itClasses = new LinkedHashSet<>();
        for (T t : testItems) {
            Class<?> testClass = testClassExtractor.getTestClass(t);
            if (!itClasses.contains(testClass) && isReorderTest(testClass)) {
                itClasses.add(testClass);
            }
        }
        if (!itClasses.isEmpty()) {
            printSuiteTests(testItems.size(), itClasses);
        }
        return itClasses;
    }

    /**
     * Get the order of non-integration test execution (bigger is later). Can be either first or last. 0 (first) by
     * default.
     */
    protected int getNonItOrder() {
        return 0;
    }

    protected <T> void initialSort(List<T> testItems, TestClassExtractor<T> testClassExtractor) {
        testItems.sort(comparing(testItem -> testClassExtractor.getTestClass(testItem).getName()));
        if (Boolean.getBoolean("testsmartcontext.reverse")) {
            Collections.reverse(testItems);
        }
    }

    protected boolean isReorderTest(Class<?> testClass) {
        if (Modifier.isAbstract(testClass.getModifiers())) {
            return false;
        }

        if (ApplicationContextAware.class.isAssignableFrom(testClass)) {
            // Subtypes of org.springframework.test.context.testng.AbstractTestNGSpringContextTests
            // and org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests
            return true;
        }

        if (JUnitPlatformSupport.isJunit4Present() && isReorderTestJUnit4(testClass)) {
            return true;
        }

        //noinspection RedundantIfStatement
        if (JUnitPlatformSupport.isJunit5JupiterApiPresent() && isReorderTestJUnit5Jupiter(testClass)) {
            return true;
        }

        return false;
    }

    /**
     * This method should be only called if JUnit4 is on the classpath
     */
    protected boolean isReorderTestJUnit4(Class<?> testClass) {
        // can be inherited, but cannot be meta-annotation
        RunWith runWith = testClass.getAnnotation(RunWith.class);
        if (runWith == null) {
            return false;
        }
        Class<? extends Runner> runner = runWith.value();
        // includes org.springframework.test.context.junit4.SpringRunner
        return SpringJUnit4ClassRunner.class.isAssignableFrom(runner);
    }

    /**
     * This method should be only called if JUnit5 Jupiter API is on the classpath
     */
    protected boolean isReorderTestJUnit5Jupiter(Class<?> testClass) {
        // can be inherited, can be meta-annotation e.g. via @SpringBootTest
        Set<ExtendWith> extendWith = AnnotatedElementUtils.findAllMergedAnnotations(testClass, ExtendWith.class);
        if (extendWith.isEmpty()) {
            return false;
        }

        return extendWith.stream().map(ExtendWith::value).flatMap(Arrays::stream)
            .anyMatch(SpringExtension.class::isAssignableFrom);
    }

    protected boolean isDirtiesContextAfterClass(Class<?> testClass) {
        Set<DirtiesContext> dirtiesContexts = AnnotatedElementUtils.findAllMergedAnnotations(testClass,
            DirtiesContext.class);
        if (dirtiesContexts.isEmpty()) {
            return false;
        }

        return dirtiesContexts.stream().map(DirtiesContext::classMode)
            .anyMatch(t -> DirtiesContext.ClassMode.AFTER_CLASS == t);
    }

    private void printSuiteTests(int totalTests, Collection<Class<?>> itClasses) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        pw.println("Running suite of " + totalTests + " tests. Integration test classes " +
            "(" + itClasses.size() + " classes):");
        itClasses.forEach(pw::println);
        log.info(sw.toString());
    }

    private void printSuiteTestsPerConfig(List<List<Class<?>>> sortedConfigToTests) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        pw.println("Integration test classes grouped and reordered by MergedContextConfiguration " +
            "(" + sortedConfigToTests.size() + " groups):");
        pw.println("------");
        sortedConfigToTests.forEach(itClasses -> {
            for (Class<?> itClass : itClasses) {
                pw.println(itClass.getName());
            }
            pw.println("------");
        });
        log.info(sw.toString());
    }

    @FunctionalInterface
    public interface TestClassExtractor<T> {
        Class<?> getTestClass(T test);
    }

    private static final class TestClasses {

        private final int order;
        private final Set<Class<?>> classes;

        private TestClasses(int order, Set<Class<?>> classes) {
            this.order = order;
            this.classes = classes;
        }
    }
}
