/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.seregamorph.testsmartcontext.mockbean;

import com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSupport;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.util.Assert;

// Based on original code from
// https://github.com/spring-projects/spring-boot/tree/v3.3.7/spring-boot-project/spring-boot-test/src/main/java/org/springframework/boot/test/mock/mockito

/**
 * A {@link ContextCustomizerFactory} to add {@link SmartMockBean} support.
 *
 * @author Sergey Chernov
 */
class SmartMockitoContextCustomizerFactory extends SmartDirtiesTestsSupport implements ContextCustomizerFactory {

    private static final Log LOG = LogFactory.getLog(SmartMockitoContextCustomizerFactory.class);

    @Override
    public ContextCustomizer createContextCustomizer(
            Class<?> testClass,
            List<ContextConfigurationAttributes> configAttributes
    ) {
        // We gather the explicit mock definitions here from all known test classes of the suite.
        Class<?> enclosingTestClass = getEnclosingClass(testClass);
        Set<Class<?>> testClasses = getIntegrationTestClasses(enclosingTestClass);
        if (testClasses == null) {
            // Non strict logic here as in some scenarios like running JUnit 4 tests from
            // IDEA can skip test ordering or evaluate ContextCustomizerFactory several times
            // (before and after test ordering).
            LOG.info("The SmartDirtiesTestsHolder.engineClassOrderStateMap is not initialized, "
                    + "fail over to the default behavior");
            SmartDefinitionsParser parser = new SmartDefinitionsParser();
            parser.parse(enclosingTestClass);
            return new SmartMockitoContextCustomizer(parser.getDefinitions());
        }

        Assert.state(testClasses.contains(enclosingTestClass), "The SmartDirtiesTestsHolder.engineClassOrderStateMap "
            + "does not contain current test class: " + enclosingTestClass.getName()
            + "\nKnown test classes: " + testClasses);

        // define all SmartMockBean/SmartSpyBean definitions for the whole suite
        DefinitionSet mergedDefinitions = new DefinitionSet();
        for (Class<?> clazz : testClasses) {
            SmartDefinitionsParser parser = new SmartDefinitionsParser();
            parser.parse(clazz);
            mergedDefinitions.addAll(parser.getDefinitions());
        }
        return new SmartMockitoContextCustomizer(mergedDefinitions);
    }

    private static Class<?> getEnclosingClass(Class<?> testClass) {
        if (isInnerClass(testClass)) {
            SmartDefinitionsParser parser = new SmartDefinitionsParser();
            parser.parse(testClass);
            if (!parser.getDefinitions().isEmpty()) {
                throw new IllegalStateException("Inner class " + testClass.getName()
                    + " contains @SmartMockBean or @SmartSpyBean definitions, "
                    + "it's not supported with current implementation.");
            }
            return getEnclosingClass(testClass.getEnclosingClass());
        }
        return testClass;
    }
}
