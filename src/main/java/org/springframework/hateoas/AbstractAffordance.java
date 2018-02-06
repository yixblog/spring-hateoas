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
package org.springframework.hateoas;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

/**
 * @author Greg Turnquist
 */
@RequiredArgsConstructor
public abstract class AbstractAffordance implements Affordance {

	/**
	 * {@link Map} of all {@link AffordanceModel}s gathered for this {@link Affordance}.
	 */
	private @Getter final Map<MediaType, AffordanceModel> affordanceModels = new HashMap<>();

	/**
	 * Handle on the Spring MVC controller {@link Method}.
	 */
	private @Getter final Method method;

	/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.Affordance#getName()
		 */
	@Override
	public String getName() {
		return this.method.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.Affordance#getAffordanceModel(org.springframework.http.MediaType)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends AffordanceModel> T getAffordanceModel(MediaType mediaType) {
		return (T) getAffordanceModels().get(mediaType);
	}

	/**
	 * Adds the given {@link AffordanceModel} to the {@link Affordance}.
	 *
	 * @param affordanceModel must not be {@literal null}.
	 */
	public void addAffordanceModel(AffordanceModel affordanceModel) {

		Assert.notNull(affordanceModel, "Affordance model must not be null!");

		for (MediaType mediaType : affordanceModel.getMediaTypes()) {
			this.affordanceModels.put(mediaType, affordanceModel);
		}
	}

}
