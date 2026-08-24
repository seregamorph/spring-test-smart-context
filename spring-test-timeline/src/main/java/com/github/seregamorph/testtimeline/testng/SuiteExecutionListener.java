package com.github.seregamorph.testtimeline.testng;

import com.github.seregamorph.testtimeline.EventTrackerSupport;
import org.testng.IExecutionListener;
import org.testng.internal.RuntimeBehavior;

/**
 * Executed once before all tests and once after all tests of the TestNG execution (all suites).
 *
 * @author Sergey Chernov
 */
public class SuiteExecutionListener extends EventTrackerSupport implements IExecutionListener {

    @Override
    public void onExecutionStart() {
        // dryRun is only true when called via junit platform testng-engine on discovery phase, there will be subsequent
        // call of this method with dryRun=false on execution phase
        if (RuntimeBehavior.isDryRun()) {
            return;
        }
        System.out.println("TimelineExecutionListener.onExecutionStart");
        startEventTracking();
    }

    @Override
    public void onExecutionFinish() {
        if (RuntimeBehavior.isDryRun()) {
            return;
        }
        System.out.println("TimelineExecutionListener.onExecutionFinish");
        finishEventTracking();
    }
}
