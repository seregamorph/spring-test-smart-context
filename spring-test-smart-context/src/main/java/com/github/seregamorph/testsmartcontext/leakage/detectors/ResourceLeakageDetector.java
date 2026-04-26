package com.github.seregamorph.testsmartcontext.leakage.detectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
threads ThreadsResourceLeakageDetector
heap memory HeapResourceLeakageDetector
docker containers TestContainersResourceLeakageDetector
opened files
opened server sockets ServerSocketResourceLeakageDetector
opened client sockets ClientSocketResourceLeakageDetector
loaded classes
CPU history
 */
/**
 *
 * @author Sergey Chernov
 */
public abstract class ResourceLeakageDetector {

    List<Class<?>> testClasses;

    public abstract List<String> getIndicatorKeys();

    public abstract Map<String, Number> getIndicators();

    public void handleBeforeClassGroup() {
        this.testClasses = new ArrayList<>();
    }

    public void handleAfterClass(Class<?> testClass) {
        // testClasses can be null in case if a single test is executed
        if (this.testClasses != null) {
            this.testClasses.add(testClass);
        }
    }

    public void handleAfterClassGroup() {
        this.testClasses = null;
    }
}
