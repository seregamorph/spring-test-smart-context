package com.github.seregamorph.testsmartcontext;

import java.util.List;

public final class SmartDirtiesTestKitSupport {

    static boolean isFirstClassPerConfig(Class<?> testClass) {
        if (SmartDirtiesTestsSupport.isInnerClass(testClass)) {
            // to support @Nested classes (without own context configuration)
            return false;
        }

        // this method is only used in tests, so we don't need to be lenient there
        List<SmartDirtiesTestsSupport.ClassOrderState> classOrderStates =
            SmartDirtiesTestsSupport.getOrderStates(testClass);
        if (classOrderStates.size() == 1) {
            SmartDirtiesTestsSupport.ClassOrderState classOrderState = classOrderStates.get(0);
            if (!classOrderState.isIntegrationTest) {
                throw new IllegalStateException("Test " + testClass + " is not recognized as integration test");
            }
            return classOrderState.isFirst;
        } else {
            throw new IllegalStateException("Unexpected more than one matching test engine for " + testClass);
        }
    }

    static boolean isLastClassPerConfig(Class<?> testClass) {
        return SmartDirtiesTestsSupport.isLastClassPerConfig(testClass);
    }

    private SmartDirtiesTestKitSupport() {
    }
}
