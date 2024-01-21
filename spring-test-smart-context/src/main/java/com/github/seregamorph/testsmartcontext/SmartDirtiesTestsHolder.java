package com.github.seregamorph.testsmartcontext;

import java.util.LinkedHashMap;
import java.util.Map;

public class SmartDirtiesTestsHolder {

    private static Map<Class<?>, Boolean> lastClassPerConfig;

    static int lastClassPerConfigSize() {
        return lastClassPerConfig == null ? 0 : lastClassPerConfig.size();
    }

    static boolean isLastClassPerConfig(Class<?> testClass) {
        if (lastClassPerConfig == null) {
            throw new IllegalStateException("lastClassPerConfig is not initialized");
        }
        Boolean isLastClassPerConfig = lastClassPerConfig.get(testClass);
        if (isLastClassPerConfig == null) {
            throw new IllegalStateException("lastClassPerConfig is not defined for class "
                + testClass + ", it means that it was skipped on initial analysis. " +
                "Discovered classes: " + lastClassPerConfig.keySet());
        }
        return isLastClassPerConfig;
    }

    public static void setLastClassPerConfig(Map<Class<?>, Boolean> lastClassPerConfig) {
        SmartDirtiesTestsHolder.lastClassPerConfig = new LinkedHashMap<>(lastClassPerConfig);
    }
}
