package com.github.seregamorph.testsmartcontext;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * Integration Test class filter
 *
 * @author Sergey Chernov
 */
public abstract class IntegrationTestFilter {

    protected static <T extends IntegrationTestFilter> T initInstance(Class<T> type, Supplier<T> defaultSupplier) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(type, type.getClassLoader());
        Iterator<T> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return defaultSupplier.get();
        }
    }

    /**
     * Returns true if testClass is a Spring integration test class
     *
     * @param testClass
     * @return
     */
    protected abstract boolean isIntegrationTest(Class<?> testClass);

    @Override
    public String toString() {
        return getClass().getName();
    }

    public static class NoOpIntegrationTestFilter extends IntegrationTestFilter {
        @Override
        protected boolean isIntegrationTest(Class<?> testClass) {
            return true;
        }
    }
}
