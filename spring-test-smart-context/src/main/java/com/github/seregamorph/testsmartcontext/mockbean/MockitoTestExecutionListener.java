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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

// Based on original code from
// https://github.com/spring-projects/spring-boot/tree/v3.3.7/spring-boot-project/spring-boot-test/src/main/java/org/springframework/boot/test/mock/mockito

/**
 * {@link TestExecutionListener} to enable {@link MockBean @MockBean} and
 * {@link SpyBean @SpyBean} support. Also triggers
 * {@link MockitoAnnotations#openMocks(Object)} when any Mockito annotations used,
 * primarily to allow {@link Captor @Captor} annotations.
 * <p>
 * To use the automatic reset support of {@code @MockBean} and {@code @SpyBean}, configure
 * {@link ResetMocksTestExecutionListener} as well.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Moritz Halbritter
 * @since 1.4.2
 * @see ResetMocksTestExecutionListener
 */
public class MockitoTestExecutionListener extends AbstractTestExecutionListener {

	private static final String MOCKS_ATTRIBUTE_NAME = MockitoTestExecutionListener.class.getName() + ".mocks";

	@Override
	public final int getOrder() {
		return 1950;
	}

	@Override
	public void prepareTestInstance(TestContext testContext) throws Exception {
		closeMocks(testContext);
		initMocks(testContext);
		injectFields(testContext);
	}

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		if (Boolean.TRUE.equals(
				testContext.getAttribute(DependencyInjectionTestExecutionListener.REINJECT_DEPENDENCIES_ATTRIBUTE))) {
			closeMocks(testContext);
			initMocks(testContext);
			reinjectFields(testContext);
		}
	}

	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		closeMocks(testContext);
	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		closeMocks(testContext);
	}

	private void initMocks(TestContext testContext) {
		if (hasMockitoAnnotations(testContext)) {
			testContext.setAttribute(MOCKS_ATTRIBUTE_NAME, MockitoAnnotations.openMocks(testContext.getTestInstance()));
		}
	}

	private void closeMocks(TestContext testContext) throws Exception {
		Object mocks = testContext.getAttribute(MOCKS_ATTRIBUTE_NAME);
		if (mocks instanceof AutoCloseable) {
            AutoCloseable closeable = (AutoCloseable) mocks;
            closeable.close();
		}
	}

	private boolean hasMockitoAnnotations(TestContext testContext) {
		MockitoAnnotationCollection collector = new MockitoAnnotationCollection();
		ReflectionUtils.doWithFields(testContext.getTestClass(), collector);
		return collector.hasAnnotations();
	}

	private void injectFields(TestContext testContext) {
		postProcessFields(testContext, (mockitoField, postProcessor) -> postProcessor.inject(mockitoField.field,
				mockitoField.target, mockitoField.definition));
	}

	private void reinjectFields(final TestContext testContext) {
		postProcessFields(testContext, (mockitoField, postProcessor) -> {
			ReflectionUtils.makeAccessible(mockitoField.field);
			ReflectionUtils.setField(mockitoField.field, testContext.getTestInstance(), null);
			postProcessor.inject(mockitoField.field, mockitoField.target, mockitoField.definition);
		});
	}

	private void postProcessFields(TestContext testContext, BiConsumer<MockitoField, MockitoPostProcessor> consumer) {
		DefinitionsParser parser = new DefinitionsParser();
		parser.parse(testContext.getTestClass());
		if (!parser.getDefinitions().isEmpty()) {
			MockitoPostProcessor postProcessor = testContext.getApplicationContext()
				.getBean(MockitoPostProcessor.class);
			for (Definition definition : parser.getDefinitions()) {
				Field field = parser.getField(definition);
				if (field != null) {
					consumer.accept(new MockitoField(field, testContext.getTestInstance(), definition), postProcessor);
				}
			}
		}
	}

	/**
	 * {@link FieldCallback} to collect Mockito annotations.
	 */
	private static final class MockitoAnnotationCollection implements FieldCallback {

		private final Set<Annotation> annotations = new LinkedHashSet<>();

		@Override
		public void doWith(Field field) throws IllegalArgumentException {
			for (Annotation annotation : field.getDeclaredAnnotations()) {
				if (annotation.annotationType().getName().startsWith("org.mockito")) {
					this.annotations.add(annotation);
				}
			}
		}

		boolean hasAnnotations() {
			return !this.annotations.isEmpty();
		}

	}

	private static final class MockitoField {

		private final Field field;

		private final Object target;

		private final Definition definition;

		private MockitoField(Field field, Object instance, Definition definition) {
			this.field = field;
			this.target = instance;
			this.definition = definition;
		}

	}

}
