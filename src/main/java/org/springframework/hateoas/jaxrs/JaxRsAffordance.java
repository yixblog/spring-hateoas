/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.jaxrs;

import java.lang.reflect.Method;

import org.springframework.hateoas.AbstractAffordance;

/**
 * {@link org.springframework.hateoas.Affordance} based on a string-based HTTP method.
 *
 * @author Greg Turnquist
 */
class JaxRsAffordance extends AbstractAffordance {

	private final String httpMethod;

	JaxRsAffordance(String httpMethod, Method method) {

		super(method);
		this.httpMethod = httpMethod;
	}

	/**
	 * HTTP method this affordance covers. For multiple methods, add multiple {@link org.springframework.hateoas.Affordance}s.
	 *
	 * @return
	 */
	@Override
	public String getHttpMethod() {
		return this.httpMethod;
	}
}
