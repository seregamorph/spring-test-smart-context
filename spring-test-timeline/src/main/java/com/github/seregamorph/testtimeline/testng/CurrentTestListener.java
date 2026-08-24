package com.github.seregamorph.testtimeline.testng;

import com.github.seregamorph.testtimeline.CurrentTestContextSupport;
import org.testng.IClassListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.internal.RuntimeBehavior;

/**
 * Tracks the currently executed test class in {@link CurrentTestContextSupport}. The class-level callbacks (invoked before
 * {@code @BeforeClass} methods and after {@code @AfterClass} methods) are the primary source of the state, so
 * the current test class is already known while the spring context of this class is bootstrapping.
 * <p>
 * The test method callbacks are only a fallback for the case when the class-level callbacks were invoked on
 * another thread (e.g. {@code parallel="methods"} execution).
 *
 * @author Sergey Chernov
 */
public class CurrentTestListener extends CurrentTestContextSupport implements IClassListener, ITestListener {

    /**
     * Marker of {@link ITestResult} meaning that the current test class was pushed by the method-level callback,
     * hence it should be popped once the method is finished.
     */
    private static final String PUSHED_BY_METHOD = CurrentTestListener.class.getName() + ".pushedByMethod";

    @Override
    public void onBeforeClass(ITestClass testClass) {
        // dryRun is only true when called via junit platform testng-engine on discovery phase, there will be
        // subsequent call of this method with dryRun=false on execution phase
        if (RuntimeBehavior.isDryRun()) {
            return;
        }
        CurrentTestContextSupport.pushCurrentTestClass(testClass.getRealClass());
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
        if (RuntimeBehavior.isDryRun()) {
            return;
        }
        CurrentTestContextSupport.popCurrentTestClass(testClass.getRealClass());
    }

    @Override
    public void onTestStart(ITestResult result) {
        if (RuntimeBehavior.isDryRun()) {
            return;
        }
        Class<?> testClass = result.getTestClass().getRealClass();
        if (!testClass.equals(CurrentTestContextSupport.getCurrentTestClass())) {
            CurrentTestContextSupport.pushCurrentTestClass(testClass);
            result.setAttribute(PUSHED_BY_METHOD, Boolean.TRUE);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        onTestFinish(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        onTestFinish(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        onTestFinish(result);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        onTestFinish(result);
    }

    @Override
    public void onStart(ITestContext context) {
    }

    @Override
    public void onFinish(ITestContext context) {
    }

    private static void onTestFinish(ITestResult result) {
        if (Boolean.TRUE.equals(result.getAttribute(PUSHED_BY_METHOD))) {
            result.removeAttribute(PUSHED_BY_METHOD);
            CurrentTestContextSupport.popCurrentTestClass(result.getTestClass().getRealClass());
        }
    }
}
