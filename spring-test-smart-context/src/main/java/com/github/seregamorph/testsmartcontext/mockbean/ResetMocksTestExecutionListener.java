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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.NativeDetector;
import org.springframework.core.Ordered;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.ClassUtils;

// Based on original code from
// https://github.com/spring-projects/spring-boot/tree/v3.3.7/spring-boot-project/spring-boot-test/src/main/java/org/springframework/boot/test/mock/mockito

/**
 * {@link TestExecutionListener} to reset any mock beans that have been marked with a {@link SmartMockReset}. Typically used
 * alongside {@link SmartMockitoTestExecutionListener}.
 *
 * @author Phillip Webb
 * @see SmartMockitoTestExecutionListener
 * @since 1.4.0
 */
public class ResetMocksTestExecutionListener extends AbstractTestExecutionListener {

    private static final boolean MOCKITO_IS_PRESENT = ClassUtils.isPresent("org.mockito.MockSettings",
        ResetMocksTestExecutionListener.class.getClassLoader());

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        if (MOCKITO_IS_PRESENT && !NativeDetector.inNativeImage()) {
            resetMocks(testContext.getApplicationContext(), SmartMockReset.BEFORE);
        }
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        if (MOCKITO_IS_PRESENT && !NativeDetector.inNativeImage()) {
            resetMocks(testContext.getApplicationContext(), SmartMockReset.AFTER);
        }
    }

    private void resetMocks(ApplicationContext applicationContext, SmartMockReset reset) {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) applicationContext;
            resetMocks(configurableContext, reset);
        }
    }

    private void resetMocks(ConfigurableApplicationContext applicationContext, SmartMockReset reset) {
        // TODO rewrite - reset only SmartMockBean of current test class

        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        String[] names = beanFactory.getBeanDefinitionNames();
        Set<String> instantiatedSingletons = new HashSet<>(Arrays.asList(beanFactory.getSingletonNames()));
        for (String name : names) {
            BeanDefinition definition = beanFactory.getBeanDefinition(name);
            if (definition.isSingleton() && instantiatedSingletons.contains(name)) {
                Object bean = getBean(beanFactory, name);
                if (bean != null && reset.equals(SmartMockReset.get(bean))) {
                    Mockito.reset(bean);
                }
            }
        }
        try {
            MockitoBeans mockedBeans = beanFactory.getBean(MockitoBeans.class);
            for (Object mockedBean : mockedBeans) {
                if (reset.equals(SmartMockReset.get(mockedBean))) {
                    Mockito.reset(mockedBean);
                }
            }
        } catch (NoSuchBeanDefinitionException ex) {
            // Continue
        }
        if (applicationContext.getParent() != null) {
            resetMocks(applicationContext.getParent(), reset);
        }
    }

    private Object getBean(ConfigurableListableBeanFactory beanFactory, String name) {
        try {
            if (isStandardBeanOrSingletonFactoryBean(beanFactory, name)) {
                return beanFactory.getBean(name);
            }
        } catch (Exception ex) {
            // Continue
        }
        return beanFactory.getSingleton(name);
    }

    private boolean isStandardBeanOrSingletonFactoryBean(ConfigurableListableBeanFactory beanFactory, String name) {
        String factoryBeanName = BeanFactory.FACTORY_BEAN_PREFIX + name;
        if (beanFactory.containsBean(factoryBeanName)) {
            FactoryBean<?> factoryBean = (FactoryBean<?>) beanFactory.getBean(factoryBeanName);
            return factoryBean.isSingleton();
        }
        return true;
    }

}
