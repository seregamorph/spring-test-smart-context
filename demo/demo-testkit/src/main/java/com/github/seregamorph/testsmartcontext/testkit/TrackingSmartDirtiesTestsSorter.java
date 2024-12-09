package com.github.seregamorph.testsmartcontext.testkit;

import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSorter;

public class TrackingSmartDirtiesTestsSorter extends SmartDirtiesTestsSorter {

    public TrackingSmartDirtiesTestsSorter() {
        System.out.println("Using TrackingSmartDirtiesTestsSorter");
    }
}
