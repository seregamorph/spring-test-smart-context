package com.github.seregamorph.testsmartcontext.leakage.detectors;

import static com.github.seregamorph.testsmartcontext.leakage.detectors.LsofUtils.Filter.CLIENT_SOCKET;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClientSocketResourceLeakageDetector extends ResourceLeakageDetector {

    private final long pid;

    public ClientSocketResourceLeakageDetector() {
        this(LsofUtils.getPid());
    }

    public ClientSocketResourceLeakageDetector(long pid) {
        this.pid = pid;
    }

    @Override
    public List<String> getIndicatorKeys() {
        return Arrays.asList("client_sockets");
    }

    @Override
    public Map<String, Number> getIndicators() {
        Map<String, Number> map = new LinkedHashMap<>();
        int serverSockets = LsofUtils.countListeningSockets(pid, CLIENT_SOCKET);
        map.put("client_sockets", serverSockets);
        return map;
    }
}
