package com.github.seregamorph.testsmartcontext;

import java.util.Stack;
import org.springframework.lang.Nullable;

public class CurrentTestContext {

    private static final ThreadLocal<Stack<Class<?>>> currentTestClassIdentifier = new ThreadLocal<>();

    @Nullable
    public static String getCurrentTestClassName() {
        Stack<Class<?>> stack = currentTestClassIdentifier.get();
        return stack == null ? null : stack.peek().getName();
    }

    static void pushCurrentTestClass(Class<?> testClass) {
        Stack<Class<?>> stack = currentTestClassIdentifier.get();
        if (stack == null) {
            stack = new Stack<>();
            currentTestClassIdentifier.set(stack);
        }
        stack.push(testClass);
    }

    static void popCurrentTestClass() {
        Stack<Class<?>> stack = currentTestClassIdentifier.get();
        if (stack != null) {
            stack.pop();
            if (stack.isEmpty()) {
                currentTestClassIdentifier.remove();
            }
        }
    }

    private CurrentTestContext() {
    }
}
