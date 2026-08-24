package com.github.seregamorph.testtimeline;

import com.github.dockerjava.api.model.Container;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;
import org.testcontainers.DockerClientFactory;

public class TestcontainersMetricsCollector {

    private static final Logger logger = LoggerFactory.getLogger(TestcontainersMetricsCollector.class);

    public static boolean isAvailable() {
        return ClassUtils.isPresent("org.testcontainers.DockerClientFactory", null);
    }

    public int getContainersCount() {
        String sessionId = DockerClientFactory.SESSION_ID;
        int containersCount;
        try {
            containersCount = countTestContainers(sessionId);
        } catch (IOException e) {
            logger.warn("Error while obtaining number of TestContainers", e);
            containersCount = 0;
        }
        return containersCount;
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
