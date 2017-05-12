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
package org.springframework.hateoas.uber;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.support.MappingUtils.*;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Greg Turnquist
 */
public class UberSerializationTest {

	ObjectMapper objectMapper;

	@Before
	public void setUp() {

		this.objectMapper = new ObjectMapper();
		this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	@Test
	public void deserializingUberDocumentShouldWork() throws IOException {

		UberDocument uberDocument = this.objectMapper.readValue(
			read(new ClassPathResource("reference-1.json", getClass())), UberDocument.class);

		assertThat(uberDocument.getUber().getLinks()).hasSize(8);

		assertThat(uberDocument.getUber().getLinks().get(0)).isEqualTo(new Link("http://example.org/", "self"));
		assertThat(uberDocument.getUber().getLinks().get(1)).isEqualTo(new Link("http://example.org/list/", "collection"));
		assertThat(uberDocument.getUber().getLinks().get(2)).isEqualTo(new Link("http://example.org/search{?title}", "search"));
		assertThat(uberDocument.getUber().getLinks().get(3)).isEqualTo(new Link("http://example.org/search{?title}", "collection"));
		assertThat(uberDocument.getUber().getLinks().get(4)).isEqualTo(new Link("http://example.org/list/1", "item"));
		assertThat(uberDocument.getUber().getLinks().get(5)).isEqualTo(new Link("http://example.org/list/1", "http://example.org/rels/todo"));
		assertThat(uberDocument.getUber().getLinks().get(6)).isEqualTo(new Link("http://example.org/list/2", "item"));
		assertThat(uberDocument.getUber().getLinks().get(7)).isEqualTo(new Link("http://example.org/list/2", "http://example.org/rels/todo"));

		assertThat(uberDocument.getUber().getData()).hasSize(5);

		assertThat(uberDocument.getUber().getData().get(0)).isEqualTo(new UberData()
			.withRel(Arrays.asList("self"))
			.withUrl("http://example.org/"));

		assertThat(uberDocument.getUber().getData().get(1)).isEqualTo(new UberData()
			.withName("list")
			.withLabel("ToDo List")
			.withRel(Arrays.asList("collection"))
			.withUrl("http://example.org/list/"));

		assertThat(uberDocument.getUber().getData().get(2)).isEqualTo(new UberData()
			.withName("search")
			.withLabel("Search")
			.withRel(Arrays.asList("search", "collection"))
			.withUrl("http://example.org/search{?title}"));

		assertThat(uberDocument.getUber().getData().get(3)).isEqualTo(new UberData()
			.withName("todo")
			.withRel(Arrays.asList("item", "http://example.org/rels/todo"))
			.withUrl("http://example.org/list/1")
			.withData(Arrays.asList(new UberData()
				.withName("title")
				.withLabel("Title")
				.withValue("Clean house")
			, new UberData()
				.withName("dueDate")
				.withLabel("Date Due")
				.withValue("2014-05-01"))));

		assertThat(uberDocument.getUber().getData().get(4)).isEqualTo(new UberData()
			.withName("todo")
			.withRel(Arrays.asList("item", "http://example.org/rels/todo"))
			.withUrl("http://example.org/list/2")
				.withData(Arrays.asList(new UberData()
				.withName("title")
				.withLabel("Title")
				.withValue("Paint the fence"),
				new UberData()
				.withName("dueDate")
				.withLabel("Date Due")
				.withValue("2014-06-01"))));
	}

	@Test
	public void serializingUberDocumentShouldWork() throws IOException {

		Uber uberContainer = new Uber()
			.withVersion("1.0")
			.withData(Arrays.asList(
				new UberData()
					.withRel(Arrays.asList("self"))
					.withUrl("http://example.org/")
				, new UberData()
					.withName("list")
					.withLabel("ToDo List")
					.withRel(Arrays.asList("collection"))
					.withUrl("http://example.org/list/")
				, new UberData()
					.withName("search")
					.withLabel("Search")
					.withRel(Arrays.asList("search", "collection"))
					.withUrl("http://example.org/search{?title}")
				, new UberData()
					.withName("todo")
					.withRel(Arrays.asList("item", "http://example.org/rels/todo"))
					.withUrl("http://example.org/list/1")
					.withData(Arrays.asList(
						new UberData()
							.withName("title")
							.withLabel("Title")
							.withValue("Clean house")
							,
						new UberData()
							.withName("dueDate")
							.withLabel("Date Due")
							.withValue("2014-05-01")))
				, new UberData()
					.withName("todo")
					.withRel(Arrays.asList("item", "http://example.org/rels/todo"))
					.withUrl("http://example.org/list/2")
					.withData(Arrays.asList(new UberData()
						.withName("title")
						.withLabel("Title")
						.withValue("Paint the fence")
					, new UberData()
						.withName("dueDate")
						.withLabel("Date Due")
						.withValue("2014-06-01")))));


		String actual = this.objectMapper.writeValueAsString(new UberDocument(uberContainer));
		assertThat(actual).isEqualTo(read(new ClassPathResource("nicely-formatted-reference.json", getClass())));
	}

	@Test
	public void errorsShouldDeserializeProperly() throws IOException {

		UberDocument uberDocument = this.objectMapper.readValue(
			read(new ClassPathResource("reference-2.json", getClass())), UberDocument.class);
		
		assertThat(uberDocument.getUber().getError().getData())
			.hasSize(4)
			.containsExactly(
				new UberData()
					.withName("type")
					.withRel(Arrays.asList("https://example.com/rels/http-problem#type"))
					.withValue("out-of-credit"),
				new UberData()
					.withName("title")
					.withRel(Arrays.asList("https://example.com/rels/http-problem#title"))
					.withValue("You do not have enough credit"),
				new UberData()
					.withName("detail")
					.withRel(Arrays.asList("https://example.com/rels/http-problem#detail"))
					.withValue("Your balance is 30, but the cost is 50."),
				new UberData()
					.withName("balance")
					.withRel(Arrays.asList("https://example.com/rels/http-problem#balance"))
					.withValue("30"));
	}

	@Test
	public void anotherSpecUberDocumentToTest() throws IOException {

		UberDocument uberDocument = this.objectMapper.readValue(
			read(new ClassPathResource("reference-3.json", getClass())), UberDocument.class);

		assertThat(uberDocument.getUber().getLinks()) //
			.hasSize(6) //
			.containsExactly( //
				new Link("http://example.org/", "self"),
				new Link("http://example.org/profiles/people-and-places", "profile"),
				new Link("http://example.org/people/", "collection"),
				new Link("http://example.org/people/", "http://example.org/rels/people"),
				new Link("http://example.org/places/", "collection"),
				new Link("http://example.org/places/", "http://example.org/rels/places"));

		assertThat(uberDocument.getUber().getData())
			.hasSize(4) //
			.containsExactly(
				new UberData()
					.withRel(Arrays.asList("self"))
					.withUrl("http://example.org/"),
				new UberData()
					.withRel(Arrays.asList("profile"))
					.withUrl("http://example.org/profiles/people-and-places"),
				new UberData()
					.withId("people")
					.withRel(Arrays.asList("collection", "http://example.org/rels/people"))
					.withUrl("http://example.org/people/")
					.withData(Arrays.asList(
						new UberData()
							.withName("create")
							.withRel(Arrays.asList("http://example.org/rels/create"))
							.withUrl("http://example.org/people/")
							.withModel("g={givenName}&f={familyName}&e={email}")
							.withAction(UberAction.APPEND),
						new UberData()
							.withName("search")
							.withRel(Arrays.asList("search", "collection"))
							.withUrl("http://example.org/people/search{?givenName,familyName,email}"),
						new UberData()
							.withName("person")
							.withRel(Arrays.asList("item", "http://example.org/rels/person"))
							.withUrl("http://example.org/people/1")
							.withData(Arrays.asList(
								new UberData()
									.withName("givenName")
									.withValue("Mike")
									.withLabel("First Name"),
								new UberData()
									.withName("familyName")
									.withValue("Amundsen")
									.withLabel("Last Name"),
								new UberData()
									.withName("email")
									.withValue("mike@example.org")
									.withLabel("E-mail"),
								new UberData()
									.withName("avatarUrl")
									.withTransclude(true)
									.withUrl("http://example.org/avatars/1")
									.withValue("User Photo")
									.withAccepting(Arrays.asList("image/*")))),
						new UberData()
							.withName("person")
							.withRel(Arrays.asList("item", "http://example.org/rels/person"))
							.withUrl("http://example.org/people/2")
							.withData(Arrays.asList(
								new UberData()
									.withName("givenName")
									.withValue("Mildred")
									.withLabel("First Name"),
								new UberData()
									.withName("familyName")
									.withValue("Amundsen")
									.withLabel("Last Name"),
								new UberData()
									.withName("email")
									.withValue("mildred@example.org")
									.withLabel("E-mail"),
								new UberData()
									.withName("avatarUrl")
									.withTransclude(true)
									.withUrl("http://example.org/avatars/2")
									.withValue("User Photo")
									.withAccepting(Arrays.asList("image/*")))))),
				new UberData()
					.withId("places")
					.withRel(Arrays.asList("collection", "http://example.org/rels/places"))
					.withUrl("http://example.org/places/")
					.withData(Arrays.asList(
						new UberData()
							.withName("search")
							.withRel(Arrays.asList("search", "collection"))
							.withUrl("http://example.org/places/search{?addressRegion,addressLocality,postalCode}"),
						new UberData()
							.withName("place")
							.withRel(Arrays.asList("item", "http://example.org/rels/place"))
							.withUrl("http://example.org/places/a")
							.withData(Arrays.asList(
								new UberData()
									.withName("name")
									.withValue("Home"),
								new UberData()
									.withName("address")
									.withData(Arrays.asList(
										new UberData()
											.withName("streetAddress")
											.withValue("123 Main Street")
											.withLabel("Street Address"),
										new UberData()
											.withName("addressLocalitly") // sic
											.withValue("Byteville")
											.withLabel("City"),
										new UberData()
											.withName("addressRegion")
											.withValue("MD")
											.withLabel("State"),
										new UberData()
											.withName("postalCode")
											.withValue("12345")
											.withLabel("ZIP")
									)))),
						new UberData()
							.withName("place")
							.withRel(Arrays.asList("item", "http://example.org/rels/place"))
							.withUrl("http://example.org/places/b")
							.withData(Arrays.asList(
								new UberData()
									.withName("name")
									.withValue("Work"),
								new UberData()
									.withName("address")
									.withData(Arrays.asList(
										new UberData()
											.withName("streetAddress")
											.withValue("1456 Grand Ave.")
											.withLabel("Street Address"),
										new UberData()
											.withName("addressLocalitly") // sic
											.withValue("Byteville")
											.withLabel("City"),
										new UberData()
											.withName("addressRegion")
											.withValue("MD")
											.withLabel("State"),
										new UberData()
											.withName("postalCode")
											.withValue("12345")
											.withLabel("ZIP")
									))))))
			);
	}
}
