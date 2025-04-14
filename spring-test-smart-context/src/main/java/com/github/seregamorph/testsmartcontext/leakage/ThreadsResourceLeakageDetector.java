package com.github.seregamorph.testsmartcontext.leakage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author Sergey Chernov
 */
public class ThreadsResourceLeakageDetector extends ResourceLeakageDetector implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ThreadsResourceLeakageDetector.class);

    private final ThreadMXBean threadMXBean;
    @Nullable
    private final PrintWriter leakageOut;

    private volatile Set<Long> threadIdsBeforeClassGroup;

    public ThreadsResourceLeakageDetector() {
        super(Arrays.asList("threads", "daemonThreads"));
        threadMXBean = ManagementFactory.getThreadMXBean();

        /*@Nullable*/
        File reportsBaseDir = ResourceLeakageUtils.getReportsBaseDir();
        if (reportsBaseDir == null) {
            leakageOut = null;
        } else {
            File file = new File(reportsBaseDir, "threads-leakage.txt");
            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(file, false);
            } catch (FileNotFoundException e) {
                throw new UncheckedIOException(e);
            }
            leakageOut = new PrintWriter(new OutputStreamWriter(fileOutputStream, UTF_8), true);
        }
    }

    @Override
    public Map<String, Long> getIndicators() {
        Map<String, Long> map = new HashMap<>();
        map.put("threads", (long) threadMXBean.getThreadCount());
        map.put("daemonThreads", (long) threadMXBean.getDaemonThreadCount());
        return map;
    }

    @Override
    public void handleBeforeClassGroup() {
        super.handleBeforeClassGroup();
        threadIdsBeforeClassGroup = getAllThreadIds();
    }

    @Override
    public void handleAfterClassGroup() {
        Set<Long> threadIdsBeforeClassGroup = this.threadIdsBeforeClassGroup;
        if (threadIdsBeforeClassGroup != null) {
            this.threadIdsBeforeClassGroup = null;

            Set<Long> threadIds = getAllThreadIds();
            threadIds.removeAll(threadIdsBeforeClassGroup);

            if (!threadIds.isEmpty() && leakageOut != null) {
                leakageOut.println("Classes " + testClasses);
                leakageOut.println("New threads number: " + threadIds.size());
                ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds.stream().mapToLong(Long::longValue).toArray(), 64);
                leakageOut.println("New threads:");
                for (ThreadInfo threadInfo : threadInfos) {
                    leakageOut.println("---");
                    leakageOut.println(threadInfo.getThreadId() + ": " + threadInfo.getThreadName());
                    for (StackTraceElement stackTraceElement : threadInfo.getStackTrace()) {
                        leakageOut.println(stackTraceElement);
                    }
                }
                leakageOut.println();
                leakageOut.println("--");
                leakageOut.println();
            }
        } else {
            logger.warn("threadIdsBeforeClassGroup not initialized");
        }
        super.handleAfterClassGroup();
    }

    @Override
    public void close() {
        leakageOut.close();
    }

    private Set<Long> getAllThreadIds() {
        Set<Long> threadIds = new LinkedHashSet<>();
        for (long threadId : threadMXBean.getAllThreadIds()) {
            threadIds.add(threadId);
        }
        return threadIds;
    }
}
