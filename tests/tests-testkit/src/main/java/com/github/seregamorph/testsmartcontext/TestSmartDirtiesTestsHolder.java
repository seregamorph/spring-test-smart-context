package com.github.seregamorph.testsmartcontext;

import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSupport.ClassGroupState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.util.Assert;

class TestSmartDirtiesTestsHolder {

    static List<Class<?>> getIntegrationTestClasses(String engine) {
        Map<Class<?>, ClassGroupState> testClasses = SmartDirtiesTestsSupport.getTestClasses(engine);
        List<Class<?>> integrationTestClasses = new ArrayList<>();
        Assert.state(testClasses != null, "Test classes are not initialized");
        for (Map.Entry<Class<?>, ClassGroupState> testClassEntry : testClasses.entrySet()) {
            if (testClassEntry.getValue().integrationTestFilter.isIntegrationTest(testClassEntry.getKey())) {
                integrationTestClasses.add(testClassEntry.getKey());
            }
        }
        return integrationTestClasses;
    }

    private TestSmartDirtiesTestsHolder() {
    }
}
