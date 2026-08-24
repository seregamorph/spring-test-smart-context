package com.github.seregamorph.testtimeline;

import java.util.ArrayDeque;
import java.util.Deque;
import org.springframework.lang.Nullable;

/**
 * The state of currently executed test class of the current thread. Nested execution is supported: if a test class
 * starts another test class (e.g. a test that launches a nested test engine), the nested class becomes the current
 * one until it is finished, then the outer class becomes current again.
 *
 * @author Sergey Chernov
 */
public abstract class CurrentTestContextSupport {

    private static final ThreadLocal<Deque<Class<?>>> currentTestClass = new ThreadLocal<>();

    protected CurrentTestContextSupport() {
    }

    /**
     * Get currently executed test class. It's only defined for the thread executing the test class.
     *
     * @return current test class or null if not defined
     */
    @Nullable
    protected static Class<?> getCurrentTestClass() {
        Deque<Class<?>> stack = currentTestClass.get();
        return stack == null ? null : stack.peek();
    }

    /**
     * Internal API: mark the test class as started on the current thread.
     */
    protected static void pushCurrentTestClass(Class<?> testClass) {
        Deque<Class<?>> stack = currentTestClass.get();
        if (stack == null) {
            currentTestClass.set(stack = new ArrayDeque<>());
        }
        stack.push(testClass);
    }

    /**
     * Internal API: mark the test class as finished on the current thread. The class is only removed if it is
     * the current one, otherwise the call is ignored (the state is managed by another thread).
     *
     * @return true if the state was changed
     */
    protected static boolean popCurrentTestClass(Class<?> testClass) {
        Deque<Class<?>> stack = currentTestClass.get();
        if (stack == null || !testClass.equals(stack.peek())) {
            return false;
        }
        stack.pop();
        if (stack.isEmpty()) {
            currentTestClass.remove();
        }
        return true;
    }
}
