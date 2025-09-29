package com.github.seregamorph.testsmartcontext.leakage;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * @author Sergey Chernov
 */
public class ThreadsResourceLeakageDetector extends ResourceLeakageDetector implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ThreadsResourceLeakageDetector.class);

    // todo configurable
    private static final boolean CHECK_THREADS_TWICE = true;

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

                ThreadInfo[] originalThreadInfos = threadMXBean.getThreadInfo(threadIds.stream()
                    .mapToLong(Long::longValue).toArray(), 64);
                leakageOut.println("New threads:");

                ThreadInfo[] secondTryThreadInfos = originalThreadInfos;
                if (CHECK_THREADS_TWICE) {
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    // after sleeping some threads may be shut down (null) -
                    // monitor them separately and compare with originalThreadInfos
                    secondTryThreadInfos = threadMXBean.getThreadInfo(threadIds.stream()
                        .mapToLong(Long::longValue).toArray(), 64);
                }

                for (int i = 0; i < secondTryThreadInfos.length; i++) {
                    ThreadInfo threadInfo = secondTryThreadInfos[i];
                    if (threadInfo == null) {
                        threadInfo = originalThreadInfos[i];
                        if (threadInfo == null) {
                            // there can be a race between listing thread ids and getting thread info
                            leakageOut.println("---");
                            leakageOut.println("Thread is not found (probably already stopped), skipping");
                        } else {
                            leakageOut.println("---");
                            leakageOut.println("Thread " + threadInfo.getThreadId()
                                + " " + threadInfo.getThreadName() + " was shutdown without joining");
                        }
                    } else {
                        leakageOut.println("---");
                        leakageOut.println(threadInfo.getThreadId()
                            + " (" + threadInfo.getThreadState() + ")"
                            + ": \"" + threadInfo.getThreadName() + "\"");
                        for (StackTraceElement stackTraceElement : threadInfo.getStackTrace()) {
                            leakageOut.println(stackTraceElement);
                        }
                    }
                }

                leakageOut.println();
                leakageOut.println("--");
                leakageOut.println();
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
