package com.github.seregamorph.testsmartcontext.leakage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/*
threads
docker containers
heap memory
opened files
opened sockets
loaded classes
CPU history
 */
/**
 *
 * @author Sergey Chernov
 */
public abstract class ResourceLeakageDetector {

    private final List<String> indicatorKeys;

    List<Class<?>> testClasses;

    protected ResourceLeakageDetector(List<String> indicatorKeys) {
        this.indicatorKeys = Collections.unmodifiableList(new ArrayList<>(indicatorKeys));
    }

    public final List<String> getIndicatorKeys() {
        return indicatorKeys;
    }

    public abstract Map<String, Long> getIndicators();

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
