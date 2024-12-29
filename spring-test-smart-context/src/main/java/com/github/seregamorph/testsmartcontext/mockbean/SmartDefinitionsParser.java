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

import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.TypeVariable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

// Based on original code from
// https://github.com/spring-projects/spring-boot/tree/v3.3.7/spring-boot-project/spring-boot-test/src/main/java/org/springframework/boot/test/mock/mockito

/**
 * Parser to create {@link MockDefinition} and {@link SpyDefinition} instances from {@link SmartMockBean @MockBean} and
 * {@link SmartSpyBean @SpyBean} annotations declared on or in a class.
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 */
class SmartDefinitionsParser {

    private final DefinitionSet definitions;

    private final Map<Definition, Field> definitionFields;

    SmartDefinitionsParser() {
        this(new DefinitionSet());
    }

    SmartDefinitionsParser(DefinitionSet existing) {
        this.definitions = new DefinitionSet();
        this.definitionFields = new LinkedHashMap<>();
        if (existing != null) {
            this.definitions.addAll(existing);
        }
    }

    void parse(Class<?> source) {
        ReflectionUtils.doWithFields(source, field -> parseField(field, source));
    }

    private void parseField(Field field, Class<?> source) {
        MergedAnnotations annotations = MergedAnnotations.from(field, SearchStrategy.SUPERCLASS);
        annotations.stream(SmartMockBean.class)
            .map(MergedAnnotation::synthesize)
            .forEach((annotation) -> parseMockBeanAnnotation(annotation, field, source));
        annotations.stream(SmartSpyBean.class)
            .map(MergedAnnotation::synthesize)
            .forEach((annotation) -> parseSpyBeanAnnotation(annotation, field, source));
    }

    private void parseMockBeanAnnotation(SmartMockBean annotation, Field field, Class<?> source) {
        Set<ResolvableType> typesToMock = getOrDeduceTypes(field, annotation.value(), source);
        Assert.state(!typesToMock.isEmpty(), () -> "Unable to deduce type to mock from " + field);
        if (StringUtils.hasLength(annotation.name())) {
            Assert.state(typesToMock.size() == 1, "The name attribute can only be used when mocking a single class");
        }
        for (ResolvableType typeToMock : typesToMock) {
            MockDefinition definition = new MockDefinition(annotation.name(), typeToMock,
                    annotation.answer(), annotation.reset(),
                QualifierDefinition.forField(field));
            addDefinition(field, definition, "mock");
        }
    }

    private void parseSpyBeanAnnotation(SmartSpyBean annotation, Field field, Class<?> source) {
        Set<ResolvableType> typesToSpy = getOrDeduceTypes(field, annotation.value(), source);
        Assert.state(!typesToSpy.isEmpty(), () -> "Unable to deduce type to spy from " + field);
        if (StringUtils.hasLength(annotation.name())) {
            Assert.state(typesToSpy.size() == 1, "The name attribute can only be used when spying a single class");
        }
        for (ResolvableType typeToSpy : typesToSpy) {
            SpyDefinition definition = new SpyDefinition(annotation.name(), typeToSpy, annotation.reset(),
                annotation.proxyTargetAware(), QualifierDefinition.forField(field));
            addDefinition(field, definition, "spy");
        }
    }

    private void addDefinition(Field field, Definition definition, String type) {
        boolean isNewDefinition = this.definitions.add(definition);
        Assert.state(isNewDefinition, () -> "Duplicate " + type + " definition " + definition);
        this.definitionFields.put(definition, field);
    }

    private static Set<ResolvableType> getOrDeduceTypes(Field field, Class<?>[] value, Class<?> source) {
        Set<ResolvableType> types = new LinkedHashSet<>();
        for (Class<?> clazz : value) {
            types.add(ResolvableType.forClass(clazz));
        }
        if (types.isEmpty()) {
            types.add(field.getGenericType() instanceof TypeVariable ?
                    ResolvableType.forField(field, source) : ResolvableType.forField(field));
        }
        return types;
    }

    DefinitionSet getDefinitions() {
        return this.definitions;
    }

    Field getField(Definition definition) {
        return this.definitionFields.get(definition);
    }
}
