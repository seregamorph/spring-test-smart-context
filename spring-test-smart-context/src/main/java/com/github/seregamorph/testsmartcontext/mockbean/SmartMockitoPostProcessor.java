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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.ResolvableType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

// Based on original code from
// https://github.com/spring-projects/spring-boot/tree/v3.3.7/spring-boot-project/spring-boot-test/src/main/java/org/springframework/boot/test/mock/mockito

/**
 * A {@link BeanFactoryPostProcessor} used to register and inject {@link SmartMockBean @MockBeans} with the
 * {@link ApplicationContext}. An initial set of definitions can be passed to the processor with additional definitions
 * being automatically created from {@code @Configuration} classes that use {@link SmartMockBean @MockBean}.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @author Andreas Neiser
 * @since 1.4.0
 */
public class SmartMockitoPostProcessor implements InstantiationAwareBeanPostProcessor,
    BeanFactoryAware, BeanFactoryPostProcessor, Ordered {

    private static final String BEAN_NAME = SmartMockitoPostProcessor.class.getName();

    private static final BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();

    private final DefinitionSet definitions;

    private BeanFactory beanFactory;

    private final MockitoBeans mockitoBeans = new MockitoBeans();

    private final Map<String, SmartMockTargetSource> proxyTargetSources = new HashMap<>();

    private final Map<Definition, String> beanNameRegistry = new HashMap<>();

    private final Map<String, SpyDefinition> spies = new HashMap<>();

    /**
     * Create a new {@link SmartMockitoPostProcessor} instance with the given initial definitions.
     *
     * @param definitions the initial definitions
     */
    public SmartMockitoPostProcessor(DefinitionSet definitions) {
        this.definitions = definitions;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory,
            "Mock beans can only be used with a ConfigurableListableBeanFactory");
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Assert.isInstanceOf(BeanDefinitionRegistry.class, beanFactory,
            "@MockBean can only be used on bean factories that implement BeanDefinitionRegistry");
        postProcessBeanFactory(beanFactory, (BeanDefinitionRegistry) beanFactory);
    }

    private void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry) {
        beanFactory.registerSingleton(MockitoBeans.class.getName(), this.mockitoBeans);
        SmartDefinitionsParser parser = new SmartDefinitionsParser(this.definitions);
        DefinitionSet definitions = parser.getDefinitions();
        for (Definition definition : definitions) {
            register(beanFactory, registry, definition);
        }
    }

    private void register(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                          Definition definition) {
        if (definition instanceof MockDefinition) {
            MockDefinition mockDefinition = (MockDefinition) definition;
            registerMock(beanFactory, registry, mockDefinition);
        } else if (definition instanceof SpyDefinition) {
            SpyDefinition spyDefinition = (SpyDefinition) definition;
            registerSpy(beanFactory, registry, spyDefinition);
        }
    }

    private void registerMock(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                              MockDefinition definition) {
        RootBeanDefinition beanDefinition = createBeanDefinition(definition);
        String beanName = getBeanName(beanFactory, registry, definition, beanDefinition);
        String transformedBeanName = BeanFactoryUtils.transformedBeanName(beanName);
        if (registry.containsBeanDefinition(transformedBeanName)) {
            BeanDefinition existing = registry.getBeanDefinition(transformedBeanName);
            copyBeanDefinitionDetails(existing, beanDefinition);
            registry.removeBeanDefinition(transformedBeanName);
        }
        registry.registerBeanDefinition(transformedBeanName, beanDefinition);
        Object mock = definition.createMock(beanName + " bean");

        SmartMockTargetSource proxyTargetSource = proxyTargetSources.get(transformedBeanName);
        if (proxyTargetSource == null) {
//            proxyTargetSource = new SmartMockTargetSource(transformedBeanName, ));
            proxyTargetSources.put(transformedBeanName, proxyTargetSource);
        }

        beanFactory.registerSingleton(transformedBeanName, mock);

        this.mockitoBeans.add(mock);
        this.beanNameRegistry.put(definition, beanName);
    }

    private RootBeanDefinition createBeanDefinition(MockDefinition mockDefinition) {
        RootBeanDefinition definition = new RootBeanDefinition(mockDefinition.getTypeToMock().resolve());
        definition.setTargetType(mockDefinition.getTypeToMock());
        if (mockDefinition.getQualifier() != null) {
            mockDefinition.getQualifier().applyTo(definition);
        }
        return definition;
    }

    private String getBeanName(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                               MockDefinition mockDefinition, RootBeanDefinition beanDefinition) {
        if (StringUtils.hasLength(mockDefinition.getName())) {
            return mockDefinition.getName();
        }
        Set<String> existingBeans = getExistingBeans(beanFactory, mockDefinition.getTypeToMock(),
            mockDefinition.getQualifier());
        if (existingBeans.isEmpty()) {
            return SmartMockitoPostProcessor.beanNameGenerator.generateBeanName(beanDefinition, registry);
        }
        if (existingBeans.size() == 1) {
            return existingBeans.iterator().next();
        }
        String primaryCandidate = determinePrimaryCandidate(registry, existingBeans, mockDefinition.getTypeToMock());
        if (primaryCandidate != null) {
            return primaryCandidate;
        }
        throw new IllegalStateException("Unable to register mock bean " + mockDefinition.getTypeToMock()
            + " expected a single matching bean to replace but found " + existingBeans);
    }

    private void copyBeanDefinitionDetails(BeanDefinition from, RootBeanDefinition to) {
        to.setPrimary(from.isPrimary());
    }

    private void registerSpy(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                             SpyDefinition spyDefinition) {
        Set<String> existingBeans = getExistingBeans(beanFactory, spyDefinition.getTypeToSpy(),
            spyDefinition.getQualifier());
        if (ObjectUtils.isEmpty(existingBeans)) {
            createSpy(registry, spyDefinition);
        } else {
            registerSpies(registry, spyDefinition, existingBeans);
        }
    }

    private Set<String> getExistingBeans(ConfigurableListableBeanFactory beanFactory, ResolvableType type,
                                         QualifierDefinition qualifier) {
        Set<String> candidates = new TreeSet<>();
        for (String candidate : getExistingBeans(beanFactory, type)) {
            if (qualifier == null || qualifier.matches(beanFactory, candidate)) {
                candidates.add(candidate);
            }
        }
        return candidates;
    }

    private Set<String> getExistingBeans(ConfigurableListableBeanFactory beanFactory, ResolvableType resolvableType) {
        Set<String> beans = new LinkedHashSet<>(
            Arrays.asList(beanFactory.getBeanNamesForType(resolvableType, true, false)));
        Class<?> type = resolvableType.resolve(Object.class);
        for (String beanName : beanFactory.getBeanNamesForType(FactoryBean.class, true, false)) {
            beanName = BeanFactoryUtils.transformedBeanName(beanName);
            Class<?> producedType = beanFactory.getType(beanName, false);
            if (type.equals(producedType)) {
                beans.add(beanName);
            }
        }
        beans.removeIf(this::isScopedTarget);
        return beans;
    }

    private boolean isScopedTarget(String beanName) {
        try {
            return ScopedProxyUtils.isScopedTarget(beanName);
        } catch (Throwable ex) {
            return false;
        }
    }

    private void createSpy(BeanDefinitionRegistry registry, SpyDefinition spyDefinition) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(spyDefinition.getTypeToSpy().resolve());
        String beanName = SmartMockitoPostProcessor.beanNameGenerator.generateBeanName(beanDefinition, registry);
        registry.registerBeanDefinition(beanName, beanDefinition);
        registerSpy(spyDefinition, beanName);
    }

    private void registerSpies(BeanDefinitionRegistry registry, SpyDefinition spyDefinition,
                               Collection<String> existingBeans) {
        try {
            String beanName = determineBeanName(existingBeans, spyDefinition, registry);
            registerSpy(spyDefinition, beanName);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Unable to register spy bean " + spyDefinition.getTypeToSpy(), ex);
        }
    }

    private String determineBeanName(Collection<String> existingBeans, SpyDefinition definition,
                                     BeanDefinitionRegistry registry) {
        if (StringUtils.hasText(definition.getName())) {
            return definition.getName();
        }
        if (existingBeans.size() == 1) {
            return existingBeans.iterator().next();
        }
        return determinePrimaryCandidate(registry, existingBeans, definition.getTypeToSpy());
    }

    private String determinePrimaryCandidate(BeanDefinitionRegistry registry, Collection<String> candidateBeanNames,
                                             ResolvableType type) {
        String primaryBeanName = null;
        for (String candidateBeanName : candidateBeanNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(candidateBeanName);
            if (beanDefinition.isPrimary()) {
                if (primaryBeanName != null) {
                    throw new NoUniqueBeanDefinitionException(type.resolve(), candidateBeanNames.size(),
                        "more than one 'primary' bean found among candidates: "
                            + Collections.singletonList(candidateBeanNames));
                }
                primaryBeanName = candidateBeanName;
            }
        }
        return primaryBeanName;
    }

    private void registerSpy(SpyDefinition definition, String beanName) {
        this.spies.put(beanName, definition);
        this.beanNameRegistry.put(definition, beanName);
    }

    protected final Object createSpyIfNecessary(Object bean, String beanName) throws BeansException {
        SpyDefinition definition = this.spies.get(beanName);
        if (definition != null) {
            bean = definition.createSpy(beanName, bean);
            this.mockitoBeans.add(bean);
        }
        return bean;
    }

    void inject(Field field, Object testClassInstance, Definition definition) {
        String beanName = this.beanNameRegistry.get(definition);
        Assert.state(StringUtils.hasLength(beanName), () -> "No bean found for definition " + definition);
        inject(field, testClassInstance, beanName);
    }

    private void inject(Field field, Object testClassInstance, String beanName) {
        try {
            field.setAccessible(true);
            Object existingValue = ReflectionUtils.getField(field, testClassInstance);
            Object bean = this.beanFactory.getBean(beanName, field.getType());
            if (existingValue == bean) {
                return;
            }
            Assert.state(existingValue == null, () -> "The existing value '" + existingValue + "' of field '" + field
                + "' is not the same as the new value '" + bean + "'");
            ReflectionUtils.setField(field, testClassInstance, bean);
        } catch (Throwable ex) {
            throw new BeanCreationException("Could not inject field: " + field, ex);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }

    /**
     * Register the processor with a {@link BeanDefinitionRegistry}. Not required when using the {@link SpringRunner} as
     * registration is automatic.
     *
     * @param registry    the bean definition registry
     * @param definitions the initial mock/spy definitions
     */
    static void register(BeanDefinitionRegistry registry, DefinitionSet definitions) {
        register(registry, SmartMockitoPostProcessor.class, definitions);
    }

    /**
     * Register the processor with a {@link BeanDefinitionRegistry}. Not required when using the {@link SpringRunner} as
     * registration is automatic.
     *
     * @param registry      the bean definition registry
     * @param postProcessor the post processor class to register
     * @param definitions   the initial mock/spy definitions
     */
    private static void register(BeanDefinitionRegistry registry, Class<? extends SmartMockitoPostProcessor> postProcessor,
                                DefinitionSet definitions) {
        SpyPostProcessor.register(registry);
        BeanDefinition definition = getOrAddBeanDefinition(registry, postProcessor);
        ValueHolder constructorArg = definition.getConstructorArgumentValues().getIndexedArgumentValue(0, DefinitionSet.class);
        DefinitionSet existing = (DefinitionSet) constructorArg.getValue();
        if (definitions != null) {
            existing.addAll(definitions);
        }
    }

    private static BeanDefinition getOrAddBeanDefinition(BeanDefinitionRegistry registry,
                                                         Class<? extends SmartMockitoPostProcessor> postProcessor) {
        if (!registry.containsBeanDefinition(BEAN_NAME)) {
            RootBeanDefinition definition = new RootBeanDefinition(postProcessor);
            definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            ConstructorArgumentValues constructorArguments = definition.getConstructorArgumentValues();
            constructorArguments.addIndexedArgumentValue(0, new DefinitionSet());
            registry.registerBeanDefinition(BEAN_NAME, definition);
            return definition;
        }
        return registry.getBeanDefinition(BEAN_NAME);
    }

    /**
     * {@link BeanPostProcessor} to handle {@link SmartSpyBean} definitions. Registered as a separate processor so that it
     * can be ordered above AOP post processors.
     */
    static class SpyPostProcessor implements SmartInstantiationAwareBeanPostProcessor, PriorityOrdered {

        private static final String BEAN_NAME = SpyPostProcessor.class.getName();

        private final Map<String, Object> earlySpyReferences = new ConcurrentHashMap<>(16);

        private final SmartMockitoPostProcessor smartMockitoPostProcessor;

        SpyPostProcessor(SmartMockitoPostProcessor smartMockitoPostProcessor) {
            this.smartMockitoPostProcessor = smartMockitoPostProcessor;
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }

        @Override
        public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
            if (bean instanceof FactoryBean) {
                return bean;
            }
            this.earlySpyReferences.put(getCacheKey(bean, beanName), bean);
            return this.smartMockitoPostProcessor.createSpyIfNecessary(bean, beanName);
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof FactoryBean) {
                return bean;
            }
            if (this.earlySpyReferences.remove(getCacheKey(bean, beanName)) != bean) {
                return this.smartMockitoPostProcessor.createSpyIfNecessary(bean, beanName);
            }
            return bean;
        }

        private String getCacheKey(Object bean, String beanName) {
            return StringUtils.hasLength(beanName) ? beanName : bean.getClass().getName();
        }

        static void register(BeanDefinitionRegistry registry) {
            if (!registry.containsBeanDefinition(BEAN_NAME)) {
                RootBeanDefinition definition = new RootBeanDefinition(SpyPostProcessor.class);
                definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                ConstructorArgumentValues constructorArguments = definition.getConstructorArgumentValues();
                constructorArguments.addIndexedArgumentValue(0,
                    new RuntimeBeanReference(SmartMockitoPostProcessor.BEAN_NAME));
                registry.registerBeanDefinition(BEAN_NAME, definition);
            }
        }

    }

}
