package com.github.seregamorph.testsmartcontext;

import org.springframework.util.ClassUtils;

/**
 * @author Sergey Chernov
 */
final class JUnitPlatformSupport {

    private static final boolean JUNIT4_PRESENT = isClassPresent(
        "org.junit.runner.RunWith");

    private static final boolean JUNIT5_JUPITER_API_PRESENT = isClassPresent(
        "org.junit.jupiter.api.extension.ExtendWith");

    private static final boolean JUNIT4_IDEA_TEST_RUNNER_PRESENT = isClassPresent(
        "com.intellij.junit4.JUnit4IdeaTestRunner");

    @Deprecated
    static boolean isJunit4Present() {
        return JUNIT4_PRESENT;
    }

    static boolean isJunit5JupiterApiPresent() {
        return JUNIT5_JUPITER_API_PRESENT;
    }

    private static boolean isClassPresent(String className) {
        return ClassUtils.isPresent(className, JUnitPlatformSupport.class.getClassLoader());
    }

    private JUnitPlatformSupport() {
    }
}
