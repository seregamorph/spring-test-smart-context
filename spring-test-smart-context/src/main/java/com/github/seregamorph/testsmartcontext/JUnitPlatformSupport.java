package com.github.seregamorph.testsmartcontext;

import org.springframework.util.ClassUtils;

final class JUnitPlatformSupport {

    private static final boolean JUNIT_VINTAGE_ENGINE_PRESENT = isClassPresent(
        "org.junit.vintage.engine.descriptor.RunnerTestDescriptor");

    private static final boolean JUNIT_JUPITER_ENGINE_PRESENT = isClassPresent(
        "org.junit.jupiter.engine.descriptor.JupiterTestDescriptor");

    private static final boolean JUNIT_TESTNG_ENGINE_PRESENT = isClassPresent(
        "org.junit.support.testng.engine.ClassDescriptor");

    private static final boolean JUNIT4_PRESENT = isClassPresent(
        "org.junit.runner.RunWith");

    private static final boolean JUNIT5_JUPITER_API_PRESENT = isClassPresent(
        "org.junit.jupiter.api.extension.ExtendWith");

    static boolean isJUnitVintageEnginePresent() {
        return JUNIT_VINTAGE_ENGINE_PRESENT;
    }

    static boolean isJUnitJupiterEnginePresent() {
        return JUNIT_JUPITER_ENGINE_PRESENT;
    }

    static boolean isJUnitTestNGEnginePresent() {
        return JUNIT_TESTNG_ENGINE_PRESENT;
    }

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
