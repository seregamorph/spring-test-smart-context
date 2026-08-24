package com.github.seregamorph.testtimeline;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * @author Sergey Chernov
 */
public class MetricsCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsCollector.class);

    private static final long CYCLE_INTERVAL_MS = 250L;

    private final AtomicInteger activeContexts = new AtomicInteger(0);
    private final List<TimelineReportData.Metric> metrics = new ArrayList<>();

    /*
    TODO
    getClassLoadingMXBean
     */
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final List<GarbageCollectorMXBean> garbageCollectorMXBean = ManagementFactory.getGarbageCollectorMXBeans();
    private final OperatingSystemMXBean operatingSystemMXBean =
        (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    @Nullable
    private final TestcontainersMetricsCollector testcontainersMetricsCollector;

    private final long startNanos;
    private final Thread worker;

    // @GuardedBy("metrics")
    private boolean active = true;

    // @GuardedBy("metrics")
    private Long gcCount = null;

    // last valid CPU readings, carried forward whenever the bean reports a negative
    // @GuardedBy("metrics")
    private double lastSystemCpuLoad = 0d;

    MetricsCollector(long startNanos) {
        this.startNanos = startNanos;
        this.testcontainersMetricsCollector = TestcontainersMetricsCollector.isAvailable() ?
            new TestcontainersMetricsCollector() : null;
        // first metric
        TimelineReportData.Metric firstMetric;
        LOGGER.debug("Scraping first metric");
        synchronized (metrics) {
            firstMetric = scrapeMetrics();
        }
        worker = new Thread(() -> {
            synchronized (metrics) {
                metrics.add(firstMetric);
                while (active) {
                    try {
                        metrics.wait(CYCLE_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        return;
                    }
                    if (active) {
                        LOGGER.debug("Scraping {} metric", metrics.size());
                        try {
                            TimelineReportData.Metric metric = scrapeMetrics();
                            metrics.add(metric);
                        } catch (Throwable e) {
                            LOGGER.warn("Error while adding metric: {}", e.toString());
                        }
                    } else {
                        LOGGER.debug("Exit adding metric");
                    }
                }
            }
        });
    }

    private TimelineReportData.Metric scrapeMetrics() {
        long nowNanos = System.nanoTime();
        // getThreadCount() already includes the daemon ones, so daemonThreads <= totalThreads
        // (the two readings are not atomic, but they are only rendered, never subtracted)
        int daemonThreads = threadMXBean.getDaemonThreadCount();
        // there is a race condition of scraping getDaemonThreadCount / threadCount
        int totalThreads = Math.max(threadMXBean.getThreadCount(), daemonThreads);
        long heapUsedBytes = memoryMXBean.getHeapMemoryUsage().getUsed();
        long heapCommittedBytes = memoryMXBean.getHeapMemoryUsage().getCommitted();
        double processCpuLoad = operatingSystemMXBean.getProcessCpuLoad();
        if (Double.isNaN(processCpuLoad)) {
            processCpuLoad = 0d;
        }
        // Hint: the javadoc specifies possible negative value if it's not available,
        // but in practice it can be either 0.0d or NaN, this happened at least with Corretto
        double systemCpuLoad = operatingSystemMXBean.getSystemCpuLoad();
        systemCpuLoad = lastSystemCpuLoad = Double.isNaN(systemCpuLoad) || systemCpuLoad <= 0.0d
            ? lastSystemCpuLoad : systemCpuLoad;
        // there is a race condition between processCpuLoad and systemCpuLoad scrape, but normally
        // the systemCpuLoad should be never less than processCpuLoad
        systemCpuLoad = Math.max(systemCpuLoad, processCpuLoad);
        long gcCount = garbageCollectorMXBean.stream().mapToLong(GarbageCollectorMXBean::getCollectionCount).sum();
        boolean gc = this.gcCount != null && this.gcCount < gcCount;
        this.gcCount = gcCount;

        /*Nullable*/ Integer containersCount = testcontainersMetricsCollector == null ?
            null : testcontainersMetricsCollector.getContainersCount();

        return new TimelineReportData.Metric(
            fromStart(nowNanos),
            activeContexts.get(),
            megabytes(heapUsedBytes),
            megabytes(heapCommittedBytes),
            gc,
            cpuPercent(processCpuLoad),
            cpuPercent(systemCpuLoad),
            totalThreads,
            daemonThreads,
            containersCount
        );
    }

    public List<TimelineReportData.Metric> getMetrics() {
        synchronized (metrics) {
            active = false;
            List<TimelineReportData.Metric> result = new ArrayList<>(metrics);
            TimelineReportData.Metric lastMetric = scrapeMetrics();
            result.add(lastMetric);

            // shift "gc" one metric left as we register this after,
            // but visually it's more reasonable to be shown as before
            TimelineReportData.Metric prevMetric = null;
            for (TimelineReportData.Metric metric : result) {
                if (prevMetric != null) {
                    prevMetric.setGc(metric.isGc());
                }
                prevMetric = metric;
            }
            lastMetric.setGc(false);

            metrics.notify();
            return result;
        }
    }

    private BigDecimal fromStart(long timeNanos) {
        long durationNanos = timeNanos - startNanos;
        return TimeFormatUtils.nanosToSeconds(durationNanos);
    }

    private static BigDecimal megabytes(long bytes) {
        return BigDecimal.valueOf(bytes / 1024 / 1024);
    }

    private static BigDecimal cpuPercent(double cpuLoad) {
        double percent = cpuLoad < 0 ? 0d : cpuLoad * 100d;
        return BigDecimal.valueOf(percent).setScale(3, RoundingMode.HALF_UP);
    }

    public void start() {
        worker.start();
    }

    public void incActiveContexts() {
        activeContexts.incrementAndGet();
    }

    public void decActiveContexts() {
        activeContexts.decrementAndGet();
    }
}
