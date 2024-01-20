package org.junit.support.testng.engine;

import org.junit.platform.engine.TestDescriptor;

/**
 * Accessor of testng-engine package visible class.
 */
public class ClassDescriptorHelper {

    public static boolean isClassDescriptor(TestDescriptor testDescriptor) {
        return testDescriptor instanceof ClassDescriptor;
    }
}
