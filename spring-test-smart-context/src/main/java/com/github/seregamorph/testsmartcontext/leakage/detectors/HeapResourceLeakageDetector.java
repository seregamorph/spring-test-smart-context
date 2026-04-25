package com.github.seregamorph.testsmartcontext.leakage.detectors;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sergey Chernov
 */
public class HeapResourceLeakageDetector extends ResourceLeakageDetector {

    private final MemoryMXBean memoryMXBean;

    public HeapResourceLeakageDetector() {
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
    }

    @Override
    public List<String> getIndicatorKeys() {
        return Arrays.asList("committed", "used");
    }

    @Override
    public Map<String, Number> getIndicators() {
        Map<String, Number> map = new LinkedHashMap<>();
        map.put("committed", memoryMXBean.getHeapMemoryUsage().getCommitted());
        map.put("used", memoryMXBean.getHeapMemoryUsage().getUsed());
//        map.put("init", memoryMXBean.getHeapMemoryUsage().getInit());
//        map.put("max", memoryMXBean.getHeapMemoryUsage().getMax());
        return map;
    }
}
