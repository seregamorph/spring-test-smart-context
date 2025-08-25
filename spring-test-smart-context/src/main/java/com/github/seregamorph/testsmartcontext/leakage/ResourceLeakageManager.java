package com.github.seregamorph.testsmartcontext.leakage;

import org.springframework.lang.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 * @author Sergey Chernov
 */
public class ResourceLeakageManager {

    private final AtomicInteger testGroupNumber = new AtomicInteger();
    private final AtomicInteger testNumber = new AtomicInteger();

    private final List<ResourceLeakageDetector> resourceLeakageDetectors;
    @Nullable
    private final ResourceLeakageLogWriter resourceLeakageLogWriter;

    private static final ResourceLeakageManager instance = initInstance();

    private static ResourceLeakageManager initInstance() {
        return new ResourceLeakageManager();
    }

    private ResourceLeakageManager() {
        // todo service discovery with priority
        this(Arrays.asList(
            new ThreadsResourceLeakageDetector(),
            new HeapResourceLeakageDetector()
        ));
    }

    private ResourceLeakageManager(List<ResourceLeakageDetector> resourceLeakageDetectors) {
        /*@Nullable*/
        File reportsBaseDir = ResourceLeakageUtils.getReportsBaseDir();

        this.resourceLeakageDetectors = resourceLeakageDetectors;
        if (reportsBaseDir == null) {
            this.resourceLeakageLogWriter = null;
        } else {
            File outputFile = new File(reportsBaseDir, "report.csv");
            List<String> headers = resourceLeakageDetectors.stream()
                .flatMap(detector -> detector.getIndicatorKeys().stream())
                .collect(Collectors.toList());
            resourceLeakageLogWriter = new ResourceLeakageCsvLogWriter(outputFile, headers);
        }
    }

    public static ResourceLeakageManager getInstance() {
        return instance;
    }

    public void handleBeforeClassGroup() {
        testGroupNumber.incrementAndGet();
        resourceLeakageDetectors.forEach(ResourceLeakageDetector::handleBeforeClassGroup);
    }

    public void handleBeforeClass(Class<?> testClass) {
        testNumber.incrementAndGet();
        logIndicators(testClass, "BC");
    }

    public void handleAfterClass(Class<?> testClass) {
        logIndicators(testClass, "AC");
        for (ResourceLeakageDetector resourceLeakageDetector : resourceLeakageDetectors) {
            resourceLeakageDetector.handleAfterClass(testClass);
        }
    }

    public void handleAfterClassGroup(Class<?> testClass) {
        if (Boolean.getBoolean("testsmartcontext.handleAfterClassGroup.gc")) {
            System.gc();
        }
        logIndicators(testClass, "ACG");
        for (ResourceLeakageDetector resourceLeakageDetector : resourceLeakageDetectors) {
            resourceLeakageDetector.handleAfterClassGroup();
        }
    }

    private void logIndicators(Class<?> testClass, String event) {
        if (resourceLeakageLogWriter != null) {
            Map<String, Long> indicators = new HashMap<>();
            resourceLeakageDetectors.forEach(detector -> indicators.putAll(detector.getIndicators()));
            resourceLeakageLogWriter.write(indicators, testClass, event,
                testGroupNumber.get(), testNumber.get());
        }
    }
}
