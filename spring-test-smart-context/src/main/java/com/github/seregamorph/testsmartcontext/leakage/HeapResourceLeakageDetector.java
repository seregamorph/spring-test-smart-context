package com.github.seregamorph.testsmartcontext.leakage;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sergey Chernov
 */
public class HeapResourceLeakageDetector extends ResourceLeakageDetector {

    private final MemoryMXBean memoryMXBean;

    public HeapResourceLeakageDetector() {
        super(Arrays.asList("committed", "used"));
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
    }

    @Override
    public Map<String, Long> getIndicators() {
        Map<String, Long> map = new HashMap<>();
        map.put("committed", memoryMXBean.getHeapMemoryUsage().getCommitted());
        map.put("used", memoryMXBean.getHeapMemoryUsage().getUsed());
//        map.put("init", memoryMXBean.getHeapMemoryUsage().getInit());
//        map.put("max", memoryMXBean.getHeapMemoryUsage().getMax());
        return map;
    }
}
