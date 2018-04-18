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
package org.springframework.hateoas;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponents;

/**
 * An affordance model is a media type specific description of an affordance.
 * 
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
public interface AffordanceModel {

	UriComponents getComponents();

	/**
	 * The media types this is a model for. Can be multiple ones as often media types come in different flavors like an
	 * XML and JSON one and in simple cases a single model might serve them all.
	 *
	 * @return will never be {@literal null}.
	 */
	Collection<MediaType> getMediaTypes();

	/**
	 * Get the name of an affordance's relation.
	 *
	 * @return
	 */
	default String getRel() {
		return "";
	}

	/**
	 * Get the URI of an affordance.
	 * 
	 * @return
	 */
	default String getURI() {
		return getComponents().toUriString();
	}

	/**
	 * Collection of input properties needed to render a mediatype.
	 * 
	 * @return
	 */
	default List<AffordanceModelProperty> getInputProperties() {
		return Collections.emptyList();
	}

	/**
	 * Collection of query properties used to query a resource.
	 *
	 * @return
	 */
	default List<AffordanceModelProperty> getQueryProperties() {
		return Collections.emptyList();
	}

}
