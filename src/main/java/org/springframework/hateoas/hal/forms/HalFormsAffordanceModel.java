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
import org.springframework.hateoas.AffordanceModelProperty;
import org.springframework.hateoas.MediaTypes;
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

	private final @Getter UriComponents components;
	private final boolean required;
	private final @Getter List<AffordanceModelProperty> inputProperties;

	public HalFormsAffordanceModel(Affordance affordance, UriComponents components) {

		this.components = components;
		this.required = determineRequired(affordance);
		this.inputProperties = determineAffordanceInputs(affordance);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.AffordanceModel#getMediaType()
	 */
	@Override
	public Collection<MediaType> getMediaTypes() {
		return Collections.singleton(MediaTypes.HAL_FORMS_JSON);
	}

	/**
	 * Based on the Spring MVC controller's HTTP method, decided whether or not input attributes are required or not.
	 *
	 * @param affordance
	 * @return
	 */
	private boolean determineRequired(Affordance affordance) {
		return Arrays.asList(HttpMethod.POST, HttpMethod.PUT).contains(affordance.getHttpMethod());
	}

	/**
	 * Look at the inputs for a Spring MVC controller method to decide the {@link Affordance}'s properties.
	 * @param affordance
	 */
	private List<AffordanceModelProperty> determineAffordanceInputs(Affordance affordance) {

		if (Arrays.asList(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH).contains(affordance.getHttpMethod())) {
			return affordance.getInputMethodParameters().stream()
				.findFirst()
				.map(methodParameter -> {
					ResolvableType resolvableType = ResolvableType.forMethodParameter(methodParameter);
					return PropertyUtils.findProperties(resolvableType);
				})
				.orElse(Collections.emptyList()).stream()
				.map(property -> HalFormsProperty.named(property).withRequired(required))
				.map(halFormsProperty -> (AffordanceModelProperty) halFormsProperty)
				.collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}
}
