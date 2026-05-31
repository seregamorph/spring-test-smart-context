package com.github.seregamorph.testsmartcontext.testng;

import com.github.seregamorph.testsmartcontext.IntegrationTestFilter;
import java.lang.reflect.Modifier;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

/**
 * TestNG integration Test class filter.
 * The logic of this class can be customized via
 * <pre>
 * META-INF/services/com.github.seregamorph.testsmartcontext.testng.TestNGIntegrationTestFilter
 * </pre>
 * defining subtype of this class overriding methods.
 *
 * @author Sergey Chernov
 */
public class TestNGIntegrationTestFilter extends IntegrationTestFilter {

    private static final TestNGIntegrationTestFilter instance = initInstance(TestNGIntegrationTestFilter.class,
        TestNGIntegrationTestFilter::new);

    public static IntegrationTestFilter getInstance() {
        return instance;
    }

    protected TestNGIntegrationTestFilter() {
    }

    @Override
    protected boolean isIntegrationTest(Class<?> testClass) {
        if (Modifier.isAbstract(testClass.getModifiers())) {
            return false;
        }

        return AbstractTestNGSpringContextTests.class.isAssignableFrom(testClass);
    }
}
