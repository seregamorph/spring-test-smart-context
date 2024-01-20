package com.github.seregamorph.testsmartcontext;

import static java.util.Comparator.comparing;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.BootstrapUtilsHelper;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextBootstrapper;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SuppressWarnings("CodeBlock2Expr")
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
     * Returns sorted tests, all tests are sequentially grouped by {@link MergedContextConfiguration} calculated
     * per each test class.
     * Has side effect: saves static lastClassPerConfig in SmartDirtiesTestsHolder.
     *
     * @param testItems
     * @param testClassExtractor
     * @param <T>
     * @return
     */
    public <T> List<T> sort(List<T> testItems, TestClassExtractor<T> testClassExtractor) {
        List<T> initialSorted = initialSorted(testItems, testClassExtractor);

        Set<Class<?>> itClasses = new LinkedHashSet<>();
        for (T t : initialSorted) {
            Class<?> testClass = testClassExtractor.getTestClass(t);
            if (!itClasses.contains(testClass) && isReorderTest(testClass)) {
                itClasses.add(testClass);
            }
        }
        if (!itClasses.isEmpty()) {
            printSuiteTests(initialSorted.size(), itClasses);
        }

        Map<MergedContextConfiguration, TestClasses> configToTests = new LinkedHashMap<>();
        List<T> reordered = initialSorted.stream()
            .sorted(comparing(testItem -> {
                Class<?> realClass = testClassExtractor.getTestClass(testItem);
                if (itClasses.contains(realClass)) {
                    TestContextBootstrapper bootstrapper =
                        BootstrapUtilsHelper.resolveTestContextBootstrapper(realClass);
                    MergedContextConfiguration mergedContextConfiguration =
                        bootstrapper.buildMergedContextConfiguration();
                    // Sequentially each unique mergedContextConfiguration will have own order
                    // via configToTests current size
                    TestClasses testClasses = configToTests.computeIfAbsent(mergedContextConfiguration,
                        $ -> new TestClasses(configToTests.size() + 1, new LinkedHashSet<>()));
                    testClasses.classes().add(realClass);
                    // this sorting is stable - most of the tests will preserve alphabetical ordering where possible
                    return testClasses.order();
                } else {
                    // all non-IT tests go first (other returned values are non-zero)
                    // this logic can be changed via override
                    return getNonItOrder();
                }
            }))
            .collect(Collectors.toList());

        if (!configToTests.isEmpty()) {
            printSuiteTestsPerConfig(configToTests);
        }

        Set<Class<?>> lastClassPerConfig = configToTests.values().stream()
            .map(testClasses -> getLast(testClasses.classes()))
            .collect(Collectors.toSet());
        SmartDirtiesTestsHolder.setLastClassPerConfig(lastClassPerConfig);

        return reordered;
    }

    /**
     * Get the order of non-integration test execution (bigger is later). Can be either first or last. 0 (first) by
     * default.
     */
    protected int getNonItOrder() {
        return 0;
    }

    protected <T> List<T> initialSorted(List<T> testItems, TestClassExtractor<T> testClassExtractor) {
        return testItems.stream()
            .sorted(comparing(testItem -> {
                return testClassExtractor.getTestClass(testItem).getName();
            }))
            .collect(Collectors.toList());
    }

    private static <T> T getLast(Iterable<T> iterable) {
        Iterator<T> iterator = iterable.iterator();
        T elem = iterator.next();
        while (iterator.hasNext()) {
            elem = iterator.next();
        }
        return elem;
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
        ExtendWith extendWith = AnnotatedElementUtils.findMergedAnnotation(testClass, ExtendWith.class);
        if (extendWith == null) {
            return false;
        }
        Class<? extends Extension>[] extensions = extendWith.value();
        return Stream.of(extensions)
            .anyMatch(SpringExtension.class::isAssignableFrom);
    }

    private void printSuiteTests(int totalTests, Collection<Class<?>> itClasses) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        pw.println("Running suite of " + totalTests + " tests. Integration test classes " +
            "(" + itClasses.size() + "):");
        itClasses.forEach(pw::println);
        log.info(sw.toString());
    }

    private void printSuiteTestsPerConfig(Map<MergedContextConfiguration, TestClasses> configToTests) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        pw.println("Integration test classes grouped and reordered by MergedContextConfiguration " +
            "(" + configToTests.size() + "):");
        pw.println("------");
        configToTests.values().forEach(itClasses -> {
            for (Class<?> itClass : itClasses.classes()) {
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
}
