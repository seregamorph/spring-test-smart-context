package com.github.seregamorph.testsmartcontext.leakage.detectors;

import static com.github.seregamorph.testsmartcontext.leakage.detectors.LsofUtils.Filter.SERVER_SOCKET;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServerSocketResourceLeakageDetector extends ResourceLeakageDetector {

    private final long pid;

    public ServerSocketResourceLeakageDetector() {
        this(LsofUtils.getPid());
    }

    public ServerSocketResourceLeakageDetector(long pid) {
        this.pid = pid;
    }

    @Override
    public List<String> getIndicatorKeys() {
        return Arrays.asList("server_sockets");
    }

    @Override
    public Map<String, Number> getIndicators() {
        Map<String, Number> map = new LinkedHashMap<>();
        int serverSockets = LsofUtils.countListeningSockets(pid, SERVER_SOCKET);
        map.put("server_sockets", serverSockets);
        return map;
    }
}
