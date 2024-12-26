package com.github.seregamorph.testsmartcontext.demo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import org.junit.Test;

// JUnit 4
public class Unit1Test {

    @Test
    public void test() {
        TestEventTracker.trackEvent("Running " + getClass().getName() + ".test");
        assertTrue(true);
    }
}
