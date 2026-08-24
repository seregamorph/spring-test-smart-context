package com.github.seregamorph.testtimeline;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.lang.Nullable;

/**
 * Helper bean that tracks spring bootstrap and shutdown events.
 *
 * @author Sergey Chernov
 */
public class SpringContextEventTrackerListener implements ApplicationListener<ApplicationContextEvent> {

    @Nullable
    private static final Class<? extends ContextStoppedEvent> CONTEXT_PAUSED_EVENT_CLASS =
        tryClassForName("org.springframework.context.event.ContextPausedEvent", ContextStoppedEvent.class);

    @Nullable
    private static final Class<? extends ContextStartedEvent> CONTEXT_RESTARTED_EVENT_CLASS =
        tryClassForName("org.springframework.context.event.ContextRestartedEvent", ContextStartedEvent.class);

    private final String contextId;

    public SpringContextEventTrackerListener(String contextId) {
        this.contextId = contextId;
        onCreated();
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            onContextRefreshedEvent();
        } else if (event instanceof ContextClosedEvent) {
            onContextClosedEvent();
        } else if (CONTEXT_PAUSED_EVENT_CLASS != null && CONTEXT_PAUSED_EVENT_CLASS.isInstance(event)) {
            onContextPausedEvent();
        } else if (CONTEXT_RESTARTED_EVENT_CLASS != null && CONTEXT_RESTARTED_EVENT_CLASS.isInstance(event)) {
            onContextRestartedEvent();
        } else if (event instanceof ContextStartedEvent) {
            // condition should go after CONTEXT_RESTARTED_EVENT_CLASS
            onContextStartedEvent();
        } else if (event instanceof ContextStoppedEvent) {
            // condition should go after CONTEXT_PAUSED_EVENT_CLASS
            onContextStoppedEvent();
        }
    }

    private void onCreated() {
        EventTrackerSupport.contextCreated(contextId);
    }

    private void onContextRefreshedEvent() {
        EventTrackerSupport.trackEvent(contextId, ContextEventType.REFRESHED);
    }

    private void onContextStartedEvent() {
        EventTrackerSupport.trackEvent(contextId, ContextEventType.STARTED);
    }

    private void onContextStoppedEvent() {
        EventTrackerSupport.trackEvent(contextId, ContextEventType.STOPPED);
    }

    private void onContextClosedEvent() {
        EventTrackerSupport.trackEvent(contextId, ContextEventType.CLOSED);
    }

    private void onContextPausedEvent() {
        EventTrackerSupport.trackEvent(contextId, ContextEventType.PAUSED);
    }

    private void onContextRestartedEvent() {
        EventTrackerSupport.trackEvent(contextId, ContextEventType.RESTARTED);
    }

    @Nullable
    private static <T> Class<? extends T> tryClassForName(String className, Class<T> baseClass) {
        try {
            return Class.forName(className).asSubclass(baseClass);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
