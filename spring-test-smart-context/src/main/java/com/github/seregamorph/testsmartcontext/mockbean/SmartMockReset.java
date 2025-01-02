/*
 * Copyright 2012-2023 the original author or authors.
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

import org.mockito.MockSettings;
import org.mockito.MockingDetails;
import org.mockito.Mockito;
import org.mockito.listeners.InvocationListener;
import org.mockito.listeners.MethodInvocationReport;
import org.mockito.mock.MockCreationSettings;
import org.springframework.util.Assert;

import java.util.List;

// Based on original code from
// https://github.com/spring-projects/spring-boot/tree/v3.3.7/spring-boot-project/spring-boot-test/src/main/java/org/springframework/boot/test/mock/mockito

/**
 * Reset strategy used on a mock bean. Usually applied to a mock through the {@link SmartMockBean @SmartMockBean} annotation but
 * can also be directly applied to any mock in the {@code ApplicationContext} using the static methods.
 *
 * @author Phillip Webb
 * @see ResetMocksTestExecutionListener
 * @since 1.4.0
 */
public enum SmartMockReset {

    /**
     * Reset the mock before the test method runs.
     */
    BEFORE,

    /**
     * Reset the mock after the test method runs.
     */
    AFTER,

    /**
     * Don't reset the mock.
     */
    NONE;

    /**
     * Create {@link MockSettings settings} to be used with mocks where reset should occur before each test method
     * runs.
     *
     * @return mock settings
     */
    public static MockSettings before() {
        return withSettings(BEFORE);
    }

    /**
     * Create {@link MockSettings settings} to be used with mocks where reset should occur after each test method runs.
     *
     * @return mock settings
     */
    public static MockSettings after() {
        return withSettings(AFTER);
    }

    /**
     * Create {@link MockSettings settings} to be used with mocks where a specific reset should occur.
     *
     * @param reset the reset type
     * @return mock settings
     */
    public static MockSettings withSettings(SmartMockReset reset) {
        return apply(reset, Mockito.withSettings());
    }

    /**
     * Apply {@link SmartMockReset} to existing {@link MockSettings settings}.
     *
     * @param reset    the reset type
     * @param settings the settings
     * @return the configured settings
     */
    public static MockSettings apply(SmartMockReset reset, MockSettings settings) {
        Assert.notNull(settings, "Settings must not be null");
        if (reset != null && reset != NONE) {
            settings.invocationListeners(new ResetInvocationListener(reset));
        }
        return settings;
    }

    /**
     * Get the {@link SmartMockReset} associated with the given mock.
     *
     * @param mock the source mock
     * @return the reset type (never {@code null})
     */
    static SmartMockReset get(Object mock) {
        SmartMockReset reset = SmartMockReset.NONE;
        MockingDetails mockingDetails = Mockito.mockingDetails(mock);
        if (mockingDetails.isMock()) {
            MockCreationSettings<?> settings = mockingDetails.getMockCreationSettings();
            List<InvocationListener> listeners = settings.getInvocationListeners();
            for (Object listener : listeners) {
                if (listener instanceof ResetInvocationListener) {
                    ResetInvocationListener resetInvocationListener = (ResetInvocationListener) listener;
                    reset = resetInvocationListener.getReset();
                }
            }
        }
        return reset;
    }

    /**
     * Dummy {@link InvocationListener} used to hold the {@link SmartMockReset} value.
     */
    private static class ResetInvocationListener implements InvocationListener {

        private final SmartMockReset reset;

        ResetInvocationListener(SmartMockReset reset) {
            this.reset = reset;
        }

        SmartMockReset getReset() {
            return this.reset;
        }

        @Override
        public void reportInvocation(MethodInvocationReport methodInvocationReport) {
        }

    }

}
