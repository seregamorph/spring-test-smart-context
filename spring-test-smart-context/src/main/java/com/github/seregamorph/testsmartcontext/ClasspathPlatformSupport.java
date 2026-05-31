package com.github.seregamorph.testsmartcontext;

import org.springframework.util.ClassUtils;

/**
 * @author Sergey Chernov
 */
final class ClasspathPlatformSupport {

    private static final boolean JUNIT4_PRESENT = isClassPresent(
        "org.junit.runner.RunWith");

    private static final boolean JUNIT_JUPITER_API_PRESENT = isClassPresent(
        "org.junit.jupiter.api.extension.ExtendWith");

    @Deprecated
    static boolean isJunit4Present() {
        return JUNIT4_PRESENT;
    }

    static boolean isJunitJupiterApiPresent() {
        return JUNIT_JUPITER_API_PRESENT;
    }

    private static boolean isClassPresent(String className) {
        return ClassUtils.isPresent(className, ClasspathPlatformSupport.class.getClassLoader());
    }

    private ClasspathPlatformSupport() {
    }
}
