package com.github.seregamorph.testsmartcontext.testkit;

import com.github.seregamorph.testsmartcontext.CurrentTestContext;
import com.github.seregamorph.testsmartcontext.SpringContextEventLoggerListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class TestContextEventTrackerListener extends SpringContextEventLoggerListener {

    private static final Class<? extends ApplicationContextEvent> CONTEXT_PAUSED_EVENT_CLASS =
        classForName("org.springframework.context.event.ContextPausedEvent", ApplicationContextEvent.class);

    private static final Class<? extends ApplicationContextEvent> CONTEXT_RESTARTED_EVENT_CLASS =
        classForName("org.springframework.context.event.ContextRestartedEvent", ApplicationContextEvent.class);

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (CONTEXT_PAUSED_EVENT_CLASS != null && CONTEXT_PAUSED_EVENT_CLASS.isInstance(event)) {
            TestEventTracker.trackEvent("Context paused for " + CurrentTestContext.getCurrentTestClassName());
        } else if (CONTEXT_RESTARTED_EVENT_CLASS != null && CONTEXT_RESTARTED_EVENT_CLASS.isInstance(event)) {
            TestEventTracker.trackEvent("Context restarted for " + CurrentTestContext.getCurrentTestClassName());
        } else {
            super.onApplicationEvent(event);
        }
    }

    @Override
    protected void onCreated() {
        TestEventTracker.trackEvent("Creating context for " + CurrentTestContext.getCurrentTestClassName());
    }

    @Override
    protected void onContextRefreshedEvent(ContextRefreshedEvent event) {
        super.onContextRefreshedEvent(event);
        boolean isChild = event.getApplicationContext().getParent() != null;
        TestEventTracker.trackEvent("Created " + (isChild ? "child context" : "context") + " for "
            + CurrentTestContext.getCurrentTestClassName());
    }

    @Override
    protected void onContextClosedEvent(ContextClosedEvent event) {
        String currentTestClass = CurrentTestContext.getCurrentTestClassName();
        boolean isChild = event.getApplicationContext().getParent() != null;
        var autoClosingContext = isCurrentAutoClosingContext();
        if (currentTestClass == null) {
            Assert.state(!autoClosingContext,
                "isCurrentAutoClosingContext is true while currentTestClass is not defined");
            // system shutdown hook
            TestEventTracker.trackEvent("Destroying " + (isChild ? "child context" : "context") + " (hook)");
        } else {
            // triggered via SmartDirtiesContextTestExecutionListener or spring DirtiesContextTestExecutionListener
            TestEventTracker.trackEvent((autoClosingContext ? "Auto-destroying " : "Destroying ")
                + (isChild ? "child context" : "context") + " for " + currentTestClass);
        }
    }

    @Nullable
    private static <T> Class<? extends T> classForName(String className, Class<T> baseClass) {
        try {
            return Class.forName(className).asSubclass(baseClass);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
