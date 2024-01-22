package com.github.seregamorph.testsmartcontext;

public abstract class CurrentTestContext {

    private static final ThreadLocal<String> currentTestClassIdentifier = new ThreadLocal<>();

    public static String getCurrentTestClassIdentifier() {
        return currentTestClassIdentifier.get();
    }

    protected static void setCurrentTestClassIdentifier(String testClassIdentifier) {
        currentTestClassIdentifier.set(testClassIdentifier);
    }

    protected static void resetCurrentTestClassIdentifier() {
        currentTestClassIdentifier.remove();
    }
}
