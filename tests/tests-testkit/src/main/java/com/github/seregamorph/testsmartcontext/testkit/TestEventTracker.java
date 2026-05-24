package com.github.seregamorph.testsmartcontext.testkit;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TestEventTracker {

    private static volatile TestEventTracker currentEventTracker;

    private final BlockingQueue<String> events = new LinkedBlockingQueue<>();

    public static void startTracking() {
        if (currentEventTracker != null) {
            throw new IllegalStateException("Tracker already started");
        }
        currentEventTracker = new TestEventTracker();
    }

    public static void stopTracking() {
        currentEventTracker = null;
    }

    public static void trackEvent(String event) {
        var testEventTracker = currentEventTracker;
        if (testEventTracker == null) {
            System.out.println("[" + Thread.currentThread().getName() + "] Event: " + event);
        } else {
            System.out.println("[" + Thread.currentThread().getName() + "] Tracked event: " + event);
            testEventTracker.events.add(event);
        }
    }

    public static void assertConsumedEvent(String expected) {
        var testEventTracker = currentEventTracker;
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
        var testEventTracker = currentEventTracker;
        if (testEventTracker == null) {
            throw new IllegalStateException("currentEventTracker not initialized");
        }
        if (!testEventTracker.events.isEmpty()) {
            throw new AssertionError("Expected empty, but found leftover events: " + testEventTracker.events);
        }
    }

    private TestEventTracker() {
    }
}
