package com.github.seregamorph.testtimeline;

/**
 * @author Sergey Chernov
 */
public enum ContextEventType {

    /**
     * On bean creation
     */
    CREATING,
    /**
     * On ContextStartedEvent
     */
    @Deprecated // not used in tests
    STARTED,
    /**
     * On ContextRefreshedEvent event
     */
    REFRESHED,
    /**
     * On ContextPausedEvent (extends ContextStoppedEvent) event
     * Since Spring Framework 7.0
     */
    PAUSED,
    /**
     * On ContextRestartedEvent (extends ContextStartedEvent) event
     * Since Spring Framework 7.0
     */
    RESTARTED,
    /**
     * On ContextStoppedEvent event
     */
    @Deprecated // not used in tests
    STOPPED,
    /**
     * On ContextClosedEvent
     */
    CLOSED,
    /**
     * On bean destroy
     */
    DESTROYING
}
