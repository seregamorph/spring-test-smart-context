package com.github.seregamorph.testsmartcontext;

import java.util.LinkedHashMap;
import java.util.Map;

public class SmartDirtiesTestsHolder {

    private static Map<Class<?>, Boolean> lastClassPerConfig;

    static boolean isLastClassPerConfig(Class<?> testClass) {
        if (lastClassPerConfig == null) {
            throw new IllegalStateException("lastClassPerConfig is not initialized");
        }
        Boolean isLastClassPerConfig = lastClassPerConfig.get(testClass);
        if (isLastClassPerConfig == null) {
            throw new IllegalStateException("lastClassPerConfig is not defined for class "
                + testClass + ", it means that it was skipped on initial analysis. " +
                "Registered classes: " + lastClassPerConfig.keySet());
        }
        return isLastClassPerConfig;
    }

    public static boolean isLastClassPerConfigDefined() {
        return lastClassPerConfig != null;
    }

    public static void setLastClassPerConfig(Map<Class<?>, Boolean> lastClassPerConfig) {
        SmartDirtiesTestsHolder.lastClassPerConfig = new LinkedHashMap<>(lastClassPerConfig);
    }
}
