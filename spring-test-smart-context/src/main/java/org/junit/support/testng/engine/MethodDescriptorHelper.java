package org.junit.support.testng.engine;

import org.junit.platform.engine.TestDescriptor;

public class MethodDescriptorHelper {

    public static boolean isMethodDescriptor(TestDescriptor testDescriptor) {
        return testDescriptor instanceof MethodDescriptor;
    }
}
