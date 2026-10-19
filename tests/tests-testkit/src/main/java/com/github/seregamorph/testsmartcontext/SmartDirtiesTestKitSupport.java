package com.github.seregamorph.testsmartcontext;

import java.util.List;

public final class SmartDirtiesTestKitSupport {

    static boolean isFirstClassPerConfig(Class<?> testClass) {
        // this method is only used in tests, so we don't need to be lenient there
        List<SmartDirtiesTestsSupport.ClassGroupState> classGroupStates =
            SmartDirtiesTestsSupport.getOrderStates(testClass);
        if (SmartDirtiesTestsSupport.isInnerClass(testClass) && classGroupStates.isEmpty()) {
            // @Nested classes without own context configuration
            return false;
        }
        if (classGroupStates.size() == 1) {
            SmartDirtiesTestsSupport.ClassGroupState classGroupState = classGroupStates.get(0);
            if (!classGroupState.discoveredItClasses.contains(testClass)) {
                throw new IllegalStateException("Test " + testClass + " is not recognized as integration test");
            }
            return classGroupState.discoveredItClasses.iterator().next() == testClass;
        } else {
            throw new IllegalStateException("Unexpected more than one matching test engine for " + testClass);
        }
    }


    static boolean isLastClassPerConfig(Class<?> testClass) {
        // this method is only used in tests, so we don't need to be lenient there
        List<SmartDirtiesTestsSupport.ClassGroupState> classGroupStates =
            SmartDirtiesTestsSupport.getOrderStates(testClass);
        if (SmartDirtiesTestsSupport.isInnerClass(testClass) && classGroupStates.isEmpty()) {
            // @Nested classes without own context configuration
            return false;
        }
        if (classGroupStates.size() == 1) {
            SmartDirtiesTestsSupport.ClassGroupState classGroupState = classGroupStates.get(0);
            if (!classGroupState.discoveredItClasses.contains(testClass)) {
                throw new IllegalStateException("Test " + testClass + " is not recognized as integration test");
            }
            Class<?> lastClass = null;
            for (Class<?> discoveredItClass : classGroupState.discoveredItClasses) {
                lastClass = discoveredItClass;
            }
            return lastClass == testClass;
        } else {
            throw new IllegalStateException("Unexpected more than one matching test engine for " + testClass);
        }
    }

    private SmartDirtiesTestKitSupport() {
    }
}
