package org.springframework.test.context;

/**
 * Accessor of spring-boot package visible utility
 */
public final class BootstrapUtilsHelper {

    public static TestContextBootstrapper resolveTestContextBootstrapper(Class<?> testClass) {
        // this utility becomes public since spring 6, but for spring 5 we call it from package-private accessor
        return BootstrapUtils.resolveTestContextBootstrapper(BootstrapUtils.createBootstrapContext(testClass));
    }

    private BootstrapUtilsHelper() {
    }
}
