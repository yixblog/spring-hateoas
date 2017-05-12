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
package org.springframework.hateoas.uber;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.ResolvableType;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.support.PropertyUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponents;

/**
 * @author Greg Turnquist
 */
class UberAffordanceModel implements AffordanceModel {

	private static final List<HttpMethod> METHODS_FOR_INPUT_DETECTION = Arrays.asList(HttpMethod.POST, HttpMethod.PUT,
		HttpMethod.PATCH);

	private final @Getter Collection<MediaType> mediaTypes = Collections.singleton(MediaTypes.UBER_JSON);

	private final Affordance affordance;
	private final UriComponents components;

	private final @Getter List<String> queryProperties;
	private final @Getter String inputProperties;

	UberAffordanceModel(Affordance affordance, UriComponents components) {

		this.affordance = affordance;
		this.components = components;

		this.queryProperties = determineQueryProperties();
		this.inputProperties = determineAffordanceInputs();
	}

	String getRel() {
		return this.affordance.getName();
	}

	String getURI() {
		return this.components.toUriString();
	}

	UberAction getAction() {
		return UberAction.forRequestMethod(this.affordance.getHttpMethod());
	}

	boolean isHttpGetMethod() {
		return this.affordance.getHttpMethod().equals(HttpMethod.GET);
	}

	/**
	 * Transform GET-based query parameters (e.g. {@literal &query}) into a list of query parameter names.
	 *
	 * @return
	 */
	private List<String> determineQueryProperties() {

		if (!isHttpGetMethod()) {
			return Collections.emptyList();
		}

		return this.affordance.getQueryMethodParameters().stream()
			.map(QueryParameter::getName)
			.collect(Collectors.toList());
	}

	/**
	 * Look at the inputs for a web method to decide the {@link Affordance}'s properties. Then transform them into a form input string.
	 */
	private String determineAffordanceInputs() {

		if (!METHODS_FOR_INPUT_DETECTION.contains(affordance.getHttpMethod())) {
			return null;
		}

		return this.affordance.getInputMethodParameters().stream()
			.findFirst()
			.map(methodParameter -> PropertyUtils.findProperties(ResolvableType.forMethodParameter(methodParameter)))
			.orElse(Collections.emptyList())
			.stream()
			.map(property -> property + "={" + property + "}")
			.collect(Collectors.joining("&"));
	}

}
