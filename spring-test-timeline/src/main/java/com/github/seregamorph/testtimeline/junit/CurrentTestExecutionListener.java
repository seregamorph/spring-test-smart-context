package com.github.seregamorph.testtimeline.junit;

import com.github.seregamorph.testtimeline.CurrentTestContextSupport;
import java.util.HashSet;
import java.util.Set;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.springframework.lang.Nullable;

/**
 * Tracks the currently executed test class in {@link CurrentTestContextSupport}. It is auto-registered by the
 * JUnit Platform {@code Launcher} via {@code ServiceLoader}, so it receives events of all engines (Jupiter,
 * Vintage, TestNG engine etc.). The class-level events are received before {@code @BeforeAll} methods and after
 * {@code @AfterAll} methods, so the current test class is already known while the spring context of this class
 * is bootstrapping.
 * <p>
 * Nested (e.g. {@code @Nested}) classes are supported: while the nested class is executed, it is the current one.
 * The test method events are only a fallback for the case when the class-level events were received on another
 * thread (parallel execution).
 *
 * @author Sergey Chernov
 */
public class CurrentTestExecutionListener extends CurrentTestContextSupport implements TestExecutionListener {

    /**
     * Unique ids of the test identifiers that pushed the current test class on this thread, hence they should pop
     * it once finished.
     */
    private static final ThreadLocal<Set<String>> pushedIdentifiers = new ThreadLocal<>();

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        Class<?> testClass = getTestClass(testIdentifier);
        if (testClass == null || testClass.equals(getCurrentTestClass())) {
            // not a class-related identifier or the class is already current (e.g. the method of the current class)
            return;
        }
        pushCurrentTestClass(testClass);
        Set<String> pushed = pushedIdentifiers.get();
        if (pushed == null) {
            pushedIdentifiers.set(pushed = new HashSet<>());
        }
        pushed.add(testIdentifier.getUniqueId());
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        Set<String> pushed = pushedIdentifiers.get();
        if (pushed == null || !pushed.remove(testIdentifier.getUniqueId())) {
            return;
        }
        if (pushed.isEmpty()) {
            pushedIdentifiers.remove();
        }
        Class<?> testClass = getTestClass(testIdentifier);
        if (testClass != null) {
            popCurrentTestClass(testClass);
        }
    }

    @Nullable
    private static Class<?> getTestClass(TestIdentifier testIdentifier) {
        TestSource testSource = testIdentifier.getSource().orElse(null);
        if (testSource instanceof ClassSource) {
            return ((ClassSource) testSource).getJavaClass();
        } else if (testSource instanceof MethodSource) {
            return ((MethodSource) testSource).getJavaClass();
        } else {
            return null;
        }
    }
}
