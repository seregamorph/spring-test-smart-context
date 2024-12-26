package com.github.seregamorph.testsmartcontext;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration Test class filter that should be ordered (as they use spring context). The logic of this class can be
 * customized via
 * <pre>
 * META-INF/services/com.github.seregamorph.testsmartcontext.IntegrationTestFilter
 * </pre>
 * defining subtype of this class overriding methods.
 *
 * @author Sergey Chernov
 */
public class IntegrationTestFilter {

    private static final IntegrationTestFilter instance = initInstance();

    private static IntegrationTestFilter initInstance() {
        ServiceLoader<IntegrationTestFilter> serviceLoader = ServiceLoader.load(IntegrationTestFilter.class,
            IntegrationTestFilter.class.getClassLoader());
        Iterator<IntegrationTestFilter> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return new IntegrationTestFilter();
        }
    }

    public static IntegrationTestFilter getInstance() {
        return instance;
    }

    protected IntegrationTestFilter() {
    }

    protected boolean isIntegrationTest(Class<?> testClass) {
        if (Modifier.isAbstract(testClass.getModifiers())) {
            return false;
        }

        if (ApplicationContextAware.class.isAssignableFrom(testClass)) {
            // Subtypes of org.springframework.test.context.testng.AbstractTestNGSpringContextTests
            // and org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests
            return true;
        }

        if (JUnitPlatformSupport.isJunit4Present() && isIntegrationTestJUnit4(testClass)) {
            return true;
        }

        //noinspection RedundantIfStatement
        if (JUnitPlatformSupport.isJunit5JupiterApiPresent() && isIntegrationTestJUnit5Jupiter(testClass)) {
            return true;
        }

        return false;
    }

    /**
     * This method should be only called if JUnit4 is on the classpath
     */
    protected boolean isIntegrationTestJUnit4(Class<?> testClass) {
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
    protected boolean isIntegrationTestJUnit5Jupiter(Class<?> testClass) {
        // can be inherited, can be meta-annotation e.g. via @SpringBootTest
        Set<ExtendWith> extendWith = AnnotatedElementUtils.findAllMergedAnnotations(testClass, ExtendWith.class);
        if (extendWith.isEmpty()) {
            return false;
        }

        return extendWith.stream()
            .map(ExtendWith::value)
            .flatMap(Arrays::stream)
            .anyMatch(SpringExtension.class::isAssignableFrom);
    }
}
