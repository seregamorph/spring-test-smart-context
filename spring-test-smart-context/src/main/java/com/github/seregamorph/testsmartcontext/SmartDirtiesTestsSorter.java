package com.github.seregamorph.testsmartcontext;

import static java.util.Comparator.comparing;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapUtilsHelper;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextAnnotationUtils;
import org.springframework.test.context.TestContextBootstrapper;

/**
 * The logic of this class can be customized via
 * <pre>
 * META-INF/services/com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSorter
 * </pre>
 * defining subtype of this class overriding methods.
 *
 * @author Sergey Chernov
 */
public class SmartDirtiesTestsSorter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final SmartDirtiesTestsSorter instance = initInstance();

    private static SmartDirtiesTestsSorter initInstance() {
        // subtypes can override methods for customization
        ServiceLoader<SmartDirtiesTestsSorter> loader = ServiceLoader.load(SmartDirtiesTestsSorter.class,
            SmartDirtiesTestsSorter.class.getClassLoader());

        Iterator<SmartDirtiesTestsSorter> iterator = loader.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
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
     * Sorts passed testItems (contains both integration and unit tests) in-place, all tests are sequentially grouped by
     * {@link MergedContextConfiguration} calculated per each test class.
     *
     * @param testItems
     * @param testClassExtractor
     * @return integration test classes lists grouped by config (non-integration tests classes not included)
     */
    public <T> List<List<Class<?>>> sort(List<T> testItems, TestClassExtractor<T> testClassExtractor) {
        initialSort(testItems, testClassExtractor);

        Set<Class<?>> itClasses = filterItClasses(testItems, testClassExtractor);
        if (!itClasses.isEmpty()) {
            printSuiteTests(testItems.size(), itClasses);
        }

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
                $ -> new TestClasses(orderCounter.addAndGet(3), new LinkedHashSet<>()));
            testClasses.classes.add(itClass);
            classToOrder.put(itClass, testClasses.order);
        }

        testItems.sort(comparing(testItem -> {
            Class<?> testClass = testClassExtractor.getTestClass(testItem);
            Integer order = classToOrder.get(testClass);
            if (order == null) {
                // all non-IT tests go first (other returned values are non-zero)
                // this logic can be changed via override
                return getNonItOrder();
            } else {
                // this sorting is stable - most of the tests will preserve alphabetical ordering where possible
                // we only sort classes that shut down the context to the end.
                return order + getDirtiesContextBeforeAfterOrder(testClass);
            }
        }));

        List<List<Class<?>>> sortedConfigToTests = configToTests.values().stream()
            .map(testClasses -> testClasses.classes.stream()
                .sorted(comparing(SmartDirtiesTestsSorter::getDirtiesContextBeforeAfterOrder))
                .collect(Collectors.toList()))
            .collect(Collectors.toList());

        if (!sortedConfigToTests.isEmpty()) {
            printSuiteTestsPerConfig(itClasses.size(), sortedConfigToTests);
        }

        return sortedConfigToTests;
    }

    private static <T> Set<Class<?>> filterItClasses(List<T> testItems, TestClassExtractor<T> testClassExtractor) {
        IntegrationTestFilter integrationTestFilter = IntegrationTestFilter.getInstance();
        Set<Class<?>> itClasses = new LinkedHashSet<>();
        for (T t : testItems) {
            Class<?> testClass = testClassExtractor.getTestClass(t);
            if (!itClasses.contains(testClass) && integrationTestFilter.isIntegrationTest(testClass)) {
                itClasses.add(testClass);
            }
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

    private static int getDirtiesContextBeforeAfterOrder(Class<?> testClass) {
        DirtiesContext dirtiesContext = TestContextAnnotationUtils.findMergedAnnotation(testClass, DirtiesContext.class);
        if (dirtiesContext != null) {
            if (dirtiesContext.classMode() == DirtiesContext.ClassMode.BEFORE_CLASS) {
                return -1;
            } else if (dirtiesContext.classMode() == DirtiesContext.ClassMode.AFTER_CLASS) {
                return 1;
            }
        }
        return 0;
    }

    private void printSuiteTests(int totalTests, Collection<Class<?>> itClasses) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        pw.println("Running suite of " + totalTests + " tests. Integration test classes " +
            "(" + itClasses.size() + " classes):");
        itClasses.forEach(pw::println);
        logger.debug(sw.toString());
    }

    private void printSuiteTestsPerConfig(int itClassesSize, List<List<Class<?>>> sortedConfigToTests) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        pw.println(itClassesSize + " integration test classes grouped and reordered by MergedContextConfiguration "
            + "(" + sortedConfigToTests.size() + " groups):");
        sortedConfigToTests.forEach(itClasses -> {
            pw.println("---");
            boolean isFirst = true;
            for (Iterator<Class<?>> it = itClasses.iterator(); it.hasNext(); ) {
                Class<?> itClass = it.next();
                boolean isLast = !it.hasNext();
                String suffix1 = isFirst && isLast ? "creates and closes context"
                    : isFirst ? "creates context" : isLast ? "closes context" : null;
                DirtiesContext dirtiesContext = AnnotatedElementUtils.findMergedAnnotation(itClass, DirtiesContext.class);
                String suffix2 = dirtiesContext == null ? null :
                    "marked @DirtiesContext(" + dirtiesContext.classMode().name() + ")";
                pw.print(itClass.getName());
                if (suffix1 != null || suffix2 != null) {
                    pw.print(" (" + (suffix1 == null ? "" : suffix1 + (suffix2 == null ? "" : "; "))
                        + (suffix2 == null ? "" : suffix2) + ")");
                }
                pw.println();
                isFirst = false;
            }
        });
        logger.info(sw.toString());
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
