package com.github.seregamorph.testsmartcontext;

import java.util.Stack;
import org.springframework.lang.Nullable;

/**
 * Utility class to obtain current integration test class (executed by
 * {@link org.springframework.test.context.junit.jupiter.SpringExtension},
 * {@link org.springframework.test.context.junit4.SpringJUnit4ClassRunner} or extending
 * {@link org.springframework.test.context.testng.AbstractTestNGSpringContextTests} depending on test framework).
 *
 * @author Sergey Chernov
 */
public class CurrentTestContext {

    private static final ThreadLocal<Stack<Class<?>>> currentTestClass = new ThreadLocal<>();

    /**
     * Get current test class name. It's only defined for integration test classes and
     * for the same thread as test engine.
     */
    @Nullable
    public static String getCurrentTestClassName() {
        Stack<Class<?>> stack = currentTestClass.get();
        return stack == null ? null : stack.peek().getName();
    }

    static void pushCurrentTestClass(Class<?> testClass) {
        Stack<Class<?>> stack = currentTestClass.get();
        if (stack == null) {
            stack = new Stack<>();
            currentTestClass.set(stack);
        }
        stack.push(testClass);
    }

    static void popCurrentTestClass() {
        Stack<Class<?>> stack = currentTestClass.get();
        if (stack != null) {
            stack.pop();
            if (stack.isEmpty()) {
                currentTestClass.remove();
            }
        }
    }

    private CurrentTestContext() {
    }
}
