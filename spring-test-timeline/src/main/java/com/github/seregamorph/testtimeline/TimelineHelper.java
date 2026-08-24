package com.github.seregamorph.testtimeline;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

/**
 * @author Sergey Chernov
 */
class TimelineHelper {

    private static final Logger log = LoggerFactory.getLogger(TimelineHelper.class);

    private static class UndefinedTestClass {
    }

    private final AtomicInteger workerThreadCounter = new AtomicInteger();

    private final ThreadLocal<Integer> currentWorkerThreadId =
        ThreadLocal.withInitial(workerThreadCounter::getAndIncrement);

    private final long startNanos;
    private final MetricsCollector metricsCollector;

    /**
     * contextId -> ContextData
     */
    private final Map<String, ContextState> contextStates = new LinkedHashMap<>();

    static class ContextState {

        private final long createdNanos;
        private final int createdThreadId;
        private final String createdTestClassName;

        private final List<Event> events = new ArrayList<>();

        ContextState(long createdNanos, int createdThreadId, String createdTestClassName) {
            this.createdNanos = createdNanos;
            this.createdThreadId = createdThreadId;
            this.createdTestClassName = createdTestClassName;
        }
    }

    static class Event {

        private final long timestampNanos;
        private final int threadId;
        private final Class<?> testClass;
        private final ContextEventType eventType;

        Event(long timestampNanos, int threadId, Class<?> testClass, ContextEventType eventType) {
            this.timestampNanos = timestampNanos;
            this.threadId = threadId;
            this.testClass = testClass;
            this.eventType = eventType;
        }
    }

    TimelineHelper() {
        startNanos = System.nanoTime();
        metricsCollector = new MetricsCollector(startNanos);
    }

    void start() {
        metricsCollector.start();
    }

    void contextCreated(String contextId) {
        metricsCollector.incActiveContexts();
        Class<?> currentTestClass = getCurrentTestClass();
        synchronized (contextStates) {
            contextStates.computeIfAbsent(contextId,
                $ -> new ContextState(System.nanoTime(), currentWorkerThreadId.get(), currentTestClass.getName()));
        }
    }

    void addEvent(String contextId, ContextEventType eventType) {
        long nowNanos = System.nanoTime();
        Class<?> currentTestClass = getCurrentTestClass();
        Event event = new Event(nowNanos,
            currentWorkerThreadId.get(),
            currentTestClass,
            eventType);
        synchronized (contextStates) {
            ContextState contextState = contextStates.get(contextId);
            if (contextState == null) {
                log.warn("Missing ContextState for context {}", contextId);
            } else {
                contextState.events.add(event);
            }
        }
        if (eventType == ContextEventType.DESTROYING) {
            metricsCollector.decActiveContexts();
        }
    }

    @NonNull
    private static Class<?> getCurrentTestClass() {
        Class<?> currentTestClass = CurrentTestContextSupport.getCurrentTestClass();
        if (currentTestClass == null) {
            currentTestClass = UndefinedTestClass.class;
        }
        return currentTestClass;
    }

    private BigDecimal fromStartSec(long timeNanos) {
        long durationNanos = timeNanos - startNanos;
        return TimeFormatUtils.nanosToSeconds(durationNanos);
    }

    TimelineReportData generateReport() {
        long now = System.nanoTime();
        BigDecimal duration = fromStartSec(now);
        List<TimelineReportData.Metric> metrics = metricsCollector.getMetrics();
        synchronized (contextStates) {
            TimelineReportData.Meta meta = new TimelineReportData.Meta(workerThreadCounter.get(),
                duration, contextStates.size());
            List<TimelineReportData.ContextData> contexts = new ArrayList<>();
            contextStates.forEach((contextId, contextState) -> {
                List<TimelineReportData.ContextEvent> contextEvents = new ArrayList<>();
                long prevEventTimestampNanos = contextState.createdNanos;
                boolean keepAlive = false;
                for (Event event : contextState.events) {
                    if (event.eventType == ContextEventType.REFRESHED) {
                        keepAlive = true;
                    } else if (event.eventType == ContextEventType.DESTROYING) {
                        keepAlive = false;
                    }
                    TimelineReportData.ContextEvent contextEvent = new TimelineReportData.ContextEvent(
                            event.eventType.name().toLowerCase(Locale.ROOT), event.threadId,
                            event.testClass.getName(),
                            fromStartSec(prevEventTimestampNanos), fromStartSec(event.timestampNanos));
                    contextEvents.add(contextEvent);
                    prevEventTimestampNanos = event.timestampNanos;
                }
                if (keepAlive) {
                    contextEvents.add(new TimelineReportData.ContextEvent(
                        "keep_alive", contextState.createdThreadId,
                        contextState.createdTestClassName,
                        fromStartSec(prevEventTimestampNanos), fromStartSec(now)));
                }
                TimelineReportData.ContextData contextData = new TimelineReportData.ContextData(contextId, contextEvents);
                contexts.add(contextData);
            });
            return new TimelineReportData(meta, contexts, metrics);
        }
    }
}
