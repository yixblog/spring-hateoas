/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.hal.forms;

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
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.hateoas.support.PropertyUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponents;

/**
 * {@link AffordanceModel} for a HAL-FORMS {@link org.springframework.http.MediaType}.
 * 
 * @author Greg Turnquist
 */
class HalFormsAffordanceModel implements AffordanceModel {

	private static final List<HttpMethod> METHODS_FOR_INPUT_DETECTION = Arrays.asList(HttpMethod.POST, HttpMethod.PUT,
			HttpMethod.PATCH);

	private final @Getter Collection<MediaType> mediaTypes = Collections.singleton(MediaTypes.HAL_FORMS_JSON);

	private final Affordance affordance;
	private final UriComponents components;
	private final boolean required;
	private final @Getter List<HalFormsProperty> properties;

	public HalFormsAffordanceModel(Affordance affordance, UriComponents components) {

		this.affordance = affordance;
		this.components = components;
		
		this.required = determineRequired();
		this.properties = determineAffordanceInputs();
	}

	public String getURI() {
		return components.toUriString();
	}

	/**
	 * Based on the affordance's HTTP method, decided whether or not input attributes are required or not.
	 */
	private boolean determineRequired() {
		return Arrays.asList(HttpMethod.POST, HttpMethod.PUT).contains(this.affordance.getHttpMethod());
	}

	/**
	 * Transform the details of the REST method's {@link MethodParameters} into {@link HalFormsProperty}s.
	 */
	private List<HalFormsProperty> determineAffordanceInputs() {

		if (!METHODS_FOR_INPUT_DETECTION.contains(this.affordance.getHttpMethod())) {
			return Collections.emptyList();
		}

		List<String> propertyNames = this.affordance.getInputMethodParameters().stream()
			.findFirst()
			.map(methodParameter -> PropertyUtils.findProperties(ResolvableType.forMethodParameter(methodParameter)))
			.orElse(Collections.emptyList());
		
		return propertyNames.stream()
			.map(name -> HalFormsProperty.named(name).withRequired(this.required))
			.collect(Collectors.toList());
	}
}
