package com.github.seregamorph.testsmartcontext.leakage.detectors;

import com.github.dockerjava.api.model.Container;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;

public class TestContainersResourceLeakageDetector extends ResourceLeakageDetector {

    private static final Logger logger = LoggerFactory.getLogger(TestContainersResourceLeakageDetector.class);

    @Override
    public List<String> getIndicatorKeys() {
        return Arrays.asList("containers");
    }

    @Override
    public Map<String, Number> getIndicators() {
        Map<String, Number> map = new LinkedHashMap<>();
        String sessionId = DockerClientFactory.SESSION_ID;
        int countersCount;
        try {
            countersCount = countTestContainers(sessionId);
        } catch (IOException e) {
            logger.warn("Error while obtaining number of TestContainers", e);
            countersCount = 0;
        }
        map.put("containers", countersCount);
        return map;
    }

    @SuppressWarnings("resource")
    private static int countTestContainers(String sessionId) throws IOException {
        List<Container> containers = DockerClientFactory.lazyClient()
            .listContainersCmd()
            .withShowAll(true)
            .withLabelFilter(Collections.singletonMap("org.testcontainers.sessionId", sessionId))
            .exec();
        return containers.size();
    }
}
