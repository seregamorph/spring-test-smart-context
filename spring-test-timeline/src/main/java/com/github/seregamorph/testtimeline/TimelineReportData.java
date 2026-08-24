package com.github.seregamorph.testtimeline;

import java.math.BigDecimal;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Chernov
 */
public final class TimelineReportData {

    private final Meta meta;
    private final List<ContextData> contexts;
    private final List<Metric> metrics;

    public TimelineReportData(Meta meta, List<ContextData> contexts, List<Metric> metrics) {
        this.meta = meta;
        this.contexts = contexts;
        this.metrics = metrics;
    }

    public Meta getMeta() {
        return meta;
    }

    public List<ContextData> getContexts() {
        return contexts;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public static final class Meta {

        private final int threads;
        private final BigDecimal duration;
        // todo serialDuration
        private final int contexts;

        public Meta(int threads, BigDecimal duration, int contexts) {
            this.threads = threads;
            this.duration = duration;
            this.contexts = contexts;
        }

        public int getThreads() {
            return threads;
        }

        public BigDecimal getDuration() {
            return duration;
        }

        public int getContexts() {
            return contexts;
        }
    }

    public static final class ContextData {

        private final String contextId;
        private final List<ContextEvent> events;

        public ContextData(String contextId, List<ContextEvent> events) {
            this.contextId = contextId;
            this.events = events;
        }

        public String getContextId() {
            return contextId;
        }

        public List<ContextEvent> getEvents() {
            return events;
        }
    }

    public static final class ContextEvent {

        private final String type;
        private final int threadId;
        private final String testClassName;
        private final BigDecimal start;
        private final BigDecimal end;
        private final BigDecimal duration;

        public ContextEvent(String type, int threadId, String testClassName, BigDecimal start, BigDecimal end) {
            this.type = type;
            this.threadId = threadId;
            this.testClassName = testClassName;
            this.start = start;
            this.end = end;
            this.duration = end.subtract(start);
        }

        public String getType() {
            return type;
        }

        public int getThreadId() {
            return threadId;
        }

        public String getTestClassName() {
            return testClassName;
        }

        public BigDecimal getStart() {
            return start;
        }

        public BigDecimal getEnd() {
            return end;
        }

        public BigDecimal getDuration() {
            return duration;
        }
    }

    /**
     * A sampled metrics point at time {@code t} (seconds since build start).
     */
    public static final class Metric {

        private final BigDecimal t;
        private final int active;
        private final BigDecimal heapUsed;
        private final BigDecimal heapCommitted;
        private boolean gc;
        private final BigDecimal processCpu;
        private final BigDecimal systemCpu;
        // all live threads (daemon and non-daemon), so daemonThreads <= totalThreads
        private final int totalThreads;
        private final int daemonThreads;
        @Nullable
        private final Integer containersCount;

        public Metric(
            BigDecimal t,
            int active,
            BigDecimal heapUsed,
            BigDecimal heapCommitted,
            boolean gc,
            BigDecimal processCpu,
            BigDecimal systemCpu,
            int totalThreads,
            int daemonThreads,
            @Nullable Integer containersCount) {
            this.t = t;
            this.active = active;
            this.heapUsed = heapUsed;
            this.heapCommitted = heapCommitted;
            this.gc = gc;
            this.processCpu = processCpu;
            this.systemCpu = systemCpu;
            this.totalThreads = totalThreads;
            this.daemonThreads = daemonThreads;
            this.containersCount = containersCount;
        }

        public BigDecimal getT() {
            return t;
        }

        public int getActive() {
            return active;
        }

        public BigDecimal getHeapUsed() {
            return heapUsed;
        }

        public BigDecimal getHeapCommitted() {
            return heapCommitted;
        }

        public void setGc(boolean gc) {
            this.gc = gc;
        }

        public boolean isGc() {
            return gc;
        }

        public BigDecimal getProcessCpu() {
            return processCpu;
        }

        public BigDecimal getSystemCpu() {
            return systemCpu;
        }

        public int getTotalThreads() {
            return totalThreads;
        }

        public int getDaemonThreads() {
            return daemonThreads;
        }

        @Nullable
        public Integer getContainersCount() {
            return containersCount;
        }
    }
}
