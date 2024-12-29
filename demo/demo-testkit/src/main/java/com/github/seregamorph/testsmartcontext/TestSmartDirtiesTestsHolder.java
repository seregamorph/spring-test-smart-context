package com.github.seregamorph.testsmartcontext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.util.Assert;

class TestSmartDirtiesTestsHolder {

    static List<Class<?>> getIntegrationTestClasses(String engine) {
        Set<Class<?>> testClasses = SmartDirtiesTestsHolder.getTestClasses(engine);
        List<Class<?>> integrationTestClasses = new ArrayList<>();
        IntegrationTestFilter integrationTestFilter = IntegrationTestFilter.getInstance();
        Assert.state(testClasses != null, "Test classes are not initialized");
        for (Class<?> testClass : testClasses) {
            if (integrationTestFilter.isIntegrationTest(testClass)) {
                integrationTestClasses.add(testClass);
            }
        }
        return integrationTestClasses;
    }

    private TestSmartDirtiesTestsHolder() {
    }
}
