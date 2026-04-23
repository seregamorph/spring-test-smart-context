package com.github.seregamorph.testsmartcontext.demo;

import com.github.seregamorph.testsmartcontext.testkit.TestEventTracker;
import org.junit.Test;

public class Unit1Test {

    @Test
    public void test() {
        TestEventTracker.trackEvent("Running Unit1Test");
    }
}
