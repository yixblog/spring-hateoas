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

import static com.fasterxml.jackson.annotation.JsonInclude.*;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.support.PropertyUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Core element containing either links or data inside a {@link UberDocument}.
 * 
 * @author Greg Turnquist
 */
@Value
@Wither(AccessLevel.PACKAGE)
@JsonInclude(Include.NON_NULL)
class UberData {

	private String id;
	private String name;
	private String label;
	private List<String> rel;
	private String url;
	private UberAction action;
	private boolean transclude;
	private String model;
	private List<String> sending;
	private List<String> accepting;
	private Object value;
	private List<UberData> data;

	@JsonCreator
	UberData(@JsonProperty("id") String id, @JsonProperty("name") String name,
			 @JsonProperty("label") String label, @JsonProperty("rel") List<String> rel,
			 @JsonProperty("url") String url, @JsonProperty("action") UberAction action,
			 @JsonProperty("transclude") boolean transclude, @JsonProperty("model") String model,
			 @JsonProperty("sending") List<String> sending, @JsonProperty("accepting") List<String> accepting,
			 @JsonProperty("value") Object value, @JsonProperty("data") List<UberData> data) {

		this.id = id;
		this.name = name;
		this.label = label;
		this.rel = rel;
		this.url = url;
		this.action = action;
		this.transclude = transclude;
		this.model = model;
		this.sending = sending;
		this.accepting = accepting;
		this.value = value;
		this.data = data;
	}

	UberData() {
		this(null, null, null, null, null, UberAction.READ, false, null, null, null, null, null);
	}

	/**
	 * Don't render if it's {@link UberAction#READ}.
	 */
	public UberAction getAction() {

		if (this.action == UberAction.READ) {
			return null;
		}
		return this.action;
	}

	/*
	 * Don't render if {@literal null}.
	 */
	public List<String> getRel() {

		if (this.rel == null || this.rel.isEmpty()) {
			return null;
		}
		return this.rel;
	}

	/*
	 * Don't render if {@literal null}.
	 */
	public List<UberData> getData() {

		if (this.data == null || this.data.isEmpty()) {
			return null;
		}
		return this.data;
	}

	/*
	 * Use a {@link Boolean} to support returning {@literal null}, and if it is {@literal null}, don't render.
	 */
	public Boolean isTemplated() {

		return Optional.ofNullable(this.url)
			.map(s -> s.contains("{?") ? true : null)
			.orElse(null);
	}

	public void setTemplated(boolean __) {
		// Ignore since "templated" is a virtual property
	}

	/*
	 * Use a {@link Boolean} to support returning {@literal null}, and if it is {@literal null}, don't render.
	 */
	public Boolean isTransclude() {
		return this.transclude ? this.transclude : null;
	}

	/**
	 * Fetch all the links found in this {@link UberData}.
	 *
	 * @return
	 */
	@JsonIgnore
	public List<Link> getLinks() {

		return Optional.ofNullable(this.rel)
			.map(rels -> rels.stream()
				.map(rel -> new Link(this.url, rel))
				.collect(Collectors.toList()))
			.orElse(Collections.emptyList());
	}

	/**
	 * Simple scalar types that can be encoded by value, not type.
	 */
	private final static HashSet<Class<?>> PRIMITIVE_TYPES = new HashSet<Class<?>>() {{
		add(String.class);
	}};

	/**
	 * Convert a {@link List} of {@link Link}s into a list of {@link UberData}.
	 *
	 * @param links
	 * @return
	 */
	public static List<UberData> toUberData(List<Link> links) {

		return urlRelMap(links).entrySet().stream()
			.map(entry -> new UberData()
				.withUrl(entry.getKey())
				.withRel(entry.getValue().getRels()))
			.collect(Collectors.toList());
	}

	/**
	 * Convert a {@link ResourceSupport} into a single {@link UberData}.
	 *
	 * @param resource
	 * @return
	 */
	public static UberData toUberData(ResourceSupport resource) {

		List<UberData> data = toUberData(resource.getLinks());

		/**
		 * A base ResourceSupport has no content.
		 */
		if (!resource.getClass().equals(ResourceSupport.class)) {

			List<UberData> content = contentToUberData(resource);

			data.add(new UberData()
				.withName(resource.getClass().getSimpleName().toLowerCase())
				.withData(content));
		}

		return new UberData()
			.withData(data);
	}

		/**
		 * Convert a {@link Resource} into a single {@link UberData}.
		 *
		 * @param resource
		 * @return
		 */
	public static UberData toUberData(Resource<?> resource) {

		List<UberData> links = toUberData(resource.getLinks());

		List<UberData> affordanceBasedLinks = affordancesToUberData(resource.getLinks());

		List<UberData> allLinks = !affordanceBasedLinks.isEmpty()
			? mergeDeclaredLinksIntoAffordanceLinks(affordanceBasedLinks, links)
			: links;

		List<UberData> content = contentToUberData(resource.getContent());

		List<UberData> data = new ArrayList<>();
		data.addAll(allLinks);
		data.add(new UberData()
			.withName(resource.getContent().getClass().getSimpleName().toLowerCase())
			.withData(content));

		return new UberData()
			.withData(data);
	}

