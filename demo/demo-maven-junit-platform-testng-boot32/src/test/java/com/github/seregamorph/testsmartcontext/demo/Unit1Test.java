package com.github.seregamorph.testsmartcontext.demo;

import static org.testng.Assert.assertTrue;

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import org.testng.annotations.Test;

public class Unit1Test {

    @Test
    public void test() {
        TestEventTracker.trackEvent("Running Unit1Test");
        assertTrue(true);
    }
}
