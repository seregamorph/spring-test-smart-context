package com.github.seregamorph.testsmartcontext.leakage;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Sergey Chernov
 */
public abstract class ResourceLeakageLogWriter implements Closeable {

    private final long startNanoTime = System.nanoTime();

    public abstract void write(
        Map<String, Long> indicators,
        Class<?> testClass,
        String event,
        int testGroupNumber,
        int testNumber
    );

    protected String getTimestamp() {
        long now = System.nanoTime();
        long totalSeconds = TimeUnit.NANOSECONDS.toSeconds(now - startNanoTime);

        return formatTimestamp(totalSeconds);
    }

    static String formatTimestamp(long totalSeconds) {
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds = totalSeconds / 60) % 60;
        long hours = totalSeconds / 60;
        return hours + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
    }
}
