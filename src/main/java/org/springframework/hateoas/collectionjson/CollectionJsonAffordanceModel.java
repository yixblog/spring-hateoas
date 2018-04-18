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
package org.springframework.hateoas.collectionjson;

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
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.support.PropertyUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponents;

/**
 * @author Greg Turnquist
 */
class CollectionJsonAffordanceModel implements AffordanceModel {

	private final Affordance affordance;
	private final @Getter UriComponents components;
	private final @Getter List<AffordanceModelProperty> inputProperties;
	private final @Getter List<AffordanceModelProperty> queryProperties;

	CollectionJsonAffordanceModel(Affordance affordance, UriComponents components) {

		this.affordance = affordance;
		this.components = components;

		this.inputProperties = determineAffordanceInputs();
		this.queryProperties = determineQueryProperties();
	}

	@Override
	public Collection<MediaType> getMediaTypes() {
		return Collections.singleton(MediaTypes.COLLECTION_JSON);
	}

	public String getRel() {
		
		if (this.affordance.getHttpMethod().equals(HttpMethod.GET)) {
			return this.affordance.getName();
		} else {
			return "";
		}
	}

	/**
	 * Get the URI of an affordance.
	 *
	 * @return
	 */
	@Override
	public String getURI() {
		
		if (this.affordance.getHttpMethod().equals(HttpMethod.GET)) {
			return this.components.toUriString();
		} else {
			return "";
		}
	}

	/**
	 * Look at the inputs for a Spring MVC controller method to decide the {@link Affordance}'s properties.
	 * Then transform them into a list of {@link CollectionJsonData} objects.
	 */
	private List<AffordanceModelProperty> determineAffordanceInputs() {

		if (!Arrays.asList(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH).contains(affordance.getHttpMethod())) {
			return Collections.emptyList();
		}

		return this.affordance.getInputMethodParameters().stream()
			.findFirst()
			.map(methodParameter -> {
				ResolvableType resolvableType = ResolvableType.forMethodParameter(methodParameter);
				return PropertyUtils.findProperties(resolvableType);
			})
			.orElse(Collections.emptyList())
			.stream()
			.map(property -> new CollectionJsonData().withName(property).withValue(""))
			.collect(Collectors.toList());
	}

	/**
	 * Transform a list of {@link QueryParameter}s into a list of {@link CollectionJsonData} objects.
	 *
	 * @return
	 */
	private List<AffordanceModelProperty> determineQueryProperties() {

		if (this.affordance.getHttpMethod().equals(HttpMethod.GET)) {
			return this.affordance.getQueryMethodParameters().stream()
				.map(queryProperty -> new CollectionJsonData().withName(queryProperty.getName()).withValue(""))
				.collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}

}
