package com.github.seregamorph.testsmartcontext.leakage.detectors;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LsofUtils {

    private static final Logger logger = LoggerFactory.getLogger(LsofUtils.class);

    enum Filter {
        SERVER_SOCKET("TCP:LISTEN", "(LISTEN)"),
        CLIENT_SOCKET("TCP:ESTABLISHED", "(ESTABLISHED)");

        private final String cmdFilter;
        private final String outputFilter;

        Filter(String cmdFilter, String outputFilter) {
            this.cmdFilter = cmdFilter;
            this.outputFilter = outputFilter;
        }
    }

    static int countListeningSockets(long pid, Filter filter) {
        try {
            // macOS-friendly
            String cmd = String.format("lsof -nP -iTCP -a -p %d -s" + filter.cmdFilter, pid);
            logger.debug("Executing '{}'", cmd);
            Process process = Runtime.getRuntime().exec(cmd);
            if (process.waitFor(5, TimeUnit.SECONDS)) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8))) {
                    int count = 0;
                    String line;
                    while ((line = in.readLine()) != null) {
                        // First line looks like
                        // "COMMAND   PID   USER   FD   TYPE             DEVICE SIZE/OFF NODE NAME"
                        // Other lines look like
                        // "java    86895 sergey   74u  IPv6 0xc1122215a0cdfb84      0t0  TCP 127.0.0.1:38437 (LISTEN)"
                        if (line.contains(filter.outputFilter)) {
                            count++;
                        }
                    }
                    return count;
                }
            } else {
                logger.warn("Timeout waiting for lsof output");
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("Failed to call lsof", e);
        }
        return 0;
    }

    static long getPid() {
        // e.g. "12345@hostname"
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(name.split("@")[0]);
    }

    private LsofUtils() {
    }
}
