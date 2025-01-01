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

    private static final ThreadLocal<Stack<Class<?>>> currentTestClassStack = new InheritableThreadLocal<>();

    private static volatile Class<?> globalCurrentTestClass;

    @Nullable
    public static Class<?> getCurrentTestClass() {
        // ThreadLocal is always more precise and we claim that parallel test execution is not supported,
        // but with global reference failover we can call this from any thread
        Stack<Class<?>> stack = currentTestClassStack.get();
        return stack == null ? globalCurrentTestClass : stack.peek();
    }

    @Nullable
    public static String getCurrentTestClassName() {
        Class<?> currentTestClass = getCurrentTestClass();
        return currentTestClass == null ? null : currentTestClass.getName();
    }

    static void pushCurrentTestClass(Class<?> testClass) {
        Stack<Class<?>> stack = currentTestClassStack.get();
        if (stack == null) {
            stack = new Stack<>();
            currentTestClassStack.set(stack);
        }
        stack.push(testClass);
        globalCurrentTestClass = testClass;
    }

    static void popCurrentTestClass() {
        Stack<Class<?>> stack = currentTestClassStack.get();
        if (stack != null) {
            stack.pop();
            if (stack.isEmpty()) {
                currentTestClassStack.remove();
                globalCurrentTestClass = null;
            } else {
                globalCurrentTestClass = stack.peek();
            }
        }
    }

    private CurrentTestContext() {
    }
}
