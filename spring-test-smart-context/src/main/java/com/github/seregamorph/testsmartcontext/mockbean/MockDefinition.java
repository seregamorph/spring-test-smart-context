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

import org.mockito.Answers;
import org.mockito.MockSettings;
import org.springframework.core.ResolvableType;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import static org.mockito.Mockito.mock;

// Based on original code from
// https://github.com/spring-projects/spring-boot/tree/v3.3.7/spring-boot-project/spring-boot-test/src/main/java/org/springframework/boot/test/mock/mockito

/**
 * A complete definition that can be used to create a Mockito mock.
 *
 * @author Phillip Webb
 */
class MockDefinition extends Definition {

    private static final int MULTIPLIER = 31;

    private final ResolvableType typeToMock;
    private final Answers answer;

    MockDefinition(String name, ResolvableType typeToMock, Answers answer,
                   SmartMockReset reset, QualifierDefinition qualifier) {
        super(name, reset, false, qualifier);
        Assert.notNull(typeToMock, "TypeToMock must not be null");
        this.typeToMock = typeToMock;
        this.answer = answer == null ? Answers.RETURNS_DEFAULTS : answer;
    }

    /**
     * Return the type that should be mocked.
     *
     * @return the type to mock; never {@code null}
     */
    ResolvableType getTypeToMock() {
        return this.typeToMock;
    }

    /**
     * Return the answers mode.
     *
     * @return the answers mode; never {@code null}
     */
    Answers getAnswer() {
        return this.answer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        MockDefinition other = (MockDefinition) obj;
        boolean result = super.equals(obj);
        result = result && ObjectUtils.nullSafeEquals(this.typeToMock, other.typeToMock);
        result = result && ObjectUtils.nullSafeEquals(this.answer, other.answer);
        return result;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = MULTIPLIER * result + ObjectUtils.nullSafeHashCode(this.typeToMock);
        result = MULTIPLIER * result + ObjectUtils.nullSafeHashCode(this.answer);
        return result;
    }

    @Override
    public String toString() {
        return new ToStringCreator(this).append("name", getName())
            .append("typeToMock", this.typeToMock)
            .append("answer", this.answer)
            .append("reset", getReset())
            .toString();
    }

    @SuppressWarnings("unchecked")
    <T> T createMock(String name) {
        MockSettings settings = SmartMockReset.withSettings(getReset());
        if (StringUtils.hasLength(name)) {
            settings.name(name);
        }
        settings.defaultAnswer(this.answer);
        return (T) mock(this.typeToMock.resolve(), settings);
    }
}
