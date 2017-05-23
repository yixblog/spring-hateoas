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

package org.springframework.hateoas.mvc;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import org.junit.Before;
import org.junit.Test;

import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.EvoInflectorRelProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Greg Turnquist
 */
public class SimpleIdentifiableResourceAssemblerTest {

	MockHttpServletRequest request;

	@Before
	public void setUp() {

		request = new MockHttpServletRequest();
		ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(requestAttributes);
	}

	@Test
	public void convertingToResourceShouldWork() {

		TestResourceAssembler assembler = new TestResourceAssembler();
		Resource<Employee> resource = assembler.toResource(new Employee(1, "Frodo"));

		assertThat(resource.getContent().getName(), is("Frodo"));
	}

	@Test
	public void convertingToResourcesShouldWork() {

		TestResourceAssembler assembler = new TestResourceAssembler();

		Employee frodo = new Employee(1, "Frodo");
		Resource frodoResource = new Resource(
			frodo,
			new Link("http://localhost/employees/1").withSelfRel(),
			new Link("http://localhost/employees").withRel("employees"));

		Resources<Resource<Employee>> resources = assembler.toResources(Arrays.asList(frodo));

		assertThat(resources.getContent(), hasSize(1));
		assertThat(resources.getContent(), contains(frodoResource));
		assertThat(resources.getLinks(), hasSize(1));
		assertThat(resources.getLinks(), contains(new Link("http://localhost/employees").withSelfRel()));

		assertThat(resources.getContent().iterator().next(), is(frodoResource));
	}

	@Test
	public void convertingToResourceWithCustomLinksShouldWork() {

		ResourceAssemblerWithCustomLink assembler = new ResourceAssemblerWithCustomLink();
		Employee frodo = new Employee(1, "Frodo");
		Resource<Employee> resource = assembler.toResource(frodo);

		assertThat(resource.getContent().getName(), is("Frodo"));
		assertThat(resource.getLinks(), hasSize(1));
		assertThat(resource.getLinks(), hasItem(new Link("/employees").withRel("employees")));
	}

	@Test
	public void convertingToResourcesWithCustomLinksShouldWork() {

		ResourceAssemblerWithCustomLink assembler = new ResourceAssemblerWithCustomLink();

		Employee frodo = new Employee(1, "Frodo");
		Resource frodoResource = new Resource(frodo, new Link("/employees").withRel("employees"));

		Resources<Resource<Employee>> resources = assembler.toResources(Arrays.asList(frodo));

		assertThat(resources.getContent(), hasSize(1));
		assertThat(resources.getContent(), contains(frodoResource));
		assertThat(resources.getLinks(), hasSize(1));
		assertThat(resources.getLinks(), contains(new Link("/allEmployees").withRel("all-employees")));

		assertThat(resources.getContent().iterator().next(), is(frodoResource));
	}

	@Test
	public void changingBasePathShouldWork() {

		TestResourceAssembler assembler = new TestResourceAssembler();
		assembler.setBasePath("/api");

		Employee frodo = new Employee(1, "Frodo");
		Resource<Employee> expectedEmployeeResource = new Resource(
			frodo,
			new Link("http://localhost/api/employees/1").withSelfRel(),
			new Link("http://localhost/api/employees").withRel("employees"));

		Resource<Employee> actualEmployeeResource = assembler.toResource(frodo);

		assertThat(actualEmployeeResource, is(expectedEmployeeResource));

		Resources<Resource<Employee>> expectedEmployeeResources = new Resources(Arrays.asList(expectedEmployeeResource));
		expectedEmployeeResources.add(new Link("http://localhost/api/employees").withSelfRel());

		Resources<Resource<Employee>> actualEmployeeResources = assembler.toResources(Arrays.asList(frodo));

		assertThat(actualEmployeeResources, is(expectedEmployeeResources));
	}


	class TestResourceAssembler extends SimpleIdentifiableResourceAssembler<Employee> {
		
		public TestResourceAssembler() {
			super(EmployeeController.class, new EvoInflectorRelProvider());
		}
	}

	class ResourceAssemblerWithCustomLink extends SimpleIdentifiableResourceAssembler<Employee> {

		public ResourceAssemblerWithCustomLink() {
			super(EmployeeController.class, new EvoInflectorRelProvider());

		}

		@Override
		protected void addLinks(Resource<Employee> resource) {
			resource.add(new Link("/employees").withRel("employees"));
		}

		@Override
		protected void addLinks(Resources<Resource<Employee>> resources) {
			resources.add(new Link("/allEmployees").withRel("all-employees"));
		}
	}

	@Data
	static class Employee implements Identifiable<Integer> {
		private final Integer id;
		private final String name;
	}
	
	@Controller
	static class EmployeeController {
		
		final static Map<Integer, Employee> EMPLOYEES = new HashMap<Integer, Employee>() {{
			put(1, new Employee(1, "Frodo"));
			put(2, new Employee(2, "Bilbo"));
		}};
		
		@GetMapping("/employees")
		Collection<Employee> employees() {
			return EMPLOYEES.values();
		}
		
		@GetMapping("/employees/{id}")
		Employee employee(@PathVariable int id) {
			return EMPLOYEES.get(id);
		}
	}

}