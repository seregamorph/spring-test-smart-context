package com.github.seregamorph.testsmartcontext.testkit;

import java.util.LinkedList;
import java.util.Queue;

public class TestEventTracker {

    private static final ThreadLocal<TestEventTracker> currentEventTracker = new ThreadLocal<>();

    private final Queue<String> events = new LinkedList<>();

    public static void startTracking() {
        currentEventTracker.set(new TestEventTracker());
    }

    public static void stopTracking() {
        currentEventTracker.remove();
    }

    public static void trackEvent(String event) {
        var testEventTracker = currentEventTracker.get();
        if (testEventTracker != null) {
            testEventTracker.addEvent(event);
        }
    }

    public static void assertConsumedEvent(String expected) {
        var testEventTracker = currentEventTracker.get();
        if (testEventTracker == null) {
            throw new IllegalStateException("currentEventTracker not initialized");
        }
        var actual = testEventTracker.events.poll();
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected [" + expected + "] but got [" + actual + "], " +
                    "leftover events: " + testEventTracker.events);
        }
    }

    public static void assertEmpty() {
        var testEventTracker = currentEventTracker.get();
        if (testEventTracker == null) {
            throw new IllegalStateException("currentEventTracker not initialized");
        }
        if (!testEventTracker.events.isEmpty()) {
            throw new AssertionError("Expected empty, but found leftover events: " + testEventTracker.events);
        }
    }

    private void addEvent(String event) {
        System.out.println("Event: " + event);
        events.add(event);
    }

    private TestEventTracker() {
    }
}
