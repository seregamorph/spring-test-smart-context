package com.github.seregamorph.testsmartcontext;

import java.util.HashSet;
import java.util.Set;

public class SmartDirtiesTestsHolder {

    private static Set<Class<?>> lastClassPerConfig;

    static boolean isLastClassPerConfig(Class<?> testClass) {
        if (lastClassPerConfig == null) {
            throw new IllegalStateException("lastClassPerConfig is not initialized");
        }
        return lastClassPerConfig.contains(testClass);
    }

    public static void setLastClassPerConfig(Set<Class<?>> lastClassPerConfig) {
        SmartDirtiesTestsHolder.lastClassPerConfig = new HashSet<>(lastClassPerConfig);
    }
}
