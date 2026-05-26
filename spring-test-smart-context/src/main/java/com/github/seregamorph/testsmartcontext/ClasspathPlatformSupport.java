package com.github.seregamorph.testsmartcontext;

import org.springframework.util.ClassUtils;

/**
 * @author Sergey Chernov
 */
final class ClasspathPlatformSupport {

    private static final boolean SPRING_BOOT_TEST_PRESENT = isClassPresent(
        "org.springframework.boot.test.context.SpringBootTest");

    @Deprecated
    private static final boolean JUNIT_VINTAGE_ENGINE_PRESENT = isClassPresent(
        "org.junit.vintage.engine.descriptor.RunnerTestDescriptor");

    @Deprecated
    private static final boolean JUNIT4_PRESENT = isClassPresent(
        "org.junit.runner.RunWith");

    private static final boolean JUNIT_JUPITER_API_PRESENT = isClassPresent(
        "org.junit.jupiter.api.extension.ExtendWith");

    private static final boolean JUNIT4_IDEA_TEST_RUNNER_PRESENT = isClassPresent(
        "com.intellij.junit4.JUnit4IdeaTestRunner");

    private static final boolean KOTEST_SPEC_PRESENT = isClassPresent(
        "io.kotest.core.spec.Spec");

    static boolean isSpringBootTestPresent() {
        return SPRING_BOOT_TEST_PRESENT;
    }

    @Deprecated
    static boolean isJUnitVintageEnginePresent() {
        return JUNIT_VINTAGE_ENGINE_PRESENT;
    }

    @Deprecated
    static boolean isJunit4Present() {
        return JUNIT4_PRESENT;
    }

    static boolean isJunitJupiterApiPresent() {
        return JUNIT_JUPITER_API_PRESENT;
    }

    @Deprecated
    static boolean isJUnit4IdeaTestRunnerPresent() {
        return JUNIT4_IDEA_TEST_RUNNER_PRESENT;
    }

    static boolean isKotestSpecPresent() {
        return KOTEST_SPEC_PRESENT;
    }

    private static boolean isClassPresent(String className) {
        return ClassUtils.isPresent(className, ClasspathPlatformSupport.class.getClassLoader());
    }

    private ClasspathPlatformSupport() {
    }
}
