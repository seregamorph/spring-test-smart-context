/*
 * Copyright 2012-2019 the original author or authors.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Based on original code from
// https://github.com/spring-projects/spring-boot/tree/v3.3.7/spring-boot-project/spring-boot-test/src/main/java/org/springframework/boot/test/mock/mockito

/**
 * Beans created using Mockito.
 *
 * @author Andy Wilkinson
 */
class MockitoBeans implements Iterable<Object> {

	private final List<Object> beans = new ArrayList<>();

	void add(Object bean) {
		this.beans.add(bean);
	}

	@Override
	public Iterator<Object> iterator() {
		return this.beans.iterator();
	}

}