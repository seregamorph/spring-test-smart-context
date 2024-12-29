/*
 * Copyright 2012-2024 the original author or authors.
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

import java.lang.reflect.Field;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.util.ReflectionUtils;

// Based on original code from
// https://github.com/spring-projects/spring-boot/tree/v3.3.7/spring-boot-project/spring-boot-test/src/main/java/org/springframework/boot/test/mock/mockito

/**
 * {@link TestExecutionListener} to enable {@link SmartMockBean @SmartMockBean} and {@link SmartSpyBean @SmartSpyBean} support. Also
 * triggers {@link MockitoAnnotations#openMocks(Object)} when any Mockito annotations used, primarily to allow
 * {@link Captor @Captor} annotations.
 * <p>
 * To use the automatic reset support of {@code @MockBean} and {@code @SpyBean}, configure
 * {@link ResetMocksTestExecutionListener} as well.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Moritz Halbritter
 * @see ResetMocksTestExecutionListener
 * @since 1.4.2
 */
public class SmartMockitoTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public final int getOrder() {
        // org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener.getOrder() + 1
        return 1951;
    }

    @Override
    public void prepareTestInstance(TestContext testContext) {
        postProcessFields(testContext, false);
    }

    @Override
    public void beforeTestMethod(TestContext testContext) {
        if (Boolean.TRUE.equals(
                testContext.getAttribute(DependencyInjectionTestExecutionListener.REINJECT_DEPENDENCIES_ATTRIBUTE))) {
            postProcessFields(testContext, true);
        }
    }

    private static void postProcessFields(TestContext testContext, boolean erase) {
        SmartDefinitionsParser parser = new SmartDefinitionsParser();
        parser.parse(testContext.getTestClass());
        if (!parser.getDefinitions().isEmpty()) {
            SmartMockitoPostProcessor postProcessor = testContext.getApplicationContext()
                    .getBean(SmartMockitoPostProcessor.class);
            for (Definition definition : parser.getDefinitions()) {
                Field field = parser.getField(definition);
                if (field != null) {
                    if (erase) {
                        ReflectionUtils.makeAccessible(field);
                        ReflectionUtils.setField(field, testContext.getTestInstance(), null);
                    }
                    postProcessor.inject(field, testContext.getTestInstance(), definition);
                }
            }
        }
    }
}
