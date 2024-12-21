package com.github.seregamorph.testsmartcontext.testkit;

import com.github.seregamorph.testsmartcontext.CurrentTestContext;
import com.github.seregamorph.testsmartcontext.SpringContextEventLoggerListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

public class TestContextEventTrackerListener extends SpringContextEventLoggerListener {

    @Override
    protected void onCreated() {
        TestEventTracker.trackEvent("Creating context for " + CurrentTestContext.getCurrentTestClassName());
    }

    @Override
    protected void onContextRefreshedEvent(ContextRefreshedEvent event) {
        super.onContextRefreshedEvent(event);
        TestEventTracker.trackEvent("Created context for " + CurrentTestContext.getCurrentTestClassName());
    }

    @Override
    protected void onContextClosedEvent(ContextClosedEvent event) {
        String currentTestClass = CurrentTestContext.getCurrentTestClassName();
        if (currentTestClass == null) {
            // system shutdown hook
            TestEventTracker.trackEvent("Destroying context (hook)");
        } else {
            // triggered via SmartDirtiesContextTestExecutionListener or spring DirtiesContextTestExecutionListener
            TestEventTracker.trackEvent("Destroying context for " + currentTestClass);
        }
    }
}
