package com.github.seregamorph.testsmartcontext.jupiter;

import com.github.seregamorph.testsmartcontext.IntegrationTestFilter;
import com.github.seregamorph.testsmartcontext.NestedTestClasses;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * JUnit Jupiter 5/6 integration Test class filter.
 * The logic of this class can be customized via
 * <pre>
 * META-INF/services/com.github.seregamorph.testsmartcontext.jupiter.JupiterIntegrationTestFilter
 * </pre>
 * defining subtype of this class overriding methods.
 *
 * @author Sergey Chernov
 */
public class JupiterIntegrationTestFilter extends IntegrationTestFilter {

    private static final JupiterIntegrationTestFilter instance = initInstance(JupiterIntegrationTestFilter.class,
        JupiterIntegrationTestFilter::new);

    public static IntegrationTestFilter getInstance() {
        return instance;
    }

    protected JupiterIntegrationTestFilter() {
    }

    @Override
    protected boolean isIntegrationTest(Class<?> testClass) {
        if (Modifier.isAbstract(testClass.getModifiers())) {
            return false;
        }
        if (isSpringTestClass(testClass)) {
            return true;
        }
        // enclosing class without Spring configuration, which declares @Nested Spring integration test classes
        return NestedTestClasses.getNestedTestClasses(testClass).stream()
            .anyMatch(this::isSpringTestClass);
    }

    @Override
    protected boolean isSpringTestClass(Class<?> testClass) {
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