	/**
	 * Convert {@link Resources} into a list of {@link UberData}.
	 *
	 * @param resources
	 * @return
	 */
	public static List<UberData> toUberData(Resources<?> resources) {

		List<UberData> affordanceBasedLinks = affordancesToUberData(resources.getLinks());

		List<UberData> links = toUberData(resources.getLinks());

		List<UberData> allLinks = !affordanceBasedLinks.isEmpty()
			? mergeDeclaredLinksIntoAffordanceLinks(affordanceBasedLinks, links)
			: links;

		List<UberData> content = resources.getContent().stream()
			.map(item -> {
				if (item instanceof Resource) {
					return toUberData((Resource<?>) item);
				} else if (item instanceof ResourceSupport) {
					return toUberData((ResourceSupport) item);
				} else {
					return toUberData(new Resource<>(item));
				}
			})
			.collect(Collectors.toList());

		List<UberData> data = new ArrayList<>();
		data.addAll(allLinks);
		data.addAll(content);

		return data;
	}

	public static List<UberData> toUberData(PagedResources<?> resources) {

		List<UberData> collectionResources = toUberData((Resources<?>) resources);

		if (resources.getMetadata() != null ) {
			UberData pageData = new UberData()
				.withName("page")
				.withData(Arrays.asList(
					new UberData()
						.withName("number")
						.withValue(resources.getMetadata().getNumber()),
					new UberData()
						.withName("size")
						.withValue(resources.getMetadata().getSize()),
					new UberData()
						.withName("totalElements")
						.withValue(resources.getMetadata().getTotalElements()),
					new UberData()
						.withName("totalPages")
						.withValue(resources.getMetadata().getTotalPages())));
			collectionResources.add(pageData);
		}


		return collectionResources;
	}

	/**
	 * Turn a {@list List} of {@link Link}s into a {@link Map}, where you can see ALL the rels of a given
	 * link.
	 *
	 * @param links
	 * @return a map with links mapping onto a {@link List} of rels
	 */
	private static Map<String, LinkAndRels> urlRelMap(List<Link> links) {

		Map<String, LinkAndRels> urlRelMap = new LinkedHashMap<>();

		links.forEach(link -> {
			LinkAndRels linkAndRels = urlRelMap.computeIfAbsent(link.getHref(), s -> new LinkAndRels());
			linkAndRels.setLink(link);
			linkAndRels.getRels().add(link.getRel());
		});

		return urlRelMap;
	}

	/**
	 * Find all the {@link Affordance}s for a set of {@link Link}s, and convert them into {@link UberData}.
	 *
	 * @param links
	 * @return
	 */
	private static List<UberData> affordancesToUberData(List<Link> links) {

		return links.stream()
			.map(Link::getAffordances)
			.flatMap(Collection::stream)
			.map(affordance -> (UberAffordanceModel) affordance.getAffordanceModel(MediaTypes.UBER_JSON))
			.map(model -> {
				if (model.isHttpGetMethod()) {

					String suffix;
					if (model.getQueryProperties().size() > 0) {
						suffix = "{?" + model.getQueryProperties().stream().collect(Collectors.joining(",")) + "}";
					} else {
						suffix = "";
					}

					return new UberData()
						.withName(model.getRel())
						.withRel(Arrays.asList(model.getRel()))
						.withUrl(model.getURI() + suffix)
						.withAction(model.getAction());
				} else {
					return new UberData()
						.withName(model.getRel())
						.withRel(Arrays.asList(model.getRel()))
						.withUrl(model.getURI())
						.withModel(model.getInputProperties())
						.withAction(model.getAction());
				}
			})
			.collect(Collectors.toList());
	}

	/**
	 * Take a list of {@link Affordance}-based {@link Link}s, and overlay them with intersecting, declared {@link Link}s.
	 * 
	 * @param affordanceBasedLinks
	 * @param links
	 * @return
	 */
	private static List<UberData> mergeDeclaredLinksIntoAffordanceLinks(List<UberData> affordanceBasedLinks, List<UberData> links) {

		return affordanceBasedLinks.stream()
			.flatMap(affordance -> links.stream()
				.filter(link -> link.getUrl().equals(affordance.getUrl()))
				.map(link -> {
					if (link.getAction() == affordance.getAction()) {
						List<String> rels = new ArrayList<>(link.getRel());
						rels.addAll(affordance.getRel());
						return affordance
							.withName(rels.get(0))
							.withRel(rels);
					} else {
						return affordance;
					}
				}))
			.collect(Collectors.toList());
	}

	/**
	 * Transform the payload of a {@link Resource} into {@link UberData}.
	 * 
	 * @param obj
	 * @return
	 */
	private static List<UberData> contentToUberData(Object obj) {

		if (PRIMITIVE_TYPES.contains(obj.getClass())) {
			return Arrays.asList(new UberData()
				.withValue(obj));
		}

		return PropertyUtils.findProperties(obj).entrySet().stream()
			.map(entry -> new UberData()
				.withName(entry.getKey())
				.withValue(entry.getValue()))
			.collect(Collectors.toList());
	}


	/**
	 * Holds both a {@link Link} and related {@literal rels}.
	 *
	 */
	@Data
	private static class LinkAndRels {

		 private Link link;
		 private List<String> rels = new ArrayList<>();
	}
}
