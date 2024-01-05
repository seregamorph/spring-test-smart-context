package com.github.seregamorph.testsmartcontext;

public abstract class CurrentTestContext {

    private static final ThreadLocal<Class<?>> currentTestClass = new ThreadLocal<>();

    public static Class<?> getCurrentTestClass() {
        return currentTestClass.get();
    }

    protected static void setCurrentTestClass(Class<?> testClass) {
        currentTestClass.set(testClass);
    }

    protected static void resetCurrentTestClass() {
        currentTestClass.remove();
    }
}
