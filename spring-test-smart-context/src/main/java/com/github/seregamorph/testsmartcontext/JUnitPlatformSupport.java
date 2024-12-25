package com.github.seregamorph.testsmartcontext;

import org.springframework.util.ClassUtils;

/**
 * @author Sergey Chernov
 */
final class JUnitPlatformSupport {

    @Deprecated
    private static final boolean JUNIT_VINTAGE_ENGINE_PRESENT = isClassPresent(
        "org.junit.vintage.engine.descriptor.RunnerTestDescriptor");

    @Deprecated
    private static final boolean JUNIT4_PRESENT = isClassPresent(
        "org.junit.runner.RunWith");

    private static final boolean JUNIT5_JUPITER_API_PRESENT = isClassPresent(
        "org.junit.jupiter.api.extension.ExtendWith");

    private static final boolean JUNIT4_IDEA_TEST_RUNNER_PRESENT = isClassPresent(
        "com.intellij.junit4.JUnit4IdeaTestRunner");

    @Deprecated
    static boolean isJUnitVintageEnginePresent() {
        return JUNIT_VINTAGE_ENGINE_PRESENT;
    }

    @Deprecated
    static boolean isJunit4Present() {
        return JUNIT4_PRESENT;
    }

    static boolean isJunit5JupiterApiPresent() {
        return JUNIT5_JUPITER_API_PRESENT;
    }

    @Deprecated
    static boolean isJUnit4IdeaTestRunnerPresent() {
        return JUNIT4_IDEA_TEST_RUNNER_PRESENT;
    }

    private static boolean isClassPresent(String className) {
        return ClassUtils.isPresent(className, JUnitPlatformSupport.class.getClassLoader());
    }

    private JUnitPlatformSupport() {
    }
}
