/*
 * Copyright 2012-2022 the original author or authors.
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.MockSettings;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.junit4.SpringRunner;

// Based on original code from
// https://github.com/spring-projects/spring-boot/tree/v3.3.7/spring-boot-project/spring-boot-test/src/main/java/org/springframework/boot/test/mock/mockito

/**
 * Annotation that can be used to add mocks to a Spring {@link ApplicationContext}. Can be used as a class level
 * annotation or on fields in either {@code @Configuration} classes, or test classes that are {@link RunWith @RunWith}
 * the {@link SpringRunner}.
 * <p>
 * Mocks can be registered by type or by {@link #name() bean name}. When registered by type, any existing single bean of
 * a matching type (including subclasses) in the context will be replaced by the mock. When registered by name, an
 * existing bean can be specifically targeted for replacement by a mock. In either case, if no existing bean is defined
 * a new one will be added. Dependencies that are known to the application context but are not beans (such as those
 * {@link org.springframework.beans.factory.config.ConfigurableListableBeanFactory#registerResolvableDependency(Class,
 * Object) registered directly}) will not be found and a mocked bean will be added to the context alongside the existing
 * dependency.
 * <p>
 * When {@code @MockBean} is used on a field, as well as being registered in the application context, the mock will also
 * be injected into the field. Typical usage might
 * be: <pre class="code">
 * &#064;RunWith(SpringRunner.class)
 * public class ExampleTests {
 *
 *     &#064;MockBean
 *     private ExampleService service;
 *
 *     &#064;Autowired
 *     private UserOfService userOfService;
 *
 *     &#064;Test
 *     public void testUserOfService() {
 *         given(this.service.greet()).willReturn("Hello");
 *         String actual = this.userOfService.makeUse();
 *         assertEquals("Was: Hello", actual);
 *     }
 *
 *     &#064;Configuration
 *     &#064;Import(UserOfService.class) // A &#064;Component injected with ExampleService
 *     static class Config {
 *     }
 *
 *
 * }
 * </pre> If there is more than one bean of the requested type, qualifier metadata must be
 * specified at field level: <pre class="code">
 * &#064;RunWith(SpringRunner.class)
 * public class ExampleTests {
 *
 *     &#064;MockBean
 *     &#064;Qualifier("example")
 *     private ExampleService service;
 *
 *     ...
 * }
 * </pre>
 * <p>
 * @author Phillip Webb
 * @see SmartMockitoPostProcessor
 * @since 1.4.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SmartMockBean {

    /**
     * The name of the bean to register or replace. If not specified the name will either be generated or, if the mock
     * replaces an existing bean, the existing name will be used.
     *
     * @return the name of the bean
     */
    String name() default "";

    /**
     * The classes to mock. This is an alias of {@link #classes()} which can be used for brevity if no other attributes
     * are defined. See {@link #classes()} for details.
     *
     * @return the classes to mock
     */
    @AliasFor("classes")
    Class<?>[] value() default {};

    /**
     * The classes to mock. Each class specified here will result in a mock being created and registered with the
     * application context. Classes can be omitted when the annotation is used on a field.
     * <p>
     * When {@code @MockBean} also defines a {@code name} this attribute can only contain a single value.
     * <p>
     * If this is the only specified attribute consider using the {@code value} alias instead.
     *
     * @return the classes to mock
     */
    @AliasFor("value")
    Class<?>[] classes() default {};

    /**
     * The {@link Answers} type to use on the mock.
     *
     * @return the answer type
     */
    Answers answer() default Answers.RETURNS_DEFAULTS;

    /**
     * If the generated mock is serializable. See {@link MockSettings#serializable()} for details.
     *
     * @return if the mock is serializable
     */
    boolean serializable() default false;

    /**
     * The reset mode to apply to the mock bean. The default is {@link SmartMockReset#AFTER} meaning that mocks are
     * automatically reset after each test method is invoked.
     *
     * @return the reset mode
     */
    SmartMockReset reset() default SmartMockReset.AFTER;

}
