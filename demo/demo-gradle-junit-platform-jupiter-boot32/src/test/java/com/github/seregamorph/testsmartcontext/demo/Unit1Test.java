package com.github.seregamorph.testsmartcontext.demo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import org.junit.jupiter.api.Test;

public class Unit1Test {

    @Test
    public void test() {
        TestEventTracker.trackEvent("Running Unit1Test.test");
        assertTrue(true);
    }
}
