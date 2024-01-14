package com.github.seregamorph.testsmartcontext.testng;

import static java.util.Comparator.comparing;

import com.github.seregamorph.testsmartcontext.SmartDirtiesContextTestExecutionListener;
import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsHolder;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.context.BootstrapUtilsHelper;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextBootstrapper;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.IAlterSuiteListener;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.internal.RuntimeBehavior;
import org.testng.xml.XmlSuite;

/**
 * See description in {@link SmartDirtiesContextTestExecutionListener}.
 * <p>
 * Reorders tests in suite grouping ITs with the same context configuration to minimize number of parallel existing
 * contexts.
 */
@SuppressWarnings("CodeBlock2Expr")
public class SmartDirtiesSuiteListener implements IAlterSuiteListener, IMethodInterceptor {

    private static final Log LOGGER = LogFactory.getLog(SmartDirtiesSuiteListener.class);

    @Override
    public void alter(List<XmlSuite> suites) {
        if (RuntimeBehavior.isDryRun()) {
            // the list of test classes is wrong, listener is called per each class as single in suite
            return;
        }
        // TestNG needs it (otherwise reorders back to default alphabetical order)
        suites.forEach(suite -> {
            suite.getTests().forEach(xmlTest -> {
                xmlTest.setPreserveOrder(false);
            });
        });
    }

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        if (RuntimeBehavior.isDryRun()) {
            // the list of test classes is wrong, listener is called per each class as single in suite
            return methods;
        }
        // this intercept method is executed by TestNG before running the suite (both IDEA and maven)
        Set<Class<?>> itClasses = new LinkedHashSet<>();
        Map<MergedContextConfiguration, TestClasses> configToTests = new LinkedHashMap<>();
        List<IMethodInstance> reordered = methods.stream()
            .sorted(comparing(methodInstance -> {
                return methodInstance.getMethod().getTestClass().getRealClass().getName();
            }))
            .sorted(comparing(methodInstance -> {
                ITestClass testClass = methodInstance.getMethod().getTestClass();
                Class<?> realClass = testClass.getRealClass();
                if (isReorderTest(realClass)) {
                    itClasses.add(realClass);
                    TestContextBootstrapper bootstrapper =
                        BootstrapUtilsHelper.resolveTestContextBootstrapper(realClass);
                    MergedContextConfiguration mergedContextConfiguration =
                        bootstrapper.buildMergedContextConfiguration();
                    // each unique mergedContextConfiguration will have sequential 1-based order value via
                    // "configToTests.size() + 1" lambda
                    TestClasses testClasses = configToTests.computeIfAbsent(mergedContextConfiguration,
                        $ -> new TestClasses(configToTests.size() + 1, new LinkedHashSet<>()));
                    testClasses.classes().add(realClass);
                    // this sorting is stable - most of the tests will preserve alphabetical ordering where possible
                    return testClasses.order();
                } else {
                    // all non-IT tests go first (other returned values are non-zero)
                    return 0;
                }
            }))
            .collect(Collectors.toList());

        if (!itClasses.isEmpty()) {
            printSuiteTests(itClasses);
        }

        if (!configToTests.isEmpty()) {
            printSuiteTestsPerConfig(configToTests);
        }

        Set<Class<?>> lastClassPerConfig = configToTests.values().stream()
            .map(testClasses -> getLast(testClasses.classes()))
            .collect(Collectors.toSet());
        SmartDirtiesTestsHolder.setLastClassPerConfig(lastClassPerConfig);

        return reordered;
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
        return !Modifier.isAbstract(testClass.getModifiers())
            && AbstractTestNGSpringContextTests.class.isAssignableFrom(testClass);
    }

    private static void printSuiteTests(Collection<Class<?>> itClasses) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        pw.println("Running suite of tests (" + itClasses.size() + ")");
        itClasses.forEach(pw::println);
        LOGGER.info(sw.toString());
    }

    private static void printSuiteTestsPerConfig(Map<MergedContextConfiguration, TestClasses> configToTests) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        pw.println("Tests grouped and reordered by MergedContextConfiguration (" + configToTests.size() + ")");
        pw.println("------");
        configToTests.values().forEach(itClasses -> {
            for (Class<?> itClass : itClasses.classes()) {
                pw.println(itClass.getName());
            }
            pw.println("------");
        });
        LOGGER.info(sw.toString());
    }
}
