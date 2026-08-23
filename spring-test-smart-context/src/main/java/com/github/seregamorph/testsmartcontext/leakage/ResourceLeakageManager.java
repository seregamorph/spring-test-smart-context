package com.github.seregamorph.testsmartcontext.leakage;

import com.github.seregamorph.testsmartcontext.leakage.detectors.ClientSocketResourceLeakageDetector;
import com.github.seregamorph.testsmartcontext.leakage.detectors.HeapResourceLeakageDetector;
import com.github.seregamorph.testsmartcontext.leakage.detectors.ResourceLeakageDetector;
import com.github.seregamorph.testsmartcontext.leakage.detectors.ServerSocketResourceLeakageDetector;
import com.github.seregamorph.testsmartcontext.leakage.detectors.TestContainersResourceLeakageDetector;
import com.github.seregamorph.testsmartcontext.leakage.detectors.ThreadsResourceLeakageDetector;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

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
        this(getResourceLeakageDetectors());
    }

    private static List<ResourceLeakageDetector> getResourceLeakageDetectors() {
        // todo service discovery with priority
        List<ResourceLeakageDetector> list = new ArrayList<>();
        list.add(new ThreadsResourceLeakageDetector());
        list.add(new HeapResourceLeakageDetector());
        list.add(new ServerSocketResourceLeakageDetector());
        list.add(new ClientSocketResourceLeakageDetector());
        if (ClassUtils.isPresent("org.testcontainers.DockerClientFactory", null)) {
            list.add(new TestContainersResourceLeakageDetector());
        }
        return list;
    }

    private ResourceLeakageManager(List<ResourceLeakageDetector> resourceLeakageDetectors) {
        /*@Nullable*/
        File reportsBaseDir = ResourceLeakageUtils.getReportsBaseDir();

        this.resourceLeakageDetectors = resourceLeakageDetectors;
        //this.detectorExecutor = Executors.newFixedThreadPool(resourceLeakageDetectors.size());
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
        for (ResourceLeakageDetector resourceLeakageDetector : resourceLeakageDetectors) {
            resourceLeakageDetector.handleBeforeClassGroup();
        }
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
            Map<String, Number> indicators = new HashMap<>();
            long start = System.nanoTime();
            for (ResourceLeakageDetector detector : resourceLeakageDetectors) {
                indicators.putAll(detector.getIndicators());
            }
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            // TODO remove or logger
            System.out.println("getIndicators duration " + durationMs + "ms");
            resourceLeakageLogWriter.write(indicators, testClass, event,
                testGroupNumber.get(), testNumber.get());
        }
    }
}
